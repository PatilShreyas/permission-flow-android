/**
 * Copyright 2022 Shreyas Patil
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.shreyaspatil.permissionFlow.watchmen

import dev.shreyaspatil.permissionFlow.MultiplePermissionState
import dev.shreyaspatil.permissionFlow.PermissionState
import dev.shreyaspatil.permissionFlow.internal.ApplicationStateMonitor
import dev.shreyaspatil.permissionFlow.utils.stateFlow.combineStates
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

/**
 * A watchmen which keeps watching state changes of permissions and events of permissions.
 */
@Suppress("unused")
internal class PermissionWatchmen(
    private val appStateMonitor: ApplicationStateMonitor,
    dispatcher: CoroutineDispatcher,
) {
    private val watchmenScope = CoroutineScope(
        dispatcher +
            SupervisorJob() +
            CoroutineName("PermissionWatchmen"),
    )

    private var watchEventsJob: Job? = null
    private var watchActivityEventJob: Job? = null

    /**
     * A in-memory store for storing permission and its state holder i.e. [StateFlow]
     */
    private val permissionFlows = mutableMapOf<String, PermissionStateFlowDelegate>()

    private val permissionEvents = MutableSharedFlow<PermissionState>()

    fun watchState(permission: String): StateFlow<PermissionState> {
        // Wakeup watchmen if sleeping
        wakeUp()
        return getOrCreatePermissionStateFlow(permission)
    }

    fun watchMultipleState(permissions: Array<String>): StateFlow<MultiplePermissionState> {
        // Wakeup watchmen if sleeping
        wakeUp()

        val permissionStates = permissions
            .distinct()
            .map { getOrCreatePermissionStateFlow(it) }
            .toTypedArray()

        return combineStates(*permissionStates) { MultiplePermissionState(it.toList()) }
    }

    fun watchStateEvents(permission: String): Flow<PermissionState> {
        // Add permission to state watchlist too
        watchState(permission)
        return getPermissionEvent(permission)
    }

    fun notifyPermissionsChanged(permissions: Array<String>) {
        watchmenScope.launch {
            permissions.forEach { permission ->
                permissionEvents.emit(appStateMonitor.getPermissionState(permission))
            }
        }
    }

    @Synchronized
    fun wakeUp() {
        watchPermissionEvents()
        watchActivities()
        notifyAllPermissionsChanged()
    }

    @Synchronized
    fun sleep() {
        watchmenScope.coroutineContext.cancelChildren()
    }

    /**
     * First finds for existing flow (if available) otherwise creates a new [MutableStateFlow]
     * for [permission] and returns a read-only [StateFlow] for a [permission].
     */
    @Synchronized
    private fun getOrCreatePermissionStateFlow(permission: String): StateFlow<PermissionState> {
        return permissionFlows.getOrPut(permission) {
            PermissionStateFlowDelegate(appStateMonitor.getPermissionState(permission))
        }.state
    }

    private fun getPermissionEvent(permission: String) =
        permissionEvents.filter { it.permission == permission }

    /**
     * Watches for the permission events and updates appropriate state holders of permission
     */
    private fun watchPermissionEvents() {
        if (watchEventsJob != null && watchEventsJob?.isActive == true) return
        watchEventsJob = watchmenScope.launch {
            permissionEvents.collect {
                permissionFlows[it.permission]?.setState(it)
            }
        }
    }

    /**
     * Watches for activity foreground events (to detect whether user has changed permission by
     * going in settings) and recalculates state of the permissions which are currently being
     * observed.
     */
    @OptIn(FlowPreview::class)
    private fun watchActivities() {
        if (watchActivityEventJob != null && watchActivityEventJob?.isActive == true) return
        watchActivityEventJob = appStateMonitor.activityForegroundEvents
            .onEach {
                // Since this is not priority task, we want to yield current thread for other
                // tasks for the watchmen.
                yield()
                notifyAllPermissionsChanged()
            }.launchIn(watchmenScope)
    }

    private fun notifyAllPermissionsChanged() {
        if (permissionFlows.isEmpty()) return
        notifyPermissionsChanged(permissionFlows.keys.toTypedArray())
    }

    /**
     * A delegate for [MutableStateFlow] which creates flow for holding state of a permission.
     */
    private class PermissionStateFlowDelegate(initialState: PermissionState) {

        private val _state = MutableStateFlow(initialState)
        val state = _state.asStateFlow()

        fun setState(newState: PermissionState) {
            _state.value = newState
        }
    }
}

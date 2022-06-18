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

import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dev.shreyaspatil.permissionFlow.utils.activityForegroundEventFlow
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

/**
 * A watchmen which keeps watching state changes of permissions and events of permissions.
 */
@OptIn(FlowPreview::class)
@Suppress("OPT_IN_IS_NOT_ENABLED", "unused")
internal class PermissionWatchmen(
    private val application: Application,
    dispatcher: CoroutineDispatcher
) {
    private val watchmenScope = CoroutineScope(
        dispatcher +
            SupervisorJob() +
            CoroutineName("PermissionWatchmen")
    )

    private var watchEventsJob: Job? = null
    private var watchActivityEventJob: Job? = null

    /**
     * A in-memory store for storing permission and its state holder i.e. [StateFlow]
     */
    private val permissionFlows = mutableMapOf<String, MutableStateFlow<Boolean>>()

    private val permissionEvents = MutableSharedFlow<PermissionEvent>()

    fun watch(permission: String): StateFlow<Boolean> {
        wakeUp()
        return getOrCreatePermissionStateFlow(permission)
    }

    fun notifyPermissionsChanged(permissions: Array<String>) {
        watchmenScope.launch {
            permissions.forEach { permission ->
                permissionEvents.emit(
                    PermissionEvent(
                        permission = permission,
                        isGranted = isPermissionGranted(permission)
                    )
                )
            }
        }
    }

    @Synchronized
    fun wakeUp() {
        notifyAllPermissionsChanged()
        watchPermissionEvents()
        watchActivities()
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
    private fun getOrCreatePermissionStateFlow(permission: String): StateFlow<Boolean> {
        val flow = permissionFlows[permission]
            ?: MutableStateFlow(isPermissionGranted(permission)).also {
                permissionFlows[permission] = it
            }

        return flow.asStateFlow()
    }

    /**
     * Watches for the permission events and updates appropriate state holders of permission
     */
    private fun watchPermissionEvents() {
        if (watchEventsJob != null && watchEventsJob?.isActive == true) return
        watchEventsJob = watchmenScope.launch {
            permissionEvents.collect { (permission, isGranted) ->
                permissionFlows[permission]?.value = isGranted
            }
        }
    }

    /**
     * Watches for activity foreground events (to detect whether user has changed permission by
     * going in settings) and recalculates state of the permissions which are currently being
     * observed.
     */
    private fun watchActivities() {
        if (watchActivityEventJob != null && watchActivityEventJob?.isActive == true) return
        watchActivityEventJob = application.activityForegroundEventFlow
            // This is just to avoid frequent events.
            .debounce(DEFAULT_DEBOUNCE_FOR_ACTIVITY_CALLBACK)
            .onEach {
                // Since this is not priority task, we want to yield current thread for other
                // tasks for the watchmen.
                yield()
                notifyAllPermissionsChanged()
            }
            .launchIn(watchmenScope)
    }

    private fun notifyAllPermissionsChanged() {
        if (permissionFlows.isEmpty()) return
        notifyPermissionsChanged(permissionFlows.keys.toTypedArray())
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            application,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private data class PermissionEvent(val permission: String, val isGranted: Boolean)

    companion object {
        private const val DEFAULT_DEBOUNCE_FOR_ACTIVITY_CALLBACK = 5_000L
    }
}

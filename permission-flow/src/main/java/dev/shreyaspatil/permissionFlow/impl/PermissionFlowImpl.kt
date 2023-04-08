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
package dev.shreyaspatil.permissionFlow.impl

import android.app.Application
import android.content.Context
import androidx.annotation.VisibleForTesting
import dev.shreyaspatil.permissionFlow.MultiplePermissionState
import dev.shreyaspatil.permissionFlow.PermissionFlow
import dev.shreyaspatil.permissionFlow.PermissionState
import dev.shreyaspatil.permissionFlow.watchmen.PermissionWatchmen
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow

/**
 * Default implementation of a [PermissionFlow]
 */
internal class PermissionFlowImpl @VisibleForTesting constructor(
    private val watchmen: PermissionWatchmen,
) : PermissionFlow {

    override fun getPermissionState(permission: String): StateFlow<PermissionState> {
        return watchmen.watch(permission)
    }

    override fun getMultiplePermissionState(vararg permissions: String): StateFlow<MultiplePermissionState> {
        return watchmen.watchMultiple(permissions.toList().toTypedArray())
    }

    override fun notifyPermissionsChanged(vararg permissions: String) {
        watchmen.notifyPermissionsChanged(permissions.toList().toTypedArray())
    }

    override fun startListening() {
        watchmen.wakeUp()
    }

    override fun stopListening() {
        watchmen.sleep()
    }

    internal companion object {
        @Volatile
        var instance: PermissionFlowImpl? = null
            private set

        @Synchronized
        fun init(context: Context, dispatcher: CoroutineDispatcher) {
            if (instance == null) {
                val watchmen = PermissionWatchmen(
                    application = context.applicationContext as Application,
                    dispatcher = dispatcher,
                )
                instance = PermissionFlowImpl(watchmen)
            }
        }
    }
}

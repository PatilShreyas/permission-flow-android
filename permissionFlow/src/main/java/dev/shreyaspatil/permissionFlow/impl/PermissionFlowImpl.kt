package dev.shreyaspatil.permissionFlow.impl

import android.app.Application
import android.content.Context
import androidx.annotation.VisibleForTesting
import dev.shreyaspatil.permissionFlow.PermissionFlow
import dev.shreyaspatil.permissionFlow.watchmen.PermissionWatchmen
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow

/**
 * Default implementation of a [PermissionFlow]
 */
internal class PermissionFlowImpl @VisibleForTesting constructor(
    private val watchmen: PermissionWatchmen
) : PermissionFlow {

    private constructor(
        application: Application,
        dispatcher: CoroutineDispatcher
    ) : this(PermissionWatchmen(application, dispatcher))

    override fun get(permission: String): StateFlow<Boolean> {
        return watchmen.watch(permission)
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
                instance = PermissionFlowImpl(context.applicationContext as Application, dispatcher)
            }
        }
    }
}
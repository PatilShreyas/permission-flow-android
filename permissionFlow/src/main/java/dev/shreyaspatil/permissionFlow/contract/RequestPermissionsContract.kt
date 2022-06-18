package dev.shreyaspatil.permissionFlow.contract

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import dev.shreyaspatil.permissionFlow.PermissionFlow
import dev.shreyaspatil.permissionFlow.utils.registerForPermissionFlowRequestsResult

/**
 * An [ActivityResultContract] which delegates request and response to
 * [ActivityResultContracts.RequestMultiplePermissions] and silently notifier [PermissionFlow]
 * regarding state change of a permissions which are requested through [ActivityResultLauncher].
 *
 * Refer to [ComponentActivity.registerForPermissionFlowRequestsResult] for actual usage.
 */

class RequestPermissionsContract : ActivityResultContract<Array<String>, Map<String, Boolean>>() {

    private val contract = ActivityResultContracts.RequestMultiplePermissions()

    override fun createIntent(context: Context, input: Array<String>?): Intent {
        return contract.createIntent(context, input ?: emptyArray())
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Map<String, Boolean> {
        return contract.parseResult(resultCode, intent).also {
            val permissions = it.keys.filterNotNull().toTypedArray()
            PermissionFlow.getInstance().notifyPermissionsChanged(*permissions)
        }
    }
}

@file:JvmName("ComponentActivityExt")
@file:Suppress("UNUSED_PARAMETER")

package dev.shreyaspatil.permissionFlow.utils

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import dev.shreyaspatil.permissionFlow.PermissionFlow
import dev.shreyaspatil.permissionFlow.contract.RequestPermissionsContract

/**
 * Returns a [ActivityResultLauncher] which internally notifies [PermissionFlow] about the state
 * change whenever permission state is changed with this launcher.
 *
 * Usage:
 *
 * ```
 *  class MyActivity: AppCompatActivity() {
 *      private val permissionLauncher = registerForPermissionFlowRequestsResult()
 *
 *      fun askContactPermission() {
 *          permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
 *      }
 *  }
 * ```
 *
 * @param activityResultRegistry Activity result registry. By default it uses Activity's Result
 * registry.
 * @param callback Callback of a permission state change.
 */
@JvmOverloads
fun ComponentActivity.registerForPermissionFlowRequestsResult(
    activityResultRegistry: ActivityResultRegistry = getActivityResultRegistry(),
    callback: ActivityResultCallback<Map<String, Boolean>> = emptyCallback()
) = registerForActivityResult(RequestPermissionsContract(), callback)

private fun <T> emptyCallback() = ActivityResultCallback<T> {}

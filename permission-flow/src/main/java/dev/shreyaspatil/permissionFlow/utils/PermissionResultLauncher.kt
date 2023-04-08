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
@file:JvmName("PermissionResultLauncher")

package dev.shreyaspatil.permissionFlow.utils

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.fragment.app.Fragment
import dev.shreyaspatil.permissionFlow.PermissionFlow
import dev.shreyaspatil.permissionFlow.contract.RequestPermissionsContract

/**
 * Returns a [ActivityResultLauncher] for this Activity which internally notifies [PermissionFlow]
 * about the state change whenever permission state is changed with this launcher.
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
 * @param requestPermissionsContract A contract specifying permission request and result.
 * @param activityResultRegistry Activity result registry. By default it uses Activity's Result
 * registry.
 * @param callback Callback of a permission state change.
 */
@JvmOverloads
fun ComponentActivity.registerForPermissionFlowRequestsResult(
    requestPermissionsContract: RequestPermissionsContract = RequestPermissionsContract(),
    activityResultRegistry: ActivityResultRegistry = getActivityResultRegistry(),
    callback: ActivityResultCallback<Map<String, Boolean>> = emptyCallback(),
): ActivityResultLauncher<Array<String>> = registerForActivityResult(
    requestPermissionsContract,
    activityResultRegistry,
    callback,
)

/**
 * Returns a [ActivityResultLauncher] for this Fragment which internally notifies [PermissionFlow]
 * about the state change whenever permission state is changed with this launcher.
 *
 * Usage:
 *
 * ```
 *  class MyFragment: Fragment() {
 *      private val permissionLauncher = registerForPermissionFlowRequestsResult()
 *
 *      fun askContactPermission() {
 *          permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
 *      }
 *  }
 * ```
 *
 * @param requestPermissionsContract A contract specifying permission request and result.
 * @param activityResultRegistry Activity result registry. By default it uses Activity's Result
 * registry.
 * @param callback Callback of a permission state change.
 */
@JvmOverloads
fun Fragment.registerForPermissionFlowRequestsResult(
    requestPermissionsContract: RequestPermissionsContract = RequestPermissionsContract(),
    activityResultRegistry: ActivityResultRegistry = requireActivity().activityResultRegistry,
    callback: ActivityResultCallback<Map<String, Boolean>> = emptyCallback(),
): ActivityResultLauncher<Array<String>> = registerForActivityResult(
    requestPermissionsContract,
    activityResultRegistry,
    callback,
)

private fun <T> emptyCallback() = ActivityResultCallback<T> {}

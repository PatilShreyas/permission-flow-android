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
package dev.shreyaspatil.permissionflow.compose

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import dev.shreyaspatil.permissionFlow.MultiplePermissionState
import dev.shreyaspatil.permissionFlow.PermissionFlow
import dev.shreyaspatil.permissionFlow.PermissionState
import dev.shreyaspatil.permissionFlow.contract.RequestPermissionsContract

/**
 * Creates a [PermissionState] for a [permission] that is remembered across compositions.
 *
 * Example:
 * ```
 *  @Composable
 *  fun PermissionDemo() {
 *      val state by rememberPermissionState(Manifest.permission.CAMERA)
 *      if (state.isGranted) {
 *          // Render something
 *      } else {
 *          if (state.isRationaleRequired) {
 *              // Show rationale
 *          }
 *          // Render something else
 *      }
 *  }
 * ```
 *
 * @param permission The permission to observe.
 *
 * @throws IllegalStateException If [PermissionFlow] is not initialized
 */
@Composable
fun rememberPermissionState(
    permission: String,
): State<PermissionState> = remember {
    PermissionFlow.getInstance().getPermissionState(permission)
}.collectAsState()

/**
 * Creates a [MultiplePermissionState] for a multiple [permissions] that is remembered
 * across compositions.
 *
 * Example:
 * ```
 *  @Composable
 *  fun PermissionDemo() {
 *      val state by rememberMultiplePermissionState(
 *          Manifest.permission.CAMERA
 *          Manifest.permission.ACCESS_FINE_LOCATION,
 *          Manifest.permission.READ_CONTACTS
 *      )
 *
 *      if (state.allGranted) {
 *          // Render something
 *      }
 *
 *      val grantedPermissions = state.grantedPermissions
 *      // Do something with `grantedPermissions`
 *
 *      val deniedPermissions = state.deniedPermissions
 *      // Do something with `deniedPermissions`
 *  }
 * ```
 *
 * @param permissions The list of permissions to observe.
 *
 * @throws IllegalStateException If [PermissionFlow] is not initialized
 */
@Composable
fun rememberMultiplePermissionState(
    vararg permissions: String,
): State<MultiplePermissionState> = remember {
    PermissionFlow.getInstance().getMultiplePermissionState(*permissions)
}.collectAsState()

/**
 * Creates a [ManagedActivityResultLauncher] that is tied with [PermissionFlow] APIs and
 * remembered across recompositions.
 *
 * Example:
 *
 * ```
 *  @Composable
 *  fun DemoPermissionLaunch() {
 *      val permissionLauncher = rememberPermissionFlowRequestLauncher()
 *
 *      Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
 *          Text("Launch Camera Permission Request")
 *      }
 *  }
 * ```
 *
 * Make sure to use [ManagedActivityResultLauncher.launch] method inside callback or a side effect
 * and not directly in the compose scope. Otherwise, it'll be invoked across recompositions.
 *
 * @param onResult The callback to invoke when the result is received.
 */
@Composable
fun rememberPermissionFlowRequestLauncher(
    onResult: (Map<String, Boolean>) -> Unit = {},
): ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>> {
    return rememberLauncherForActivityResult(
        contract = RequestPermissionsContract(),
        onResult = onResult,
    )
}

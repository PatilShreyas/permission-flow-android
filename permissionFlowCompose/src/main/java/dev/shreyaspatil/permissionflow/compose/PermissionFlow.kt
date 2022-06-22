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

@Composable
fun rememberPermissionState(
    permission: String
): State<PermissionState> = remember {
    PermissionFlow.getInstance().getPermissionState(permission)
}.collectAsState()

@Composable
fun rememberMultiplePermissionState(
    vararg permissions: String
): State<MultiplePermissionState> = remember {
    PermissionFlow.getInstance().getMultiplePermissionState(*permissions)
}.collectAsState()

@Composable
fun rememberPermissionFlowRequestLauncher(onResult: (Map<String, Boolean>) -> Unit): ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>> {
    return rememberLauncherForActivityResult(
        contract = RequestPermissionsContract(),
        onResult = onResult
    )
}


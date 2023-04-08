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
package dev.shreyaspatil.permissionFlow.contract

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import dev.shreyaspatil.permissionFlow.PermissionFlow
import dev.shreyaspatil.permissionFlow.utils.registerForPermissionFlowRequestsResult

/**
 * An [ActivityResultContract] which delegates request and response to
 * [ActivityResultContracts.RequestMultiplePermissions] and silently notifies [PermissionFlow]
 * regarding state change of a permissions which are requested through [ActivityResultLauncher].
 *
 * Refer to [ComponentActivity.registerForPermissionFlowRequestsResult] for actual usage.
 */

class RequestPermissionsContract(
    private val contract: RequestMultiplePermissions = RequestMultiplePermissions(),
    private val permissionFlow: PermissionFlow = PermissionFlow.getInstance(),
) : ActivityResultContract<Array<String>, Map<String, Boolean>>() {

    override fun createIntent(context: Context, input: Array<String>): Intent {
        return contract.createIntent(context, input ?: emptyArray())
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Map<String, Boolean> {
        return contract.parseResult(resultCode, intent).also {
            val permissions = it.keys.filterNotNull().toTypedArray()
            permissionFlow.notifyPermissionsChanged(*permissions)
        }
    }
}

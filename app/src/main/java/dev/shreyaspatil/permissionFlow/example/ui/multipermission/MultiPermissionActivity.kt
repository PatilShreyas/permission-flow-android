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
package dev.shreyaspatil.permissionFlow.example.ui.multipermission

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dev.shreyaspatil.permissionFlow.MultiplePermissionState
import dev.shreyaspatil.permissionFlow.PermissionFlow
import dev.shreyaspatil.permissionFlow.example.databinding.ActivityMultipermissionBinding
import dev.shreyaspatil.permissionFlow.utils.registerForPermissionFlowRequestsResult
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * This example activity shows how to use [PermissionFlow] to request multiple permissions at once
 * and observe multiple permissions at once.
 */
class MultiPermissionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMultipermissionBinding

    private val permissionFlow = PermissionFlow.getInstance()

    private val permissionLauncher = registerForPermissionFlowRequestsResult()

    private val permissions = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_CALL_LOG,
        android.Manifest.permission.READ_CONTACTS,
        android.Manifest.permission.READ_PHONE_STATE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultipermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        observePermissions()
    }

    private fun initViews() {
        binding.buttonAskPermission.setOnClickListener {
            permissionLauncher.launch(permissions)
        }
    }

    private fun observePermissions() {
        permissionFlow.getMultiplePermissionState(*permissions) // or use `getPermissionState()` for observing a single permission
            .flowWithLifecycle(lifecycle)
            .onEach { onPermissionStateChange(it) }
            .launchIn(lifecycleScope)
    }

    private fun onPermissionStateChange(state: MultiplePermissionState) {
        if (state.allGranted) {
            Toast.makeText(this, "All permissions are granted!", Toast.LENGTH_SHORT).show()
        }

        binding.textViewGrantedPermissions.text =
            "Granted permissions: ${state.grantedPermissions.joinToStringByNewLine()}"
        binding.textViewDeniedPermissions.text =
            "Denied permissions: ${state.deniedPermissions.joinToStringByNewLine()}"
    }

    private fun List<String>.joinToStringByNewLine(): String {
        return joinToString(prefix = "\n", postfix = "\n", separator = ",\n")
    }
}

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
package dev.shreyaspatil.permissionFlow.example.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dev.shreyaspatil.permissionFlow.PermissionFlow
import dev.shreyaspatil.permissionFlow.example.databinding.ViewFragmentExampleBinding
import dev.shreyaspatil.permissionFlow.utils.registerForPermissionFlowRequestsResult
import kotlinx.coroutines.launch

class ExampleFragment : Fragment() {

    private var _binding: ViewFragmentExampleBinding? = null
    private val binding: ViewFragmentExampleBinding get() = _binding!!

    private val permissions = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.READ_CALL_LOG,
        android.Manifest.permission.READ_CONTACTS,
        android.Manifest.permission.READ_PHONE_STATE,
    )

    private val permissionFlow = PermissionFlow.getInstance()

    private val permissionLauncher = registerForPermissionFlowRequestsResult()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = ViewFragmentExampleBinding.inflate(inflater, null, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observePermissions()
    }

    private fun initView() {
        binding.button.setOnClickListener {
            permissionLauncher.launch(permissions)
        }
    }

    private fun observePermissions() {
        viewLifecycleOwner.lifecycleScope.launch {
            permissionFlow.getMultiplePermissionState(*permissions).collect {
                val grantedPermissionsText = it.grantedPermissions.joinToString(
                    separator = "\n",
                    prefix = "Granted Permissions:\n",
                )
                val deniedPermissionsText = it.deniedPermissions.joinToString(
                    separator = "\n",
                    prefix = "Denied Permissions:\n",
                )

                binding.grantedPermissionsText.text = grantedPermissionsText
                binding.deniedPermissionsText.text = deniedPermissionsText

                binding.button.isVisible = !it.allGranted
            }
        }
    }
}

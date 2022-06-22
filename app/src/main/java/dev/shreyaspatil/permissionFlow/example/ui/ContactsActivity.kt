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
package dev.shreyaspatil.permissionFlow.example.ui

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dev.shreyaspatil.permissionFlow.example.databinding.ActivityContactsBinding
import dev.shreyaspatil.permissionFlow.utils.launch
import dev.shreyaspatil.permissionFlow.utils.registerForPermissionFlowRequestsResult
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ContactsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactsBinding
    private val viewModel by viewModels<ContactsViewModel> {
        ContactsViewModel.FactoryProvider(contentResolver).get()
    }

    private val permissionLauncher = registerForPermissionFlowRequestsResult()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeStates()
    }

    private fun render(state: ContactsUiState) {
        when (state) {
            ContactsUiState.ContactPermissionGranted -> {
                binding.permissionStatusText.apply {
                    text = "Contacts Permission Granted!"
                    setOnClickListener(null)
                }
            }
            ContactsUiState.ContactPermissionNotGranted -> {
                binding.permissionStatusText.apply {
                    text = "Click here to ask for contacts permission"
                    setOnClickListener { askContactsPermission() }
                }
            }
            is ContactsUiState.ContactsAvailable -> {
                binding.contactsDataText.text = state.contacts.joinToString("\n") {
                    "${it.id}. ${it.name} (${it.number})"
                }
            }
            is ContactsUiState.Failure -> {
                Toast.makeText(this, state.error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun askContactsPermission() {
        permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
    }

    private fun observeStates() {
        viewModel.state
            .flowWithLifecycle(lifecycle)
            .onEach { render(it) }
            .launchIn(lifecycleScope)
    }
}

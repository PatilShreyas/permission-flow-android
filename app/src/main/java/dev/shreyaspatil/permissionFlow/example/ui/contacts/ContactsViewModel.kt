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
package dev.shreyaspatil.permissionFlow.example.ui.contacts

import android.Manifest
import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.ViewModelInitializer
import dev.shreyaspatil.permissionFlow.PermissionFlow
import dev.shreyaspatil.permissionFlow.example.data.ContactRepository
import dev.shreyaspatil.permissionFlow.example.data.impl.AndroidDefaultContactRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ContactsViewModel(
    private val repository: ContactRepository,
    private val permissionFlow: PermissionFlow = PermissionFlow.getInstance(),
) : ViewModel() {
    private val uiEvents = Channel<ContactsUiEvents>(capacity = BUFFERED)
    val state: Flow<ContactsUiEvents> = uiEvents.receiveAsFlow()

    init {
        observeContacts()
        observeContactsPermission()
    }

    private fun observeContacts() {
        viewModelScope.launch {
            repository.allContacts
                .catch { setNextState(ContactsUiEvents.Failure(it.message ?: "Error occurred")) }
                .collect { contacts -> setNextState(ContactsUiEvents.ContactsAvailable(contacts)) }
        }
    }

    private fun observeContactsPermission() {
        viewModelScope.launch {
            permissionFlow.getPermissionState(Manifest.permission.READ_CONTACTS).collect { state ->
                if (state.isGranted) {
                    setNextState(ContactsUiEvents.ContactPermissionGranted)
                } else {
                    setNextState(ContactsUiEvents.ContactPermissionNotGranted)
                }
            }
        }
    }

    private fun setNextState(nextState: ContactsUiEvents) {
        uiEvents.trySend(nextState)
    }

    class FactoryProvider(private val contentResolver: ContentResolver) {
        fun get(): ViewModelProvider.Factory {
            val initializer = ViewModelInitializer(ContactsViewModel::class.java) {
                ContactsViewModel(AndroidDefaultContactRepository(contentResolver))
            }
            return ViewModelProvider.Factory.from(initializer)
        }
    }
}

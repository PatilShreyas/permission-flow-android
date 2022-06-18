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

import dev.shreyaspatil.permissionFlow.example.data.model.Contact

sealed class ContactsUiState {
    object ContactPermissionNotGranted : ContactsUiState()
    object ContactPermissionGranted : ContactsUiState()
    data class ContactsAvailable(val contacts: List<Contact>) : ContactsUiState()
    data class Failure(val error: String) : ContactsUiState()
}

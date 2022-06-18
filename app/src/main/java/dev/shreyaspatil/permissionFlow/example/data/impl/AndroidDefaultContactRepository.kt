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
package dev.shreyaspatil.permissionFlow.example.data.impl

import android.Manifest
import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract
import androidx.core.database.getStringOrNull
import dev.shreyaspatil.permissionFlow.PermissionFlow
import dev.shreyaspatil.permissionFlow.example.data.ContactRepository
import dev.shreyaspatil.permissionFlow.example.data.model.Contact
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.withContext

class AndroidDefaultContactRepository(
    private val contentResolver: ContentResolver,
    private val permissionFlow: PermissionFlow = PermissionFlow.getInstance(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ContactRepository {

    override val allContacts: Flow<List<Contact>> = permissionFlow
        .get(Manifest.permission.READ_CONTACTS)
        .transform { isGranted ->
            if (isGranted) {
                emit(getContacts())
            } else {
                emit(emptyList())
            }
        }

    private suspend fun getContacts(): List<Contact> = withContext(ioDispatcher) {
        buildList {
            var cursor: Cursor? = null
            try {
                val projection = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_URI
                )

                val order = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} ASC"

                cursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection,
                    null,
                    null,
                    order
                )
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val contactId = cursor.getStringOrNull(
                            cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                        )
                        val name = cursor.getStringOrNull(
                            cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        )
                        val number = cursor.getStringOrNull(
                            cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        )

                        if (contactId != null && name != null && number != null) {
                            add(Contact(contactId, name, number))
                        }
                    }
                }
            } finally {
                cursor?.close()
            }
        }
    }
}

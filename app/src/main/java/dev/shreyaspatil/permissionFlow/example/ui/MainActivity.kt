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

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import dev.shreyaspatil.permissionFlow.example.databinding.ActivityMainBinding
import dev.shreyaspatil.permissionFlow.example.ui.composePermission.ComposePermissionActivity
import dev.shreyaspatil.permissionFlow.example.ui.contacts.ContactsActivity
import dev.shreyaspatil.permissionFlow.example.ui.fragment.ExampleFragmentActivity
import dev.shreyaspatil.permissionFlow.example.ui.multipermission.MultiPermissionActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            buttonContacts.setOnClickListener { launchScreen<ContactsActivity>() }
            buttonMultiPermission.setOnClickListener { launchScreen<MultiPermissionActivity>() }
            buttonComposeSample.setOnClickListener { launchScreen<ComposePermissionActivity>() }
            buttonFragmentSample.setOnClickListener { launchScreen<ExampleFragmentActivity>() }
        }
    }

    private inline fun <reified S : ComponentActivity> launchScreen() {
        startActivity(Intent(this, S::class.java))
    }
}

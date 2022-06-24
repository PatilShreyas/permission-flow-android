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
package dev.shreyaspatil.permissionFlow.example.ui.composePermission

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.shreyaspatil.permissionflow.compose.rememberMultiplePermissionState
import dev.shreyaspatil.permissionflow.compose.rememberPermissionFlowRequestLauncher

/**
 * The example activity which demonstrates the usage of PermissionFlow APIs in the Jetpack Compose
 */
class ComposePermissionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MainScreen() }
    }
}

private val permissions = arrayOf(
    android.Manifest.permission.CAMERA,
    android.Manifest.permission.READ_EXTERNAL_STORAGE,
    android.Manifest.permission.READ_CALL_LOG,
    android.Manifest.permission.READ_CONTACTS,
    android.Manifest.permission.READ_PHONE_STATE
)

@Composable
fun MainScreen() {
    val permissionLauncher = rememberPermissionFlowRequestLauncher()
    val state by rememberMultiplePermissionState(*permissions)
    // or use `rememberPermissionState()` to get the state of a single permission

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { permissionLauncher.launch(permissions) }) {
            Text("Request Permissions")
        }

        Column(modifier = Modifier.background(Color.Green).padding(16.dp)) {
            Text(text = "Granted Permissions:")
            Text(text = state.grantedPermissions.joinToString())
        }

        Column(modifier = Modifier.background(Color.Red).padding(16.dp)) {
            Text(text = "Denied Permissions:")
            Text(text = state.deniedPermissions.joinToString())
        }
    }
}

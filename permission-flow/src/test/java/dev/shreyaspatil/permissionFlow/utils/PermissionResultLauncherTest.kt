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
package dev.shreyaspatil.permissionFlow.utils

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment
import dev.shreyaspatil.permissionFlow.contract.RequestPermissionsContract
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class PermissionResultLauncherTest {

    @Test
    fun test_Activity_registerForPermissionFlowRequestsResult_default() {
        val activityProvidedResultRegistry = mockk<ActivityResultRegistry>()
        val activity =
            mockk<ComponentActivity> {
                every {
                    registerForActivityResult(
                        any<ActivityResultContract<Array<String>, Map<String, Boolean>>>(),
                        any(),
                        any<ActivityResultCallback<Map<String, Boolean>>>(),
                    )
                } returns mockk<ActivityResultLauncher<Array<String>>>()

                every { activityResultRegistry } returns activityProvidedResultRegistry
            }

        activity.registerForPermissionFlowRequestsResult(mockk())

        verify(exactly = 1) {
            activity.registerForActivityResult(
                any<RequestPermissionsContract>(), activityProvidedResultRegistry, any())
        }
    }

    @Test
    fun test_Activity_registerForPermissionFlowRequestsResult_withProvidedActivityResultRegistry() {
        val activityResultRegistry = mockk<ActivityResultRegistry>()
        val activity =
            mockk<ComponentActivity> {
                every {
                    registerForActivityResult(
                        any<ActivityResultContract<Array<String>, Map<String, Boolean>>>(),
                        any(),
                        any<ActivityResultCallback<Map<String, Boolean>>>(),
                    )
                } returns mockk<ActivityResultLauncher<Array<String>>>()
            }

        activity.registerForPermissionFlowRequestsResult(
            mockk(), activityResultRegistry = activityResultRegistry)

        verify(exactly = 1) {
            activity.registerForActivityResult(
                any<RequestPermissionsContract>(), activityResultRegistry, any())
        }
    }

    @Test
    fun test_Fragment_registerForPermissionFlowRequestsResult() {
        val fragment =
            mockk<Fragment> {
                every {
                    registerForActivityResult(
                        any<ActivityResultContract<Array<String>, Map<String, Boolean>>>(),
                        any(),
                        any<ActivityResultCallback<Map<String, Boolean>>>(),
                    )
                } returns mockk<ActivityResultLauncher<Array<String>>>()
            }

        fragment.registerForPermissionFlowRequestsResult(mockk(), mockk(), mockk())

        verify(exactly = 1) {
            fragment.registerForActivityResult(any<RequestPermissionsContract>(), any(), any())
        }
    }

    @Test
    fun test_Fragment_registerForPermissionFlowRequestsResult_withoutActivityRegistry() {
        val fragment =
            mockk<Fragment> {
                every {
                    registerForActivityResult(
                        any<ActivityResultContract<Array<String>, Map<String, Boolean>>>(),
                        any<ActivityResultCallback<Map<String, Boolean>>>(),
                    )
                } returns mockk<ActivityResultLauncher<Array<String>>>()
            }

        val requestPermissionsContract = mockk<RequestPermissionsContract>()
        val callback = mockk<ActivityResultCallback<Map<String, Boolean>>>()

        fragment.registerForPermissionFlowRequestsResult(
            requestPermissionsContract = requestPermissionsContract,
            callback = callback,
        )

        verify(exactly = 1) {
            fragment.registerForActivityResult(requestPermissionsContract, callback)
        }
    }
}

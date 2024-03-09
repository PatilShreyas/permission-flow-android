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
package dev.shreyaspatil.permissionFlow

import android.app.Application
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PermissionFlowTest {
    @Test
    fun testGetInstanceWithoutInit_shouldThrowException() {
        val result = runCatching { PermissionFlow.getInstance() }

        assertTrue(result.isFailure)

        val expectedErrorMessage =
            "Failed to create instance of PermissionFlow. Did you forget to call `PermissionFlow.init(context)`?"
        assertEquals(expectedErrorMessage, result.exceptionOrNull()!!.message)
    }

    @Test
    fun testGetInstanceWithInit_shouldBeSingleInstanceAlways() {
        // Init for the first time
        initPermissionFlow()
        // Get instance 1
        val instance1 = PermissionFlow.getInstance()

        // Init for the second time
        initPermissionFlow()
        // Get instance 2
        val instance2 = PermissionFlow.getInstance()

        // Both instances should be the same
        assert(instance1 === instance2)
    }

    private fun initPermissionFlow() {
        PermissionFlow.init(
            mockk { every { applicationContext } returns mockk<Application>() },
        )
    }
}

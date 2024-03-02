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
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
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
    fun testGetInstanceWithInit_shouldBeSingleInstanceAlways() = runTest {
        PermissionFlow.init(
            mockk { every { applicationContext } returns mockk<Application>() },
        )

        val instance1 = async { PermissionFlow.getInstance() }
        val instance2 = async { PermissionFlow.getInstance() }

        assert(instance1.await() === instance2.await())
    }
}

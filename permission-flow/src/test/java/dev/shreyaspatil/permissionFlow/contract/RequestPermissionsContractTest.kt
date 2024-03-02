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
package dev.shreyaspatil.permissionFlow.contract

import android.content.Context
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import dev.shreyaspatil.permissionFlow.PermissionFlow
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RequestPermissionsContractTest {
    private lateinit var requestMultiplePermissions: RequestMultiplePermissions
    private lateinit var permissionFlow: PermissionFlow
    private lateinit var context: Context

    private lateinit var contract: RequestPermissionsContract

    @Before
    fun setUp() {
        requestMultiplePermissions = mockk()
        permissionFlow = mockk(relaxUnitFun = true)
        context = mockk()

        contract = RequestPermissionsContract(requestMultiplePermissions, permissionFlow)
    }

    @Test
    fun testCreateIntent() {
        // Given: List of permissions
        val permissions = arrayOf("A", "B", "C", "D")
        every { requestMultiplePermissions.createIntent(any(), any()) } returns mockk()

        // When: Intent is created with permissions
        contract.createIntent(context, permissions)

        // Then: The context and permissions should be delegated to original contract
        verify(exactly = 1) { requestMultiplePermissions.createIntent(context, permissions) }
    }

    @Test
    fun testParseResult() {
        // Given: Result to be parsed
        val expectedResult = mapOf("A" to true, "B" to false, "C" to true)
        every { requestMultiplePermissions.parseResult(any(), any()) } returns expectedResult

        // When: The result is parsed
        val actualResult = contract.parseResult(0, null)

        // Then: Correct result should be returned
        assertEquals(expectedResult, actualResult)

        // Then: PermissionFlow should be notified with updated permissions
        verify(exactly = 1) { permissionFlow.notifyPermissionsChanged("A", "B", "C") }
    }
}

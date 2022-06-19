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
package dev.shreyaspatil.permissionFlow.impl

import dev.shreyaspatil.permissionFlow.watchmen.PermissionWatchmen
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PermissionFlowImplTest {

    private lateinit var watchmen: PermissionWatchmen
    private lateinit var permissionFlow: PermissionFlowImpl

    @Before
    fun setUp() {
        watchmen = mockk(relaxUnitFun = true)
        permissionFlow = PermissionFlowImpl(watchmen)
    }

    @Test
    fun testGetPermission() {
        // Given: Permission flow
        val expectedFlow = MutableStateFlow(true)
        every { watchmen.watch("A") } returns expectedFlow

        // When: Flow for any permission is retrieved
        val actualFlow = permissionFlow["A"]

        // Then: Correct flow should be returned
        assertEquals(expectedFlow, actualFlow)
    }

    @Test
    fun testNotifyPermissionsChanged() {
        // Given: Permissions whose state to be notified
        val permissions = arrayOf("A", "B", "C")

        // When: Permission state changes are notified
        permissionFlow.notifyPermissionsChanged(*permissions)

        // Then: Watchmen should be get notified about permission changes
        verify(exactly = 1) { watchmen.notifyPermissionsChanged(permissions) }
    }

    @Test
    fun testStartListening() {
        // When: Starts listening
        permissionFlow.startListening()

        // Then: Watchmen should wake up
        verify(exactly = 1) { watchmen.wakeUp() }
    }

    @Test
    fun testStopListening() {
        // When: Stops listening
        permissionFlow.stopListening()

        // Then: Watchmen should sleep
        verify(exactly = 1) { watchmen.sleep() }
    }
}

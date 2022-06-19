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
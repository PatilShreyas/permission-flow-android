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
package dev.shreyaspatil.permissionFlow.watchmen

import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dev.shreyaspatil.permissionFlow.utils.activityForegroundEventFlow
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@Suppress("OPT_IN_IS_NOT_ENABLED")
@OptIn(ExperimentalCoroutinesApi::class)
class PermissionWatchmenTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var application: Application

    private lateinit var watchmen: PermissionWatchmen

    @Before
    fun setUp() {
        application = mockk(relaxUnitFun = true)
        watchmen = PermissionWatchmen(application, dispatcher)
    }

    @Test
    fun shouldWakeUpAndReturnFlow_whenWatchPermissionForTheFirstTime() {
        // Given: Permission
        val permission = "permission"
        mockPermissions(permission to true)

        // When: Starts watching permission for the first time.
        val flow = watchmen.watch(permission)

        // Then: StateFlow should be returned with valid value i.e. true (Granted).
        assertTrue(flow.value.isGranted)

        // Then: Should start watching activity foreground events.
        dispatcher.scheduler.runCurrent()
        verify(exactly = 1) { application.registerActivityLifecycleCallbacks(any()) }
    }

    @Test
    fun shouldWakeUpAndReturnFlow_whenWatchMultiplePermissionForTheFirstTime() {
        // Given: Multiple Permission
        val permission1 = "permission-1"
        val permission2 = "permission-2"
        mockPermissions(permission1 to true, permission2 to false)

        // When: Starts watching multiple permission for the first time.
        val flow = watchmen.watchMultiple(arrayOf(permission1, permission2))

        // Then: StateFlow should be returned with valid value i.e. true (Granted).
        assertTrue(flow.value.permissions[0].isGranted)
        assertFalse(flow.value.permissions[1].isGranted)

        // Then: Should start watching activity foreground events.
        dispatcher.scheduler.runCurrent()
        verify(exactly = 1) { application.registerActivityLifecycleCallbacks(any()) }
    }

    @Test
    fun shouldReturnFilteredMultiplePermissionState_whenDuplicatePermissionsAreWatched() {
        // Given: Multiple Permission
        val permission1 = "permission-1"
        val permission2 = "permission-2"
        mockPermissions(permission1 to true, permission2 to false)

        // When: Starts watching multiple permission having list of repeated permissions
        val flow = watchmen.watchMultiple(arrayOf(permission1, permission1, permission2))

        // Then: State should only contain list having two items
        assertEquals(flow.value.permissions.size, 2)
        assertEquals(flow.value.permissions.map { it.permission }, listOf(permission1, permission2))
    }

    @Test
    fun shouldReturnSameInstance_whenWatchingPermissionMoreThanOnce() {
        // Given: A permission to be observed
        val permission = "permission"
        mockPermissions(permission to true)

        // When: A permission state is watched more than once
        val flow1 = watchmen.watch(permission)
        val flow2 = watchmen.watch(permission)

        // Then: Same instance should be returned
        assert(flow1 === flow2)
    }

    @Test
    fun shouldUpdateFlowState_whenPermissionChangesAreNotified() {
        // Given: Watching a permission flow
        val permission = "permission"
        mockPermissions(permission to true)
        val flow = watchmen.watch(permission)

        // When: Change in state is notified for the same permission
        mockPermissions(permission to false)
        watchmen.notifyPermissionsChanged(permissions = arrayOf(permission))

        // Then: Current value of flow should be false i.e. Not granted
        dispatcher.scheduler.runCurrent()
        assertFalse(flow.value.isGranted)
    }

    @Test
    fun shouldUpdateMultiplePermissionFlowState_whenPermissionChangesAreNotified() {
        // Given: Watching multiple permission state
        val permission1 = "permission-1"
        val permission2 = "permission-2"
        mockPermissions(permission1 to true, permission2 to false)

        val flow = watchmen.watchMultiple(arrayOf(permission1, permission2))

        // When: Change in state is notified for the these permission
        mockPermissions(permission2 to true)
        watchmen.notifyPermissionsChanged(permissions = arrayOf(permission2))

        // Then: All permissions should be granted
        dispatcher.scheduler.runCurrent()
        assertTrue(flow.value.allGranted)
    }

    @Test
    fun shouldUpdatePermissionFlowState_whenWatchmenWakesAfterSleeping() {
        // Given: Watching a permission
        val permission = "permission"
        mockPermissions(permission to true)
        val flow = watchmen.watch(permission)

        // When: Watchmen sleeps, permission state changes and watchmen wakes after that
        watchmen.sleep()
        mockPermissions(permission to false)
        watchmen.wakeUp()

        // Then: Permission state should be get updated
        dispatcher.scheduler.advanceUntilIdle()
        assertFalse(flow.value.isGranted)
    }

    @Test
    fun shouldUpdateMultiplePermissionFlowState_whenWatchmenWakesAfterSleeping() {
        // Given: Watching multiple permissions
        val permission1 = "permission-1"
        val permission2 = "permission-2"
        mockPermissions(permission1 to true, permission2 to false)
        val flow = watchmen.watchMultiple(arrayOf(permission1, permission2))

        // When: Watchmen sleeps, permission state changes and watchmen wakes after that
        watchmen.sleep()
        mockPermissions(permission1 to true, permission2 to true)
        watchmen.wakeUp()

        // Then: Permission state should be get updated
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(flow.value.permissions[1].isGranted)
    }

    @Test
    fun shouldNotUpdateFlowState_whenPermissionChangesAreNotifiedAndWatchmenIsSleeping() {
        // Given: Watching a permission flow and watchmen is sleeping
        val permission = "permission"
        mockPermissions(permission to true)
        val flow = watchmen.watch(permission)
        watchmen.sleep()

        // When: Change in state is notified for the same permission
        mockPermissions(permission to false)
        watchmen.notifyPermissionsChanged(permissions = arrayOf(permission))

        // Then: Current value of flow should not be changed i.e. it should remain as Granted
        dispatcher.scheduler.runCurrent()
        assertTrue(flow.value.isGranted)
    }

    @Test
    fun shouldNotUpdateMultipleFlowState_whenPermissionChangesAreNotifiedAndWatchmenIsSleeping() {
        // Given: Watching a permission flow and watchmen is sleeping
        val permission1 = "permission-1"
        val permission2 = "permission-2"
        mockPermissions(permission1 to true, permission2 to false)
        val flow = watchmen.watchMultiple(arrayOf(permission1, permission2))
        watchmen.sleep()

        // When: Change in state is notified for the same permission
        mockPermissions(permission2 to true)
        watchmen.notifyPermissionsChanged(permissions = arrayOf(permission2))

        // Then: Current value of flow should not be changed i.e. it should remain as Granted
        dispatcher.scheduler.runCurrent()
        assertFalse(flow.value.permissions[1].isGranted)
    }

    @Test
    fun shouldStartObservingActivity_whenWakingUp() {
        // When: Request watchmen to wake-up
        watchmen.wakeUp()

        // Then: Should start watching activity foreground events
        dispatcher.scheduler.runCurrent()
        verify(exactly = 1) { application.registerActivityLifecycleCallbacks(any()) }
    }

    @Test
    fun shouldStartObservingActivityOnceOnce_whenWakingUpMultipleTimes() {
        // When: Request watchmen to wake-up twice
        watchmen.wakeUp()
        dispatcher.scheduler.runCurrent()

        watchmen.wakeUp()
        dispatcher.scheduler.runCurrent()

        // Then: Should start watching activity foreground events only once
        verify(exactly = 1) { application.registerActivityLifecycleCallbacks(any()) }
    }

    @Test
    fun shouldStopObservingActivityEvent_whenSleeping() {
        // When: Requests watchmen to sleep after being waking up
        watchmen.wakeUp()
        dispatcher.scheduler.runCurrent()
        watchmen.sleep()

        // Then: Should stop watching activity foreground events
        dispatcher.scheduler.runCurrent()
        verify(exactly = 1) { application.unregisterActivityLifecycleCallbacks(any()) }
    }

    @Test
    fun shouldNotifyAllPermissionChanges_whenActivityForegroundEventIsReceived() = runTest {
        // Given: A activity foreground event flow

        // Here we are using semaphore to release fake activity foreground event.
        val activityEventLock = Semaphore(permits = 1)
        // We are acquiring lock here, so that we can hold flow emission till this lock is unlocked.
        activityEventLock.acquire()

        mockActivityForegroundEventFlow(
            flow {
                activityEventLock.acquire()
                emit(Unit)
            },
        )

        // Given: Watching permissions
        mockPermissions("permission-1" to false, "permission-2" to false, "permission-3" to false)
        val permissionFlow1 = watchmen.watch("permission-1")
        val permissionFlow2 = watchmen.watch("permission-2")
        val permissionFlow3 = watchmen.watch("permission-3")

        // When: Permission state is changed and activity foreground event is received
        mockPermissions("permission-1" to true, "permission-2" to true, "permission-3" to true)
        // Release event lock so that flow can emit event
        activityEventLock.release()

        // Then: Permission state for all active flows should be get updated after debounce time.
        advanceTimeBy(6_000L) // Debounce time is 5 seconds, so 5 + 1 = 6 seconds
        assertTrue(permissionFlow1.value.isGranted)
        assertTrue(permissionFlow2.value.isGranted)
        assertTrue(permissionFlow3.value.isGranted)
    }

    /**
     * Mocks permission state i.e. granted / denied.
     */
    private fun mockPermissions(vararg permissionStates: Pair<String, Boolean>) {
        mockkStatic(ContextCompat::checkSelfPermission)
        permissionStates.forEach { (permission, isGranted) ->
            every { ContextCompat.checkSelfPermission(any(), permission) } returns
                if (isGranted) {
                    PackageManager.PERMISSION_GRANTED
                } else {
                    PackageManager.PERMISSION_DENIED
                }
        }
    }

    /**
     * Mocks application's [activityForegroundEventFlow] with the specified [flow]
     */
    private fun mockActivityForegroundEventFlow(flow: Flow<Unit>) {
        mockkStatic(Application::activityForegroundEventFlow)
        every { application.activityForegroundEventFlow } returns flow
    }

    private fun runTest(testBody: suspend TestScope.() -> Unit) =
        runTest(
            context = dispatcher,
            testBody = testBody,
        )
}

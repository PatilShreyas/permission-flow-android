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
package dev.shreyaspatil.permissionFlow.internal

import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import app.cash.turbine.test
import dev.shreyaspatil.permissionFlow.PermissionState
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ApplicationStateMonitorTest {

    private lateinit var monitor: ApplicationStateMonitor

    private lateinit var application: Application
    private val callbackSlot = slot<Application.ActivityLifecycleCallbacks>()
    private val lifecycleCallbacks
        get() = callbackSlot.captured

    @Before
    fun setUp() {
        application =
            mockk(relaxUnitFun = true) {
                every { registerActivityLifecycleCallbacks(capture(callbackSlot)) } just Runs
            }
        monitor = ApplicationStateMonitor(application)
    }

    @Test
    fun getPermissionState_returnGrantedPermissionState_andCurrentActivityNotPresent() {
        // Given: No current activity
        val permission = "A"
        mockPermissions(permission to true)
        val expectedPermissionState =
            PermissionState(permission = permission, isGranted = true, isRationaleRequired = null)

        // When: Permission state is retrieved
        val actualPermissionState = monitor.getPermissionState(permission)

        // Then: Correct permission state should be returned
        assertEquals(expectedPermissionState, actualPermissionState)
    }

    @Test
    fun getPermissionState_returnDeniedPermissionState_andCurrentActivityNotPresent() {
        // Given: No current activity
        val permission = "A"
        mockPermissions(permission to false)
        val expectedPermissionState =
            PermissionState(permission = permission, isGranted = false, isRationaleRequired = null)

        // When: Permission state is retrieved
        val actualPermissionState = monitor.getPermissionState(permission)

        // Then: Correct permission state should be returned
        assertEquals(expectedPermissionState, actualPermissionState)
    }

    @Test
    fun getPermissionState_returnGrantedAndValidRationale_whenCurrentActivityIsPresent() = runTest {
        monitor.activityForegroundEvents.test {
            // Given: current activity
            val activity = activity()
            lifecycleCallbacks.onActivityCreated(activity, null)

            val permission = "A"
            mockPermissions(permission to true)
            mockPermissionRationale(permission to false)
            val expectedPermissionState =
                PermissionState(
                    permission = permission, isGranted = true, isRationaleRequired = false)

            // When: Permission state is retrieved
            val actualPermissionState = monitor.getPermissionState(permission)

            // Then: Correct permission state should be returned
            assertEquals(expectedPermissionState, actualPermissionState)
        }
    }

    @Test
    fun getPermissionState_returnDeniedAndValidRationale_whenCurrentActivityIsPresent() = runTest {
        monitor.activityForegroundEvents.test {
            // Given: current activity
            val activity = activity()
            lifecycleCallbacks.onActivityCreated(activity, null)

            val permission = "A"
            mockPermissions(permission to false)
            mockPermissionRationale(permission to true)
            val expectedPermissionState =
                PermissionState(
                    permission = permission, isGranted = false, isRationaleRequired = true)

            // When: Permission state is retrieved
            val actualPermissionState = monitor.getPermissionState(permission)

            // Then: Correct permission state should be returned
            assertEquals(expectedPermissionState, actualPermissionState)
        }
    }

    @Test
    fun getPermissionDeniedState_returnValidRationale_whenCurrentActivityIsPresent() = runTest {
        monitor.activityForegroundEvents.test {
            // Given: current activity
            val activity = activity()
            lifecycleCallbacks.onActivityCreated(activity, null)

            val permission = "A"
            mockPermissions(permission to false)
            mockPermissionRationale(permission to true)
            val expectedPermissionState =
                PermissionState(
                    permission = permission, isGranted = false, isRationaleRequired = true)

            // When: Permission state is retrieved
            val actualPermissionState = monitor.getPermissionState(permission)

            // Then: Correct permission state should be returned
            assertEquals(expectedPermissionState, actualPermissionState)
        }
    }

    @Test
    fun testRegisterUnregisterCallback() = runTest {
        monitor.activityForegroundEvents.test {
            verify(exactly = 1) {
                application.registerActivityLifecycleCallbacks(lifecycleCallbacks)
            }
            cancelAndIgnoreRemainingEvents()
        }
        verify(exactly = 1) { application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks) }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun shouldSetCurrentActivity_whenActivityIsPreCreated() = runTest {
        monitor.activityForegroundEvents.test {
            // Given: Activity not created
            assertNull(monitor.getCurrentActivityReference())

            // When: Activity is created
            val activity = activity()
            lifecycleCallbacks.onActivityPreCreated(activity, null)

            // Then: Activity should be set
            assertEquals(activity, monitor.getCurrentActivityReference()?.get())
        }
    }

    @Test
    fun shouldSetCurrentActivity_whenActivityIsCreated() = runTest {
        monitor.activityForegroundEvents.test {
            // Given: Activity not created
            assertNull(monitor.getCurrentActivityReference())

            // When: Activity is created
            val activity = activity()
            lifecycleCallbacks.onActivityCreated(activity, null)

            // Then: Activity should be set
            assertEquals(activity, monitor.getCurrentActivityReference()?.get())
        }
    }

    @Test
    fun shouldSetCurrentActivity_whenAnotherActivityIsCreated() = runTest {
        monitor.activityForegroundEvents.test {
            // Given: Activity not created
            assertNull(monitor.getCurrentActivityReference())

            // When: Activity is created
            val activity1 = activity()
            val activity2 = activity()
            lifecycleCallbacks.onActivityCreated(activity1, null)
            lifecycleCallbacks.onActivityCreated(activity2, null)

            // Then: Latest created activity should be set as current
            assertEquals(activity2, monitor.getCurrentActivityReference()?.get())
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun shouldSetCurrentActivity_whenAnotherActivityIsCreatedInApi29() = runTest {
        monitor.activityForegroundEvents.test {
            // Given: Activity not created
            assertNull(monitor.getCurrentActivityReference())

            // When: Activity is created
            val activity = activity()
            lifecycleCallbacks.onActivityPreCreated(activity, null)
            val previousRef = monitor.getCurrentActivityReference()
            lifecycleCallbacks.onActivityCreated(activity, null)
            val afterRef = monitor.getCurrentActivityReference()

            // Then: Reference should be same
            assertEquals(previousRef, afterRef)
        }
    }

    @Test
    fun shouldClearCurrentActivity_whenActivityIsDestroyed() = runTest {
        monitor.activityForegroundEvents.test {
            // Given: Activity created
            val activity = activity()
            lifecycleCallbacks.onActivityCreated(activity, null)

            // When: Activity is destroyed
            lifecycleCallbacks.onActivityDestroyed(activity)

            // Then: Activity should be cleared
            assertNull(monitor.getCurrentActivityReference()?.get())
        }
    }

    @Test
    fun shouldNotClearCurrentActivity_whenAnotherActivityIsDestroyed() = runTest {
        monitor.activityForegroundEvents.test {
            // Given: Activity created
            val activity1 = activity()
            val activity2 = activity()

            // When: One activity is created and other is destroyed
            lifecycleCallbacks.onActivityCreated(activity1, null)
            lifecycleCallbacks.onActivityDestroyed(activity2)

            // Then: Activity1 should be present
            assertEquals(activity1, monitor.getCurrentActivityReference()?.get())
        }
    }

    @Test
    fun shouldNotEmitEvent_whenActivityIsStartedWithConfigChanges() = runTest {
        monitor.activityForegroundEvents.test {
            // Before onStart(), onStop() should be called first
            lifecycleCallbacks.onActivityStopped(activity(isChangingConfigurations = true))
            lifecycleCallbacks.onActivityStarted(activity())

            // Event should not get emitted
            expectNoEvents()
        }
    }

    @Test
    fun shouldEmitEvent_whenActivityIsStarted_andNoConfigChanges() = runTest {
        monitor.activityForegroundEvents.test {
            // Before onStart(), onStop() should be called first
            lifecycleCallbacks.onActivityStopped(activity(isChangingConfigurations = false))
            lifecycleCallbacks.onActivityStarted(activity())

            // Event should get emitted
            awaitItem()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun shouldEmitEvent_whenActivityIsResumedAfterExitingFromInMultiWindowMode() = runTest {
        monitor.activityForegroundEvents.test {
            // Before onStart(), onStop() should be called first
            lifecycleCallbacks.onActivityPaused(activity(isInMultiWindowMode = true))
            lifecycleCallbacks.onActivityResumed(activity(isInMultiWindowMode = false))

            // Event should get emitted
            awaitItem()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun shouldNotEmitEvent_whenActivityIsResumedAfterPaused_onAndroidM() = runTest {
        monitor.activityForegroundEvents.test {
            // Before onStart(), onStop() should be called first
            lifecycleCallbacks.onActivityPaused(mockk())
            lifecycleCallbacks.onActivityResumed(mockk())

            // Event should get emitted
            expectNoEvents()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun shouldNotEmitEvent_whenActivityIsResumedButStillInMultiWindowMode() = runTest {
        monitor.activityForegroundEvents.test {
            // Before onStart(), onStop() should be called first
            lifecycleCallbacks.onActivityPaused(activity(isInMultiWindowMode = true))
            lifecycleCallbacks.onActivityResumed(activity(isInMultiWindowMode = true))

            // Event should not get emitted
            expectNoEvents()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun shouldEmitEvent_whenActivityIsResumedAfterExitingFromPiPMode() = runTest {
        monitor.activityForegroundEvents.test {
            // Before onStart(), onStop() should be called first
            lifecycleCallbacks.onActivityPaused(activity(isInPictureInPictureMode = true))
            lifecycleCallbacks.onActivityResumed(activity(isInPictureInPictureMode = false))

            // Event should get emitted
            awaitItem()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun shouldNotEmitEvent_whenActivityIsResumedButStillInPiPMode() = runTest {
        monitor.activityForegroundEvents.test {
            // Before onStart(), onStop() should be called first
            lifecycleCallbacks.onActivityPaused(activity(isInPictureInPictureMode = true))
            lifecycleCallbacks.onActivityResumed(activity(isInPictureInPictureMode = true))

            // Event should not get emitted
            expectNoEvents()
        }
    }

    /** Activity factory function */
    private fun activity(
        isChangingConfigurations: Boolean = false,
        isInMultiWindowMode: Boolean = false,
        isInPictureInPictureMode: Boolean = false,
    ): Activity = mockk {
        every { isChangingConfigurations() } returns isChangingConfigurations
        every { isInMultiWindowMode() } returns isInMultiWindowMode
        every { isInPictureInPictureMode() } returns isInPictureInPictureMode
    }

    /** Mocks permission state i.e. granted / denied. */
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

    /** Mocks permission rationale state i.e. should shown or not */
    private fun mockPermissionRationale(vararg permissionStates: Pair<String, Boolean>) {
        mockkStatic(ActivityCompat::shouldShowRequestPermissionRationale)
        permissionStates.forEach { (permission, shouldShow) ->
            every { ActivityCompat.shouldShowRequestPermissionRationale(any(), permission) } returns
                shouldShow
        }
    }
}

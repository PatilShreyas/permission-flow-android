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

import android.app.Activity
import android.app.Application
import app.cash.turbine.test
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@Suppress("OPT_IN_IS_NOT_ENABLED")
@OptIn(ExperimentalCoroutinesApi::class)
class ActivityLifecycleCallbackExtTest {

    private val callbackSlot = slot<Application.ActivityLifecycleCallbacks>()

    private val application: Application = mockk(relaxUnitFun = true) {
        every { registerActivityLifecycleCallbacks(capture(callbackSlot)) } just Runs
    }

    private val lifecycleCallbacks get() = callbackSlot.captured

    @Test
    fun testRegisterUnregisterCallback() = runTest {
        application.activityForegroundEventFlow.test {
            verify(exactly = 1) {
                application.registerActivityLifecycleCallbacks(lifecycleCallbacks)
            }
            cancelAndIgnoreRemainingEvents()
        }
        verify(exactly = 1) {
            application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
        }
    }

    @Test
    fun shouldNotEmitEvent_whenActivityIsStartedWithConfigChanges() = runTest {
        application.activityForegroundEventFlow.test {
            // Before onStart(), onStop() should be called first
            lifecycleCallbacks.onActivityStopped(activity(isChangingConfigurations = true))
            lifecycleCallbacks.onActivityStarted(activity())

            // Event should not get emitted
            expectNoEvents()
        }
    }

    @Test
    fun shouldEmitEvent_whenActivityIsStarted_andNoConfigChanges() = runTest {
        application.activityForegroundEventFlow.test {
            // Before onStart(), onStop() should be called first
            lifecycleCallbacks.onActivityStopped(activity(isChangingConfigurations = false))
            lifecycleCallbacks.onActivityStarted(activity())

            // Event should get emitted
            awaitItem()
        }
    }

    @Test
    fun shouldEmitEvent_whenActivityIsResumedAfterExitingFromInMultiWindowMode() = runTest {
        mockAndroidApiHigherThanExpected()
        application.activityForegroundEventFlow.test {
            // Before onStart(), onStop() should be called first
            lifecycleCallbacks.onActivityPaused(activity(isInMultiWindowMode = true))
            lifecycleCallbacks.onActivityResumed(activity(isInMultiWindowMode = false))

            // Event should get emitted
            awaitItem()
        }
    }

    @Test
    fun shouldNotEmitEvent_whenActivityIsResumedButStillInMultiWindowMode() = runTest {
        mockAndroidApiHigherThanExpected()
        application.activityForegroundEventFlow.test {
            // Before onStart(), onStop() should be called first
            lifecycleCallbacks.onActivityPaused(activity(isInMultiWindowMode = true))
            lifecycleCallbacks.onActivityResumed(activity(isInMultiWindowMode = true))

            // Event should not get emitted
            expectNoEvents()
        }
    }

    @Test
    fun shouldEmitEvent_whenActivityIsResumedAfterExitingFromPiPMode() = runTest {
        mockAndroidApiHigherThanExpected()
        application.activityForegroundEventFlow.test {
            // Before onStart(), onStop() should be called first
            lifecycleCallbacks.onActivityPaused(activity(isInPictureInPictureMode = true))
            lifecycleCallbacks.onActivityResumed(activity(isInPictureInPictureMode = false))

            // Event should get emitted
            awaitItem()
        }
    }

    @Test
    fun shouldNotEmitEvent_whenActivityIsResumedButStillInPiPMode() = runTest {
        mockAndroidApiHigherThanExpected()
        application.activityForegroundEventFlow.test {
            // Before onStart(), onStop() should be called first
            lifecycleCallbacks.onActivityPaused(activity(isInPictureInPictureMode = true))
            lifecycleCallbacks.onActivityResumed(activity(isInPictureInPictureMode = true))

            // Event should not get emitted
            expectNoEvents()
        }
    }

    /**
     * Activity factory function
     */
    private fun activity(
        isChangingConfigurations: Boolean = false,
        isInMultiWindowMode: Boolean = false,
        isInPictureInPictureMode: Boolean = false
    ): Activity = mockk {
        every { isChangingConfigurations() } returns isChangingConfigurations
        every { isInMultiWindowMode() } returns isInMultiWindowMode
        every { isInPictureInPictureMode() } returns isInPictureInPictureMode
    }

    /**
     * Some functionalities in the utility requires Android OS version above API 24.
     * This utility function mocks OS version higher than expected version
     */
    private fun mockAndroidApiHigherThanExpected() {
        mockkObject(ApiLevelChecker)
        val slot = slot<() -> Unit>()
        every { ApiLevelChecker.ifHigherThan(any(), capture(slot)) } answers {
            slot.captured.invoke()
        }
    }
}

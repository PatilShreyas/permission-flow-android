package dev.shreyaspatil.permissionFlow.utils

import android.app.Activity
import android.app.Application
import android.os.Build
import app.cash.turbine.test
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.lang.reflect.Field
import java.lang.reflect.Modifier
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
        mockAndroidApiVersion(25)
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
        mockAndroidApiVersion(25)
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
        mockAndroidApiVersion(25)
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
        mockAndroidApiVersion(25)
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
     * This utility function reflectively mocks OS version as specified [apiVersion]
     */
    private fun mockAndroidApiVersion(apiVersion: Int) {
        Build.VERSION::class.java.getDeclaredField("SDK_INT").apply {
            isAccessible = true

            val modifiersField =
                Field::class.java.getDeclaredField("modifiers").apply { isAccessible = true }
            modifiersField.isAccessible = true
            modifiersField.setInt(this, modifiers and Modifier.FINAL.inv())

            set(null, apiVersion)
        }
    }
}
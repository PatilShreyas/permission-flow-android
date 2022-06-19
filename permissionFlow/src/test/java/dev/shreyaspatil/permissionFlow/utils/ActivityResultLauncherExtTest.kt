package dev.shreyaspatil.permissionFlow.utils

import androidx.activity.result.ActivityResultLauncher
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class ActivityResultLauncherExtTest {

    @Test
    fun testLaunch() {
        val activityResultLauncher: ActivityResultLauncher<Array<String>> = mockk(
            relaxUnitFun = true
        )

        activityResultLauncher.launch("A", "B", "C")

        verify { activityResultLauncher.launch(arrayOf("A", "B", "C")) }
    }
}
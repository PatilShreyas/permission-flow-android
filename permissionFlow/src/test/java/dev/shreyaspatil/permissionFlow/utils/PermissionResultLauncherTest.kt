package dev.shreyaspatil.permissionFlow.utils

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment
import dev.shreyaspatil.permissionFlow.contract.RequestPermissionsContract
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class PermissionResultLauncherTest {
    @Test
    fun test_Activity_registerForPermissionFlowRequestsResult() {
        val activity = mockk<ComponentActivity> {
            every {
                registerForActivityResult(
                    any<ActivityResultContract<Array<String>, Map<String, Boolean>>>(),
                    any(),
                    any<ActivityResultCallback<Map<String, Boolean>>>()
                )
            } returns mockk<ActivityResultLauncher<Array<String>>>()
        }

        activity.registerForPermissionFlowRequestsResult(mockk(), mockk())

        verify(exactly = 1) {
            activity.registerForActivityResult(any<RequestPermissionsContract>(), any(), any())
        }
    }

    @Test
    fun test_Fragment_registerForPermissionFlowRequestsResult() {
        val fragment = mockk<Fragment> {
            every {
                registerForActivityResult(
                    any<ActivityResultContract<Array<String>, Map<String, Boolean>>>(),
                    any(),
                    any<ActivityResultCallback<Map<String, Boolean>>>()
                )
            } returns mockk<ActivityResultLauncher<Array<String>>>()
        }

        fragment.registerForPermissionFlowRequestsResult(mockk(), mockk())

        verify(exactly = 1) {
            fragment.registerForActivityResult(any<RequestPermissionsContract>(), any(), any())
        }
    }
}
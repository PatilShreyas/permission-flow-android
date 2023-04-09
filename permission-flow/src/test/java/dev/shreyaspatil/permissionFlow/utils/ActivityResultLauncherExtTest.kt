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

import androidx.activity.result.ActivityResultLauncher
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class ActivityResultLauncherExtTest {

    @Test
    fun testLaunch() {
        val activityResultLauncher: ActivityResultLauncher<Array<String>> = mockk(
            relaxUnitFun = true,
        )

        activityResultLauncher.launch("A", "B", "C")

        verify { activityResultLauncher.launch(arrayOf("A", "B", "C")) }
    }
}

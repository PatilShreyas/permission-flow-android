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
package dev.shreyaspatil.permissionFlow.initializer

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import dev.shreyaspatil.permissionFlow.PermissionFlow
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verifySequence
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class PermissionFlowInitializerTest {
    private lateinit var initializer: PermissionFlowInitializer
    private lateinit var permissionFlow: PermissionFlow

    @Before
    fun setUp() {
        permissionFlow = mockk(relaxUnitFun = true)
        mockkObject(PermissionFlow)
        every { PermissionFlow.getInstance() } returns permissionFlow

        initializer = PermissionFlowInitializer()
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun testInitializer() {
        // Given: A application context providing context
        val context = ApplicationProvider.getApplicationContext<Context>()

        // When: Initializer is created
        initializer.create(context = context)

        // Then: Permission flow should be initialized
        verifySequence {
            PermissionFlow.init(context)
            PermissionFlow.getInstance()
            permissionFlow.startListening()
        }
    }

    @Test
    fun testInitializerDependencies_shouldBeEmpty() {
        Assert.assertTrue(initializer.dependencies().isEmpty())
    }
}

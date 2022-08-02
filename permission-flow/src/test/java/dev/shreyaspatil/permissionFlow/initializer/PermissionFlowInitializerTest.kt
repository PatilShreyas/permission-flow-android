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

import android.app.Application
import android.content.Context
import dev.shreyaspatil.permissionFlow.impl.PermissionFlowImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.Assert
import org.junit.Test

class PermissionFlowInitializerTest {

    @Test
    fun testInitializer() {
        val initializer = PermissionFlowInitializer()
        mockkObject(PermissionFlowImpl)
        val context = mockk<Context> {
            every { applicationContext } returns mockk<Application>()
        }

        initializer.create(context = context)

        verify(exactly = 1) { PermissionFlowImpl.init(context, any()) }
    }

    @Test
    fun testInitializerDependencies_shouldBeEmpty() {
        val initializer = PermissionFlowInitializer()

        Assert.assertTrue(initializer.dependencies().isEmpty())
    }
}

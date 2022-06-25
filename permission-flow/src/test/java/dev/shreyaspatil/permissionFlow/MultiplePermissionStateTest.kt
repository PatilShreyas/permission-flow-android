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
package dev.shreyaspatil.permissionFlow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MultiplePermissionStateTest {

    @Test
    fun allGranted_shouldReturnTrue_whenAllPermissionsAreGranted() {
        // When: Multiple permission state having all permissions granted
        val permissions = multiplePermissionState(
            grantedPermission("A"),
            grantedPermission("B"),
            grantedPermission("C")
        )

        // Then: All permissions should be granted
        assertTrue(permissions.allGranted)
    }

    @Test
    fun allGranted_shouldReturnFalse_whenAllPermissionsAreNotGranted() {
        // When: Multiple permission state having some permissions as not granted
        val permissions = multiplePermissionState(
            grantedPermission("A"),
            deniedPermission("B"),
            grantedPermission("C")
        )

        // Then: All permissions should NOT be granted
        assertFalse(permissions.allGranted)
    }

    @Test
    fun grantedPermissions_shouldReturnListOfGrantedPermissions() {
        // When: Multiple permission state
        val permissions = multiplePermissionState(
            grantedPermission("A"),
            deniedPermission("B"),
            grantedPermission("C"),
            deniedPermission("D")
        )

        // Then: Permissions A and B should be present in granted permissions list
        assertEquals(permissions.grantedPermissions, listOf("A", "C"))
    }

    @Test
    fun deniedPermissions_shouldReturnListOfDeniedPermissions() {
        // When: Multiple permission state
        val permissions = multiplePermissionState(
            grantedPermission("A"),
            deniedPermission("B"),
            grantedPermission("C"),
            deniedPermission("D")
        )

        // Then: Permissions A and B should be present in granted permissions list
        assertEquals(permissions.deniedPermissions, listOf("B", "D"))
    }

    private fun multiplePermissionState(vararg permissionState: PermissionState) =
        MultiplePermissionState(permissionState.toList())

    private fun grantedPermission(permission: String) = PermissionState(
        permission = permission,
        isGranted = true
    )

    private fun deniedPermission(permission: String) = PermissionState(
        permission = permission,
        isGranted = false
    )
}

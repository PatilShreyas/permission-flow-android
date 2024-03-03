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

import android.content.Context
import dev.shreyaspatil.permissionFlow.PermissionFlow.Companion.getInstance
import dev.shreyaspatil.permissionFlow.PermissionFlow.Companion.init
import dev.shreyaspatil.permissionFlow.impl.PermissionFlowImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.newFixedThreadPoolContext

/**
 * A utility class which provides a functionality for observing state of a permission (whether it's
 * granted or not) with reactive coroutine stream i.e. [StateFlow].
 *
 * This takes care of listening to permission state change from any screen throughout the
 * application so that you can listen to permission in any layer of architecture within app.
 *
 * To retrieve the instance, use [getInstance] method but make sure to initialize it with [init]
 * method before retrieving instance. Otherwise, it'll throw [IllegalStateException]
 *
 * Example usage:
 *
 * **1. Initialization**
 *
 * ```
 *  class MyApplication: Application() {
 *      override fun onCreate() {
 *          super.onCreate()
 *          PermissionFlow.init(this)
 *      }
 *  }
 * ```
 *
 * **2. Observing permission**
 *
 * ```
 *  val permissionFlow = PermissionFlow.getInstance()
 *
 *  fun observeContactsPermission() {
 *      coroutineScope.launch {
 *          permissionFlow.getPermissionState(android.Manifest.permission.READ_CONTACTS)
 *              .collect { state ->
 *                  if (state.isGranted) {
 *                      // Do something
 *                  } else {
 *                      if (state.isRationaleRequired) {
 *                          // Do something
 *                      }
 *                  }
 *              }
 *      }
 *  }
 * ```
 *
 * **3. Launching permission**
 *
 * ```
 *  class MyActivity: AppCompatActivity() {
 *      private val permissionLauncher = registerForPermissionFlowRequestsResult()
 *
 *      fun askContactPermission() {
 *          permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
 *      }
 *  }
 * ```
 *
 * This utility tries to listen to permission state change which may not happen within a application
 * (_e.g. user trying to allow permission from app settings_), but doesn't guarantee that you'll
 * always get a updated state at the accurate instant.
 */
interface PermissionFlow {
    /**
     * Returns [StateFlow] for a given [permission]
     *
     * @param permission Unique permission identity (for e.g.
     *   [android.Manifest.permission.READ_CONTACTS])
     *
     * Example:
     * ```
     *  permissionFlow.getPermissionState(android.Manifest.permission.READ_CONTACTS)
     *      .collect { state ->
     *          if (state.isGranted) {
     *              // Do something
     *          } else {
     *              if (state.isRationaleRequired) {
     *                  // Do something
     *              }
     *          }
     *      }
     * ```
     */
    fun getPermissionState(permission: String): StateFlow<PermissionState>

    /**
     * Returns [Flow] for a given [permission] events.
     *
     * Flow will emit [PermissionState] whenever the state of permission is changed after collecting
     * this flow. Initial state of permission won't be emitted.
     *
     * @param permission Unique permission identity (for e.g.
     *   [android.Manifest.permission.READ_CONTACTS])
     *
     * Example:
     * ```
     *  permissionFlow.getPermissionEvent(android.Manifest.permission.READ_CONTACTS)
     *      .collect { state ->
     *          if (state.isGranted) {
     *              // Do something
     *          } else {
     *              if (state.isRationaleRequired) {
     *                  // Do something
     *              }
     *          }
     *      }
     * ```
     */
    fun getPermissionEvent(permission: String): Flow<PermissionState>

    /**
     * Returns [StateFlow] of a combining state for [permissions]
     *
     * @param permissions List of permissions (for e.g. [android.Manifest.permission.READ_CONTACTS],
     *   [android.Manifest.permission.READ_SMS])
     *
     * Example:
     * ```
     *  permissionFlow.getMultiplePermissionState(
     *      android.Manifest.permission.READ_CONTACTS,
     *      android.Manifest.permission.READ_SMS
     *  ).collect { state ->
     *      // All permission states
     *      val allPermissions = state.permissions
     *
     *      // Check whether all permissions are granted
     *      val allGranted = state.allGranted
     *
     *      // List of granted permissions
     *      val grantedPermissions = state.grantedPermissions
     *
     *      // List of denied permissions
     *      val deniedPermissions = state.deniedPermissions
     *  }
     * ```
     */
    fun getMultiplePermissionState(vararg permissions: String): StateFlow<MultiplePermissionState>

    /**
     * This helps to check if specified [permissions] are changed and it verifies it and updates the
     * state of permissions which are being observed via [getMultiplePermissionState] method.
     *
     * This can be useful when you are not using result launcher which is provided with this library
     * and manually handling permission request and want to update the state of permission in this
     * library so that flows which are being observed should get an updated state.
     *
     * If [stopListening] is called earlier and hasn't started listening again, notifying permission
     * doesn't work. Its new state is automatically calculated after starting listening to states
     * again by calling [startListening] method.
     *
     * Example usage:
     *
     * In this example, we are not using result launcher provided by this library. So we are
     * manually notifying library about state change of a permission.
     *
     * ```
     *  class MyActivity: AppCompatActivity() {
     *      private val permissionFlow = PermissionFlow.getInstance()
     *      private val permissionLauncher = registerForActivityResult(RequestPermission()) { isGranted ->
     *          permissionFlow.notifyPermissionsChanged(android.Manifest.permission.READ_CONTACTS)
     *      }
     *  }
     * ```
     *
     * @param permissions List of permissions
     */
    fun notifyPermissionsChanged(vararg permissions: String)

    /**
     * Starts listening the changes of state of permissions.
     *
     * Ideally it automatically starts listening lazily when [getMultiplePermissionState] method is
     * used for the first time. But this can be used to start to listen again after stopping
     * listening with [stopListening].
     */
    fun startListening()

    /**
     * Stops listening to the state changes of permissions throughout the application. This means
     * the state of permission retrieved with [getMultiplePermissionState] method will not be
     * updated after stopping listening. To start to listen again, use [startListening] method.
     */
    fun stopListening()

    /**
     * Companion of [PermissionFlow] to provide initialization of [PermissionFlow] as well as
     * getting instance.
     */
    companion object {
        @OptIn(DelicateCoroutinesApi::class)
        private val DEFAULT_DISPATCHER = newFixedThreadPoolContext(2, "PermissionFlow")

        /**
         * Initializes this [PermissionFlow] instance with specified arguments.
         *
         * @param context The Android's [Context]. Application context is recommended.
         * @param dispatcher Coroutine dispatcher to be used in the [PermissionFlow]. By default, it
         *   utilizes dispatcher having fixed two number of threads.
         */
        @JvmStatic
        @JvmOverloads
        fun init(context: Context, dispatcher: CoroutineDispatcher = DEFAULT_DISPATCHER) {
            PermissionFlowImpl.init(context, dispatcher)
        }

        /**
         * Returns an instance with default implementation of [PermissionFlow].
         *
         * @return Instance of [PermissionFlow].
         * @throws IllegalStateException If method [init] is not called before using this method.
         */
        @JvmStatic
        fun getInstance(): PermissionFlow =
            PermissionFlowImpl.instance
                ?: error(
                    "Failed to create instance of PermissionFlow. Did you forget to call `PermissionFlow.init(context)`?")
    }
}

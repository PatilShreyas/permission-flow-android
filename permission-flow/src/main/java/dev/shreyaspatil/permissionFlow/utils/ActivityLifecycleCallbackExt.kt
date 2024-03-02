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
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

/**
 * A flow which gives callback whenever any activity is started withing application (without
 * configuration change) or any activity is resumed after being in multi-window or
 * picture-in-picture mode.
 */
internal val Application.activityForegroundEventFlow
    get() = callbackFlow {
        val callback =
            object : Application.ActivityLifecycleCallbacks {
                private var isActivityChangingConfigurations: Boolean? = null
                private var wasInMultiWindowMode: Boolean? = null
                private var wasInPictureInPictureMode: Boolean? = null

                /**
                 * Whenever activity receives onStart() lifecycle callback, emit foreground event
                 * only when activity hasn't changed configurations.
                 */
                override fun onActivityStarted(activity: Activity) {
                    if (isActivityChangingConfigurations == false) {
                        trySend(Unit)
                    }
                }

                override fun onActivityStopped(activity: Activity) {
                    isActivityChangingConfigurations = activity.isChangingConfigurations
                }

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

                /**
                 * Whenever application is resized after being in in PiP or multi-window mode, or
                 * exits from these modes, onResumed() lifecycle callback is triggered.
                 *
                 * Here we assume that user has changed permission from app settings after being in
                 * PiP or multi-window mode. So whenever these modes are exited, emit foreground
                 * event.
                 */
                override fun onActivityResumed(activity: Activity) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        if (isActivityResumedAfterMultiWindowOrPiPMode(activity)) {
                            trySend(Unit)
                        }
                        wasInMultiWindowMode = activity.isInMultiWindowMode
                        wasInPictureInPictureMode = activity.isInPictureInPictureMode
                    }
                }

                /**
                 * Whenever application is launched in PiP or multi-window mode, onPaused()
                 * lifecycle callback is triggered.
                 */
                override fun onActivityPaused(activity: Activity) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wasInMultiWindowMode = activity.isInMultiWindowMode
                        wasInPictureInPictureMode = activity.isInPictureInPictureMode
                    }
                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

                override fun onActivityDestroyed(activity: Activity) {}

                /** Returns whether [activity] was previously in multi-window mode or PiP mode. */
                @RequiresApi(Build.VERSION_CODES.N)
                private fun isActivityResumedAfterMultiWindowOrPiPMode(activity: Activity) =
                    (wasInMultiWindowMode == true && !activity.isInMultiWindowMode) ||
                        (wasInPictureInPictureMode == true && !activity.isInPictureInPictureMode)
            }

        registerActivityLifecycleCallbacks(callback)

        awaitClose {
            // Cleanup
            unregisterActivityLifecycleCallbacks(callback)
        }
    }

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

/**
 * A short-hand utility for launching multiple requests with variable arguments support.
 *
 * @param input Input string
 */
fun ActivityResultLauncher<Array<String>>.launch(vararg input: String) =
    launch(input.toList().toTypedArray())

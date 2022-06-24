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
package dev.shreyaspatil.permissionFlow.utils.stateFlow

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * [StateFlow] which delegates [flow] to use it as StateFlow and uses [getValue] to calculate
 * value at the instant.
 */
private class CombinedStateFlow<T>(
    private val getValue: () -> T,
    private val flow: Flow<T>
) : StateFlow<T> {

    override val replayCache: List<T> get() = listOf(value)

    override val value: T get() = getValue()

    override suspend fun collect(collector: FlowCollector<T>): Nothing =
        coroutineScope { flow.stateIn(this).collect(collector) }
}

/**
 * Returns implementation of [CombinedStateFlow]
 */
internal fun <T> combineStates(
    getValue: () -> T,
    flow: Flow<T>
): StateFlow<T> = CombinedStateFlow(getValue, flow)

/**
 * Combines multiple [StateFlow]s and transforms them into another [StateFlow]
 */
internal inline fun <reified T, R> combineStates(
    vararg stateFlows: StateFlow<T>,
    crossinline transform: (Array<T>) -> R
): StateFlow<R> = combineStates(
    getValue = { transform(stateFlows.map { it.value }.toTypedArray()) },
    flow = combine(*stateFlows) { transform(it) }
)

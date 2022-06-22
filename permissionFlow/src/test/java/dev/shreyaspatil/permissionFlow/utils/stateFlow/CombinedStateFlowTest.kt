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

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CombinedStateFlowTest {
    @Test
    fun combineStates_shouldReturnValidValueInitially() {
        // Given: Individual state flows
        val intState = MutableStateFlow(0)
        val stringState = MutableStateFlow("")
        val booleanState = MutableStateFlow(true)

        // When: State flows are combined
        val state = combineStates(intState, stringState, booleanState) { (s1, s2, s3) ->
            TestState(s1 as Int, s2 as String, s3 as Boolean)
        }

        // Then: Combined state should have valid initial value
        val expectedState = TestState(intValue = 0, stringValue = "", booleanValue = true)
        assertEquals(expectedState, state.value)
    }

    @Test
    fun combineStates_shouldUpdateValue_whenIndividualStateIsUpdated() {
        // Given: Individual state flows combined into another state
        val intState = MutableStateFlow(0)
        val stringState = MutableStateFlow("")
        val booleanState = MutableStateFlow(true)

        val state = combineStates(intState, stringState, booleanState) { (s1, s2, s3) ->
            TestState(s1 as Int, s2 as String, s3 as Boolean)
        }

        // When: Individual states are updated
        intState.value = 10
        stringState.value = "Test"
        booleanState.value = false

        // Then: Combined state should have valid updated value
        val expectedState = TestState(intValue = 10, stringValue = "Test", booleanValue = false)
        assertEquals(expectedState, state.value)
    }

    @Test
    fun combineStates_shouldReturnRecentValueAsCache() {
        // Given: Individual state flows combined into another state
        val intState = MutableStateFlow(0)
        val stringState = MutableStateFlow("")
        val booleanState = MutableStateFlow(true)

        val state = combineStates(intState, stringState, booleanState) { (s1, s2, s3) ->
            TestState(s1 as Int, s2 as String, s3 as Boolean)
        }

        // When: Individual states are updated
        intState.value = 10
        stringState.value = "Test"
        booleanState.value = false

        // Then: Combined state should have valid updated value
        val expectedCacheItem = TestState(intValue = 10, stringValue = "Test", booleanValue = false)
        assertEquals(listOf(expectedCacheItem), state.replayCache)
    }

    @Test
    fun combineStates_shouldEmitInCollector_whenIndividualStateIsUpdated() = runTest {
        // Given: Individual state flows combined into another state
        val intState = MutableStateFlow(0)
        val stringState = MutableStateFlow("")
        val booleanState = MutableStateFlow(true)

        val state = combineStates(intState, stringState, booleanState) { (s1, s2, s3) ->
            TestState(s1 as Int, s2 as String, s3 as Boolean)
        }

        // When: Individual states are collected
        state.test {
            // Then: Valid initial state should be emitted
            assertEquals(
                TestState(intValue = 0, stringValue = "", booleanValue = true),
                awaitItem(),
            )

            // Then: Updated state should be emitted on updating individual state
            intState.value = 10
            stringState.value = "Test"
            booleanState.value = false

            // Since we updated three values, test on third event.
            // This is because of StateFlow's conflated behaviour
            // So intentionally receive events twice with `awaitItem()`
            awaitItem()
            awaitItem()

            assertEquals(
                TestState(intValue = 10, stringValue = "Test", booleanValue = false),
                awaitItem(),
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Sample class used to test combining different StateFlows into transformed StateFlow
     * of this class.
     */
    private data class TestState(
        val intValue: Int,
        val stringValue: String,
        val booleanValue: Boolean,
    )
}

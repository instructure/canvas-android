/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.testutils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher

/**
 * Extension function to collect Flow emissions in a test context.
 * This is commonly used to collect ViewModel events or state changes during tests.
 *
 * Usage:
 * ```
 * @Test
 * fun testViewModelEvents() = runTest {
 *     val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)
 *
 *     // Trigger some action
 *     viewModel.handleAction(SomeAction)
 *
 *     // Assert on collected events
 *     assertEquals(ExpectedEvent, events.last())
 * }
 * ```
 *
 * This replaces the boilerplate:
 * ```
 * val events = mutableListOf<EventType>()
 * backgroundScope.launch(viewModelTestRule.testDispatcher) {
 *     viewModel.events.toList(events)
 * }
 * ```
 *
 * @param testDispatcher The test dispatcher to use for collection
 * @param backgroundScope The background scope from runTest
 * @return A mutable list that will be populated with Flow emissions
 */
@ExperimentalCoroutinesApi
fun <T> Flow<T>.collectForTest(
    testDispatcher: TestDispatcher,
    backgroundScope: CoroutineScope
): MutableList<T> {
    val collected = mutableListOf<T>()
    backgroundScope.launch(testDispatcher) {
        this@collectForTest.toList(collected)
    }
    return collected
}

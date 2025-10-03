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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * A JUnit test rule that combines common setup for ViewModel unit tests:
 * - InstantTaskExecutorRule for synchronous LiveData execution
 * - TestDispatcher setup/cleanup for coroutines
 * - Automatic mockk cleanup (unmockkAll)
 *
 * Usage:
 * ```
 * @get:Rule
 * val viewModelTestRule = ViewModelTestRule()
 *
 * // Access the test dispatcher if needed:
 * viewModelTestRule.testDispatcher
 * ```
 *
 * This eliminates boilerplate from every ViewModel test:
 * - No need for separate @get:Rule val instantExecutorRule
 * - No need for @Before/@After with Dispatchers.setMain/resetMain
 * - No need for @After with unmockkAll()
 * - Consistent test dispatcher setup across all tests
 */
@ExperimentalCoroutinesApi
class ViewModelTestRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestRule {

    private val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val coroutineDispatcherRule = object : TestWatcher() {
        override fun starting(description: Description) {
            Dispatchers.setMain(testDispatcher)
        }

        override fun finished(description: Description) {
            Dispatchers.resetMain()
            unmockkAll()
        }
    }

    override fun apply(base: Statement, description: Description): Statement {
        return RuleChain
            .outerRule(instantTaskExecutorRule)
            .around(coroutineDispatcherRule)
            .apply(base, description)
    }
}
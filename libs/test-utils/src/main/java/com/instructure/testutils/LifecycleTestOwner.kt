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
 */

package com.instructure.testutils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import io.mockk.mockk

/**
 * A test utility that provides a LifecycleOwner and LifecycleRegistry for testing ViewModels.
 * This eliminates the need to create these boilerplate objects in every ViewModel test.
 *
 * Usage:
 * ```
 * private val lifecycleTestOwner = LifecycleTestOwner()
 *
 * @Before
 * fun setup() {
 *     // No lifecycle setup needed anymore
 * }
 * ```
 *
 * Note: Tests that previously called `lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)`
 * in their @Before methods no longer need to do so, as this is now handled automatically.
 */
class LifecycleTestOwner {
    val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)

    /**
     * Advance the lifecycle to a specific event.
     */
    fun advanceTo(event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
    }
}

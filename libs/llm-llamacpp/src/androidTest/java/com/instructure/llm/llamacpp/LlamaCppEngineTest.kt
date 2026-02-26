/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.llm.llamacpp

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.pandautils.features.llm.engine.LlmState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LlamaCppEngineTest {

    @Test
    fun nativeLibraryLoadsAndInitializes() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val engine = LlamaCppEngine.create(context)

        // Wait for the engine to finish initializing (moves from Uninitialized -> Initializing -> Ready)
        withTimeout(30_000) {
            engine.state.first { it is LlmState.Ready || it is LlmState.Error }
        }

        val state = engine.state.value
        Log.i("LlamaCppEngineTest", "Engine state after init: $state")

        assertTrue(
            "Expected Ready state but got: $state",
            state is LlmState.Ready
        )

        engine.destroy()
    }
}
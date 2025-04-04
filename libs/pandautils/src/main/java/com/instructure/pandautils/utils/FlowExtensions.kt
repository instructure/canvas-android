/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun <T> LifecycleCoroutineScope.collectOneOffEvents(eventFlow: Flow<T>, onEvent: (T) -> Unit) {
    launch {
        withContext(Dispatchers.Main.immediate) {
            eventFlow.collect {
                onEvent(it)
            }
        }
    }
}

suspend fun <DATA, FIELD> StateFlow<DATA>.collectDistinctUntilChanged(
    lifecycle: Lifecycle,
    selector: (DATA) -> FIELD,
    onChanged: (FIELD) -> Unit
) {
    this.map(selector)
        .distinctUntilChanged()
        .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
        .collectLatest { value ->
            onChanged(value)
        }
}

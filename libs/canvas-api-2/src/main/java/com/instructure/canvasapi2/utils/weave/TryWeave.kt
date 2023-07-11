/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
@file:Suppress("EXPERIMENTAL_FEATURE_WARNING", "unused")

package com.instructure.canvasapi2.utils.weave

import kotlinx.coroutines.*

/**
 * Holds the data necessary for [tryWeave] to work correctly
 */
class TryWeave(val background: Boolean, val block: suspend WeaveCoroutine.() -> Unit)

/**
 * A convenience alternative to [weave] that automatically propagates all exceptions to the [catch]
 * block. An invocation of this function *MUST* be immediately followed by a [catch] block, otherwise
 * the body of [tryWeave] will not be executed and you will be *so embarrassed.*
 */
fun Any.tryWeave(background: Boolean = false, block: suspend WeaveCoroutine.() -> Unit) = TryWeave(background, block)

/**
 * Calling this immediately after [tryWeave] is what separates us from the the animals.
 */
infix fun TryWeave.catch(onException: (e: Throwable) -> Unit): WeaveCoroutine {
    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable !is CancellationException) onException(throwable)
    }
    val context = if (background) Dispatchers.Default else Dispatchers.Main
    val coroutine = WeaveCoroutine(context + exceptionHandler)
    coroutine.start(CoroutineStart.DEFAULT, coroutine, block)
    return coroutine
}

class TryLaunch(val coroutineScope: CoroutineScope, val block: suspend CoroutineScope.() -> Unit)

fun CoroutineScope.tryLaunch(block: suspend CoroutineScope.() -> Unit) = TryLaunch(this, block)

infix fun TryLaunch.catch(onException: (e: Throwable) -> Unit): Job {
    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable !is CancellationException) onException(throwable)
    }

    return coroutineScope.launch(context = coroutineScope.coroutineContext + exceptionHandler, block = block)
}

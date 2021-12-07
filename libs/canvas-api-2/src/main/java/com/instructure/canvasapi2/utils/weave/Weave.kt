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

import com.google.gson.Gson
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.models.CanvasError
import com.instructure.canvasapi2.utils.tryOrNull
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.TimeoutException
import kotlin.coroutines.CoroutineContext

/** Because 'Coroutine' is just too long® */
typealias WeaveJob = WeaveCoroutine

/** Type alias for the call to begin an API request */
internal typealias ManagerCall<T> = (callback: StatusCallback<T>) -> Unit

/** Type alias for the call invoked on API success */
internal typealias SuccessCall<T> = (payload: T) -> Unit

/** Type alias for the call invoke on API failure */
internal typealias ErrorCall = (error: StatusCallbackError) -> Unit

/** Convenience class to hold the information returned in [StatusCallback.onFailure] */
class StatusCallbackError(val call: Call<*>? = null, val error: Throwable? = null, val response: Response<*>? = null) : Throwable(error) {
    val canvasErrors: List<CanvasError>?
        get() = tryOrNull {
            response?.errorBody()?.string()?.let { Gson().fromJson(it, Array<CanvasError>::class.java).toList() }
        }
}

abstract class Blockable<T> {
    open fun complete(result: T){}
    open fun cancel(){}
}

/**
 * A partial generator interface usable by code designed to suspend coroutine execution indefinitely
 * while reactively iterating through a potentially infinite number of internal operations. The primary
 * application of [Stitcher] is to support API pagination within [weave].
 */
interface Stitcher {
    /** Called when the next operation is requested */
    fun next()

    /** A reference to the current [Continuation][kotlin.coroutines.experimental.Continuation] */
    var continuation: CancellableContinuation<Unit>

    /** Should be called by implementations when all internal operations have completed */
    var onRelease: () -> Unit
}

/**
 * WeaveCoroutine - A Coroutine class customized to meet the specific needs of our applications. This
 * includes a modular exception handler, Stitcher support, [onUI] and [inBackground] functions.
 */
@OptIn(InternalCoroutinesApi::class)
class WeaveCoroutine(private val parentContext: CoroutineContext) :
    AbstractCoroutine<Unit>(parentContext, true, true), CoroutineScope {

    /**
     * Runs the provided code on the UI thread. Note that this does *not* suspend the coroutine; as such, any code
     * passed to this function is *not* guaranteed to complete prior to execution of code declared after this function.
     */
    fun onUI(block: () -> Unit) {
        Dispatchers.Main.dispatch(parentContext, Runnable(block))
    }

    suspend fun <T> inBackground(block: suspend CoroutineScope.() -> T): T {
        return async(context = Dispatchers.Default, block = block).await()
    }

    override fun handleJobException(exception: Throwable): Boolean {
        handleCoroutineException(parentContext, exception)
        return true
    }

    // region Stitcher
    private var stitcher: Stitcher? = null

    fun next() = stitcher?.next()

    fun addAndStartStitcher(newStitcher: Stitcher) {
        stitcher = newStitcher
        stitcher?.onRelease = { stitcher = null }
        stitcher?.next()
    }
    // endregion
}

/**
 * Begins a [WeaveCoroutine]
 */
fun weave(background: Boolean = false, block: suspend WeaveCoroutine.() -> Unit): WeaveCoroutine {
    val context = if (background) Dispatchers.Default else Dispatchers.Main
    val coroutine = WeaveCoroutine(context)
    coroutine.start(CoroutineStart.DEFAULT, coroutine, block)
    return coroutine
}

/** Resumes the continuation (with the provided exception) if allowed by the current state. */
fun <T> CancellableContinuation<T>.resumeSafelyWithException(
    e: Throwable,
    stackTrace: Array<out StackTraceElement>? = null
) {
    if (isActive && !isCancelled && !isCompleted) {
        stackTrace?.let { e.stackTrace = it }
        e.printStackTrace()
        resumeWith(Result.failure(e))
    }
}

/** Resumes the continuation if allowed by the current state. */
fun <T> CancellableContinuation<T>.resumeSafely(payload: T) {
    if (isActive && !isCancelled && !isCompleted) resumeWith(Result.success(payload))
}

suspend fun <T> awaitBlockable(managerCall: (Blockable<T>) -> Unit): T {
    return suspendCancellableCoroutine { continuation ->
        val callback = object : Blockable<T>() {
            override fun complete(result: T) {
                continuation.resumeWith(Result.success(result))
            }
        }
        continuation.invokeOnCancellation { if(continuation.isCancelled) callback.cancel() }
        managerCall(callback)
    }
}

/**
 * DO NOT CALL THIS ON THE UI THREAD.
 *
 * Sleeps the current thread until this coroutine is complete or canceled and throws a [TimeoutCancellationException] if execution
 * time exceeds the specified [timeout] (milliseconds). Default timeout is 60 seconds. Specifying a [timeout] of 0
 * will cause the timeout to be ignored.
 *
 * @throws TimeoutCancellationException
 */
fun WeaveCoroutine.runBlocking(timeout: Long = 60_000) {
    val startTime = System.currentTimeMillis()
    while (!isCompleted && !isCancelled) {
        if (timeout > 0 && System.currentTimeMillis() - startTime > timeout) {
            throw TimeoutException("Weave execution exceeded timeout of $timeout milliseconds")
        }
        Thread.sleep(50)
    }
}

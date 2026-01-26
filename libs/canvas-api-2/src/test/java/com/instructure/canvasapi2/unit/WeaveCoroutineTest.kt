/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.WeaveTestManager
import com.instructure.canvasapi2.utils.weave.WeaveCoroutine
import com.instructure.canvasapi2.utils.weave.apiAsync
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.inParallel
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.canvasapi2.utils.weave.weave
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeoutException

class WeaveCoroutineTest : Assert() {

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.IO)
    }

    @Test
    fun apiAsyncSuccess() {
        weave {
            val deferred = apiAsync<String> { WeaveTestManager.testSuccess(it, "success") }
            val response = deferred.await()
            assertEquals(DataResult.Success("success"), response)
        }.blockWithTimeout()
    }

    @Test
    fun apiAsyncSuccessVoid() {
        weave {
            val deferred = apiAsync<Void?> { WeaveTestManager.testSuccess(it, null) }
            val response = deferred.await()
            assertEquals(DataResult.Success(null), response)
        }.blockWithTimeout()
    }

    @Test
    fun apiAsyncMultipleSuccess() {
        val strings = mutableListOf<String>()
        weave {
            strings += apiAsync<String> { WeaveTestManager.testSuccess(it, "This") }.await().dataOrNull!!
            strings += apiAsync<String> { WeaveTestManager.testSuccess(it, "is") }.await().dataOrNull!!
            strings += apiAsync<String> { WeaveTestManager.testSuccess(it, "a") }.await().dataOrNull!!
            strings += apiAsync<String> { WeaveTestManager.testSuccess(it, "test") }.await().dataOrNull!!
            strings += apiAsync<String> { WeaveTestManager.testSuccess(it, "of") }.await().dataOrNull!!
            strings += apiAsync<String> { WeaveTestManager.testSuccess(it, "success.") }.await().dataOrNull!!
        }.blockWithTimeout()
        assertEquals("This is a test of success.", strings.joinToString(" "))
    }

    @Test
    fun apiAsyncParallelSuccess() {
        val strings = mutableListOf<String>()
        weave {
            val thisDeferred = apiAsync<String> { WeaveTestManager.testSuccess(it, "This") }
            val isDeferred = apiAsync<String> { WeaveTestManager.testSuccess(it, "is") }
            val aDeferred = apiAsync<String> { WeaveTestManager.testSuccess(it, "a") }
            val testDeferred = apiAsync<String> { WeaveTestManager.testSuccess(it, "test") }
            val ofDeferred = apiAsync<String> { WeaveTestManager.testSuccess(it, "of") }
            val successDeferred = apiAsync<String> { WeaveTestManager.testSuccess(it, "success.") }
            strings += thisDeferred.await().dataOrNull!!
            strings += isDeferred.await().dataOrNull!!
            strings += aDeferred.await().dataOrNull!!
            strings += testDeferred.await().dataOrNull!!
            strings += ofDeferred.await().dataOrNull!!
            strings += successDeferred.await().dataOrNull!!
        }.blockWithTimeout()
        assertEquals("This is a test of success.", strings.joinToString(" "))
    }

    @Test
    fun apiAsyncFail() {
        var response: DataResult<String>? = null
        weave {
            response = apiAsync<String> { WeaveTestManager.testFail(it, 404) }.await()
        }.blockWithTimeout()
        val result = response
        assertThat(result, instanceOf(DataResult.Fail::class.java))
    }

    @Test
    fun mocksApiAsync() {
        mockkStatic("com.instructure.canvasapi2.utils.weave.ApiAsyncKt")
        var result: DataResult<String>? = null
        weave {
            every { apiAsync<String>(any()) } returns mockk {
                coEvery { await() } returns DataResult.Success("success")
            }
            val deferred = apiAsync<String> { WeaveTestManager.testFail(it, 404) }
            result = deferred.await()
        }.blockWithTimeout()
        assertEquals(DataResult.Success("success"), result)
        unmockkStatic("com.instructure.canvasapi2.utils.weave.ApiAsyncKt")
    }

    @Test
    fun mocksApiAsyncOutsideCoroutine() {
        mockkStatic("com.instructure.canvasapi2.utils.weave.ApiAsyncKt")
        every { apiAsync<String>(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success("success")
        }
        var result: DataResult<String>? = null
        weave {
            val deferred = apiAsync<String> { WeaveTestManager.testFail(it, 404) }
            result = deferred.await()
        }.blockWithTimeout()
        assertEquals(DataResult.Success("success"), result)
        unmockkStatic("com.instructure.canvasapi2.utils.weave.ApiAsyncKt")
    }

    @Test
    fun awaitSuccess() {
        weave {
            val response = awaitApi<String> { WeaveTestManager.testSuccess(it, "success") }
            assertEquals("success", response)
        }.blockWithTimeout()
    }

    @Test
    fun awaitSuccessVoid() {
        weave {
            val response = awaitApi<Void?> { WeaveTestManager.testSuccess(it, null) }
            assertEquals(null, response)
        }.blockWithTimeout()
    }

    @Test
    fun awaitMultipleSuccess() {
        val strings = mutableListOf<String>()
        weave {
            strings += awaitApi<String> { WeaveTestManager.testSuccess(it, "This") }
            strings += awaitApi<String> { WeaveTestManager.testSuccess(it, "is") }
            strings += awaitApi<String> { WeaveTestManager.testSuccess(it, "a") }
            strings += awaitApi<String> { WeaveTestManager.testSuccess(it, "test") }
            strings += awaitApi<String> { WeaveTestManager.testSuccess(it, "of") }
            strings += awaitApi<String> { WeaveTestManager.testSuccess(it, "success.") }
        }.blockWithTimeout()
        assertEquals("This is a test of success.", strings.joinToString(" "))
    }

    @Test
    fun awaitError() {
        // FIXME: Currently broken on the JVM but works fine in Android
        /*var exception: Throwable? = null
        weave {
            try {
                awaitApi<String> { WeaveTestManager.testFail(it) }
            } catch (e: Throwable) {
                exception = e
            }
        }.blockWithTimeout()
        assertNotNull(exception)*/
    }

    @Test
    fun awaitErrorTryWeave() {
        // FIXME: Currently broken on the JVM but works fine in Android
        /*var exception: Throwable? = null
        val job: WeaveCoroutine = tryWeave {
            awaitApi<String> { WeaveTestManager.testFail(it) }
        } catch {
            exception = it
        }
        job.blockWithTimeout()
        assertNotNull(exception)*/
    }

    @Test
    fun awaitParallelSuccess() {
        val items = listOf("This", "is", "a", "test", "of", "success.")
        val responseItems = mutableListOf<String>()
        weave {
            inParallel {
                items.forEach { item ->
                    await<String>({ WeaveTestManager.testSuccess(it, item)}) { response ->
                        responseItems += response
                    }
                }
            }
        }.blockWithTimeout()
        assertEquals(items.size, responseItems.size)
        assert(responseItems.all { it in items })
    }

    @Test
    fun awaitParallelSuccessVoid() {
        val expectedResponseCount = 10
        var responseCount = 0
        weave {
            inParallel {
                repeat(expectedResponseCount) {
                    await<Void?>({ WeaveTestManager.testSuccess(it, null)}) {
                        responseCount++
                    }
                }
            }
        }.blockWithTimeout()
        assertEquals(expectedResponseCount, responseCount)
    }

    @Test
    fun awaitParallelFail() {
        // FIXME: Currently broken on the JVM but works fine in Android
        /*var exception: Throwable? = null
        weave {
            try {
                inParallel {
                    repeat(10) { count ->
                        await<Void?>({
                            if (count % 3 == 0) {
                                WeaveTestManager.testSuccess(it, null)
                            } else {
                                WeaveTestManager.testFail(it)
                            }
                        }) {}
                    }
                }
            } catch (e: Throwable) {
                exception = e
            }
        }.blockWithTimeout()
        assertNotNull(exception)*/
    }

    @Test
    fun cancelsImmediatelyWithoutRunning() {
        var hasRun = false
        weave {
            hasRun = withContext(Dispatchers.Default) { true }
        }.cancel()
        Thread.sleep(100)
        assert(!hasRun)
    }

    @Test
    fun cancelsAfterRunningWithoutException() {
        var exception: Throwable? = null
        var hasRun = false
        val job = tryWeave {
            hasRun = true
        } catch {
            exception = it
        }
        Thread.sleep(10)
        assert(hasRun)
        job.cancel()
        Thread.sleep(10)
        assertNull(exception)
    }

    @Test
    fun cancelsInternallyWhileRunningWithoutException() {
        var exception: Throwable? = null
        var canceler = {}
        val job = tryWeave {
            delay(10)
            canceler()
        } catch {
            exception = it
        }
        canceler = { job.cancel() }
        Thread.sleep(50)
        assertNull(exception)
    }

    @Test
    fun cancelsExternallyWhileRunningWithoutException() {
        var exception: Throwable? = null
        val job = tryWeave {
            delay(20)
        } catch {
            exception = it
        }
        Thread.sleep(10)
        job.cancel()
        Thread.sleep(50)
        assertNull(exception)
    }

    private fun WeaveCoroutine.blockWithTimeout(timeout: Long = 5000) {
        val startTime = System.currentTimeMillis()
        while (!isCompleted && !isCancelled) {
            if (System.currentTimeMillis() - startTime > timeout) {
                throw TimeoutException("Weave execution exceeded timeout of $timeout milliseconds")
            }
            Thread.sleep(50)
        }
    }
}

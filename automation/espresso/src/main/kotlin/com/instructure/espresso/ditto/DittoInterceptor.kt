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
@file:Suppress("PackageDirectoryMismatch")

package okreplay

import android.util.Base64
import okhttp3.Interceptor
import okhttp3.ResponseBody
import java.io.IOException

/**
 * Provides VCR functionality by intercepting network requests and either recording them to tape or playing them back
 * from tape, depending on the tape's current mode.
 *
 * To enable this interceptor you must first call [start] and pass in a valid [DittoTape]. If [start] is not called then
 * all intercepted requests will proceed as normal. Make sure to call [stop] when this interceptor is no longer needed.
 */
open class DittoInterceptor : Interceptor {

    private lateinit var tape: DittoTape
    private var isStarted: Boolean = false
    private var playSeedCount = 0
    private var recordSeedCount = 0

    fun start(tape: DittoTape) {
        this.tape = tape
        playSeedCount = 0
        recordSeedCount = 0
        isStarted = true
    }

    fun stop() {
        isStarted = false
    }

    fun playTestData(label: String): String? {
        if (!isStarted || !tape.isReadable) return null
        val requestName = "string-$label"
        val request = TestDataRequest(requestName)
        if (!tape.seek(request)) return null
        return tape.play(request).bodyAsText()
    }

    fun recordTestData(label: String, value: String): Boolean {
        if (!isStarted || !tape.isWritable) return false
        val requestName = "string-$label"
        val request = TestDataRequest(requestName)
        tape.record(request, TestDataResponse(value))
        return true
    }

    private fun seedDataLabel(count: Int) = "seed data $count"

    fun playSeededJson(): String? {
        if (!isStarted) return null
        val label = seedDataLabel(playSeedCount)
        val dataString = playTestData(label) ?: return null
        playSeedCount++
        return dataString
    }
    fun recordSeededJson(json: String) {
        if (!isStarted) return
        val label = seedDataLabel(recordSeedCount)
        if (recordTestData(label, json)) recordSeedCount++
    }


    fun playSeededDataBytes(): ByteArray? {
        if (!isStarted) return null
        val label = seedDataLabel(playSeedCount)
        val dataString = playTestData(label) ?: return null
        playSeedCount++
        return Base64.decode(dataString, Base64.DEFAULT)
    }

    fun recordSeededDataBytes(data: ByteArray) {
        if (!isStarted) return
        val label = seedDataLabel(recordSeedCount)
        val dataString = Base64.encodeToString(data, Base64.DEFAULT)
        if (recordTestData(label, dataString)) recordSeedCount++
    }

    fun addResponseMod(mod: DittoResponseMod) {
        if (isStarted) tape.responseMods.add(mod)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()

        // Skip if the interceptor has not been started
        if (!isStarted) return chain.proceed(request)

        val recordedRequest = OkHttpRequestAdapter.adapt(request)
        if (tape.isReadable && tape.seek(recordedRequest)) {
            var recordedResponse = tape.play(recordedRequest)
            tape.responseMods.find { it.matches(recordedRequest) }?.let { mod ->
                recordedResponse = mod.modifyResponse(recordedResponse)
            }
            return OkHttpResponseAdapter.adapt(request, recordedResponse)
        } else {
            var okHttpResponse: okhttp3.Response = chain.proceed(request)
            if (tape.isWritable) {
                val contentType = okHttpResponse.body()!!.contentType()
                val bodyBytes = okHttpResponse.body()!!.bytes()
                val recordedResponse =
                    OkHttpResponseAdapter.adapt(okHttpResponse, ResponseBody.create(contentType, bodyBytes))
                tape.record(recordedRequest, recordedResponse)
                okHttpResponse = okHttpResponse.newBuilder()
                    .body(ResponseBody.create(contentType, bodyBytes))
                    .build()
            } else {
                throw RuntimeException(
                    """
                    Unhandled request!
                    ================================================================================

                    Ditto could not find a recording for following request:
                      ${request.method()} ${request.url()}

                    To record this request and play it back during future test runs:
                        1. Set the test annotation to `@Ditto(mode = DittoMode.RECORD)`
                        2. Run the test again
                        3. Run the 'pullOkReplayTapes' task

                    ================================================================================
                    """.trimIndent()
                )
            }
            return okHttpResponse
        }
    }

}

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
package com.instructure.canvasapi2.utils

import com.instructure.canvasapi2.StatusCallback
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import java.util.*
import kotlin.concurrent.thread

/**
 * DO NOT DELETE. Required by weave to check if unit testing
 */
@Suppress("unused")
class WeaveTest

object WeaveTestManager {

    inline fun <reified T> testSuccess(callback: StatusCallback<T>, payload: T) {
        thread {
            Thread.sleep(100L + Random().nextInt(100))
            val response = Response.Builder()
                .request(Request.Builder().url("https://test.com").build())
                .code(200)
                .message("todo")
                .protocol(Protocol.HTTP_1_0)
                .body("todo".toByteArray().toResponseBody("application/json".toMediaTypeOrNull()))
                .addHeader("content-type", "application/json")
                .build()

            val retrofitResponse = retrofit2.Response.success(payload, response)
            callback.onResponse(retrofitResponse, LinkHeaders(), ApiType.CACHE)
        }
    }

    inline fun <reified T> testFail(callback: StatusCallback<T>, errorCode: Int = 401) {
        thread {
            Thread.sleep(100L + Random().nextInt(100))
            val response = Response.Builder()
                .request(Request.Builder().url("https://test.com").build())
                .code(errorCode)
                .message("todo")
                .protocol(Protocol.HTTP_1_0)
                .body("""{"error": "Fake error"}""".toResponseBody("application/json".toMediaTypeOrNull()))
                .addHeader("content-type", "application/json")
                .build()

            val retrofitResponse = retrofit2.Response.error<T>(errorCode, response.body!!)
            callback.onFail(null, RuntimeException("Mock error!"), retrofitResponse)
        }
    }

}

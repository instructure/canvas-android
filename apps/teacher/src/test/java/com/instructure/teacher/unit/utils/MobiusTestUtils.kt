/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.teacher.unit.utils

import com.instructure.canvasapi2.utils.weave.StatusCallbackError
import com.spotify.mobius.First
import com.spotify.mobius.Next
import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.NextMatchers
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.hamcrest.Matcher
import org.hamcrest.Matchers

fun <M, F> matchesEffects(vararg effects: F): Matcher<Next<M, F>> {
    return NextMatchers.hasEffects(Matchers.containsInAnyOrder<F>(*effects))
}

fun <M, F> matchesFirstEffects(vararg effects: F): Matcher<First<M, F>> {
    return FirstMatchers.hasEffects(Matchers.containsInAnyOrder<F>(*effects))
}

fun <T> createError(message: String = "Error", code: Int = 400) =
    StatusCallbackError(
        null,
        null,
        retrofit2.Response.error<T>(
            "".toResponseBody(null),
            Response.Builder()
                .protocol(Protocol.HTTP_1_1)
                .message(message)
                .code(code)
                .request(Request.Builder().url("http://localhost/").build())
                .build()
        )
    )

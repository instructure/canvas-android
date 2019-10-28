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
package com.instructure.canvas.espresso.mockCanvas.utils

import com.google.gson.Gson
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvasapi2.models.User
import okhttp3.*

/**
 * Creates a successful response for this [Request] with a response code of 200 and response [body] serialized to json
 */
fun Request.successResponse(body: Any): Response {
    val responseBody = ResponseBody.create(
        MediaType.parse("application/json"),
        Gson().toJson(body)
    )
    return Response.Builder()
        .request(this)
        .body(responseBody)
        .message("Success")
        .protocol(Protocol.HTTP_1_1)
        .code(200)
        .build()
}

/**
 * Creates a "204 No Content" response for this [Request], with no body.
 */
fun Request.noContentResponse(): Response {
    return Response.Builder()
            .request(this)
            .body(ResponseBody.create(MediaType.parse("text/plain"), ""))
            .message("No Content")
            .protocol(Protocol.HTTP_1_1)
            .code(204)
            .build()
}

/**
 * Creates a successful response for this [Request] with a response code of 200 and the response [body] list serialized
 * to json. This will eventually support pagination by parsing the request's pagination query parameters, trimming
 * the response [body] list to the correct sublist, and adding the appropriate pagination response headers.
 */
fun Request.successPaginatedResponse(body: List<Any>): Response {
    // TODO: Add pagination support
    val responseBody = ResponseBody.create(
        MediaType.parse("application/json"),
        Gson().toJson(body)
    )
    return Response.Builder()
        .request(this)
        .body(responseBody)
        .message("Success")
        .protocol(Protocol.HTTP_1_1)
        .code(200)
        .build()
}

/**
 * Creates an unauthorized (401) response for this request
 */
fun Request.unauthorizedResponse(): Response {
    val body =
        ResponseBody.create(MediaType.parse("application/json"), """{ "error": "Unauthorized" }""")
    return Response.Builder()
        .message("Unauthorized")
        .protocol(Protocol.HTTP_1_1)
        .body(body)
        .request(this)
        .code(401)
        .build()
}

/**
 * The user associated with this request as determined by the request's authentication token. Will be null if this
 * request does not have an auth token or there is no user associated with the auth token.
 */
val Request.user: User?
    get() {
        return CanvasAuthModel.getAuth(this)?.let {
            val userId = MockCanvas.data.tokens[it]
            MockCanvas.data.users[userId]
        }
    }

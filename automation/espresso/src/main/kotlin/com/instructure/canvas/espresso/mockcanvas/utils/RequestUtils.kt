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
package com.instructure.canvas.espresso.mockcanvas.utils

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvasapi2.models.User
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import okio.IOException

/**
 * Creates a successful response for this [Request] with a response code of 200 and response [body] serialized to json
 */
fun Request.successResponse(body: Any): Response {
    val responseBody = Gson().toJson(body)
        .toResponseBody("application/json".toMediaTypeOrNull())
    return Response.Builder()
        .request(this)
        .body(responseBody)
        .message("Success")
        .protocol(Protocol.HTTP_1_1)
        .code(200)
        .build()
}

fun Request.successRedirectWithHeader(header: String, headerValue: String): Response {
    val responseBody = "hodor"
        .toResponseBody("text/plain".toMediaTypeOrNull())
    return Response.Builder()
            .request(this)
            .header(header, headerValue)
            .body(responseBody)
            .message("Success")
            .protocol(Protocol.HTTP_1_1)
            .code(308)
            .build()
}

/**
 * Creates a successful response for this [Request] with a response code of 200 and a plain text response [body]
 */
fun Request.successResponseRaw(body: String): Response {
    val responseBody = body
        .toResponseBody("text/plain".toMediaTypeOrNull())
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
            .body("".toResponseBody("text/plain".toMediaTypeOrNull()))
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
    val responseBody = Gson().toJson(body)
        .toResponseBody("application/json".toMediaTypeOrNull())
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
        """{ "error": "Unauthorized" }""".toResponseBody("application/json".toMediaTypeOrNull())
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

// Arrggh... There has to be an easier way or standard call for doing this, but a few hours of
// Googling did not yield such a beast.  We need a way to parse the incoming JSON body of a
// POST request.  The body might look something like this:
//    --39f93652-2013-49f1-85c5-c9373052de66
//    Content-Disposition: form-data; name="title"
//    Content-Transfer-Encoding: binary
//    Content-Type: multipart/form-data; charset=utf-8
//    Content-Length: 21
//
//    Discussion Topic Name
//    --39f93652-2013-49f1-85c5-c9373052de66
//    Content-Disposition: form-data; name="message"
//    Content-Transfer-Encoding: binary
//    Content-Type: multipart/form-data; charset=utf-8
//    Content-Length: 8
//
//    Awesome!
//    --39f93652-2013-49f1-85c5-c9373052de66
fun grabJsonFromMultiPartBody(body: RequestBody) : JsonObject {
    val buffer = Buffer()
    body.writeTo(buffer)

    val result = JsonObject()

    // Read in the initial marker line
    val firstLine = buffer.readUtf8Line()
    if(firstLine == null || !firstLine.startsWith("--")) {
        return result
    }

    while(grabJsonFieldFromBuffer(buffer,result)) { }
    return result
}

private fun grabJsonFieldFromBuffer(buffer: Buffer, jsonObject: JsonObject): Boolean {
    // Read a number of header lines followed by a blank line
    // We should be able to grab the json field name from the header lines.
    var fieldName: String? = null
    var line = buffer.readUtf8Line()
    while(line != null && line.length > 0) {
        val nameRegex = """name=\"(\w+)\"""".toRegex()
        val match = nameRegex.find(line)
        if(match != null) {
            fieldName = match.groupValues[1]
            Log.d("<--","Found fieldName=$fieldName in line=$line")
        }
        line = buffer.readUtf8Line()
    }

    if(line == null) return false // Otherwise, it was blank

    // Now read the contents, which will end with a marker line
    val contentBuilder = StringBuilder()
    line = buffer.readUtf8Line()

    // NOTE: Multi-line content supported, but not yet really tested.
    while(line != null && !line.startsWith("--")) {
        contentBuilder.append(line)
        line = buffer.readUtf8Line()
    }

    if(line == null) return false // Otherwise, we just read in our content

    // Grab the field content.  It could be multiple lines
    var fieldStringValue = contentBuilder.toString()
    if(fieldStringValue == null || fieldStringValue.length == 0) return false

    // Let's attempt to add our field and value to jsonObject, correctly typed
    var fieldValue: Any? = null
    if(fieldStringValue.equals("true", ignoreCase = true) || fieldStringValue.equals("false", ignoreCase = true))
    {
        jsonObject.addProperty(fieldName,fieldStringValue.toBoolean())
    }
    else {
        fieldValue = fieldStringValue.toIntOrNull()
        if(fieldValue != null) {
            jsonObject.addProperty(fieldName, fieldValue)
        }
        else{
            fieldValue = fieldStringValue.toDoubleOrNull()
            if (fieldValue != null) {
                jsonObject.addProperty(fieldName, fieldValue)
            } else {
                jsonObject.addProperty(fieldName, fieldStringValue)
            }
        }
    }
    return true
}

inline fun <reified T> getJsonFromRequestBody(requestBody: RequestBody?): T? {
    val jsonString = try {
        val buffer = Buffer()
        requestBody?.writeTo(buffer)
        buffer.readUtf8()
    } catch (e: IOException) {
        return null
    }

    return Gson().fromJson(jsonString, T::class.java)
}
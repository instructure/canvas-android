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
package com.instructure.canvas.espresso.mockcanvas

import com.instructure.canvas.espresso.mockcanvas.utils.PathVars
import okhttp3.Request
import okhttp3.Response

/**
 * A convenience class for more easily responding to HTTP requests with specific HTTP methods
 */
@Suppress("TestFunctionName", "unused", "FunctionName")
class HttpResponder(
    val data: MockCanvas,
    val currentPath: List<String>,
    val pathVars: PathVars,
    val request: Request
) {

    private var getMethod: (() -> Response)? = null
    private var headMethod: (() -> Response)? = null
    private var postMethod: (() -> Response)? = null
    private var putMethod: (() -> Response)? = null
    private var deleteMethod: (() -> Response)? = null
    private var headMethod: (() -> Response)? = null

    fun HttpResponder.GET(onHandle: () -> Response) {
        getMethod = onHandle
    }

    fun HttpResponder.HEAD(onHandle: () -> Response) {
        headMethod = onHandle
    }

    fun HttpResponder.POST(onHandle: () -> Response) {
        postMethod = onHandle
    }

    fun HttpResponder.PUT(onHandle: () -> Response) {
        putMethod = onHandle
    }

    fun HttpResponder.DELETE(onHandle: () -> Response) {
        deleteMethod = onHandle
    }

    fun HttpResponder.HEAD(onHandle: () -> Response) {
        headMethod = onHandle
    }

    fun handle(): Response {
        val method = request.method
        return when {
            method == "GET" && getMethod != null -> getMethod!!()
            method == "HEAD" && headMethod != null -> headMethod!!()
            method == "POST" && postMethod != null -> postMethod!!()
            method == "PUT" && putMethod != null -> putMethod!!()
            method == "DELETE" && deleteMethod != null -> deleteMethod!!()
            method == "HEAD" && headMethod != null -> headMethod!!()
            else -> throw NoSuchMethodError("Unhandled HTTP method '$method' for request ${request.url}")
        }
    }

    /** For dev purposes only. Prints the implemented HTTP method handlers. */
    fun printTree(current: String): Boolean {
        getMethod?.let { println("GET    $current\n") }
        postMethod?.let { println("POST   $current\n") }
        putMethod?.let { println("PUT    $current\n") }
        deleteMethod?.let { println("DELETE $current\n") }
        headMethod?.let { println("HEAD   $current\n") }
        return getMethod != null || postMethod != null || putMethod != null || deleteMethod != null || headMethod != null
    }

}

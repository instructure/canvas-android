/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
@file:Suppress("unused")

package com.instructure.canvas.espresso.mockcanvas

import com.instructure.canvas.espresso.mockcanvas.utils.AuthModel
import com.instructure.canvas.espresso.mockcanvas.utils.CanvasAuthModel
import com.instructure.canvas.espresso.mockcanvas.utils.PathVars
import com.instructure.canvas.espresso.mockcanvas.utils.SegmentQualifier
import com.instructure.canvas.espresso.mockcanvas.utils.unauthorizedResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * A class that emulates a Canvas API endpoint and issues a [Response] for a given [Request].
 *
 * If an endpoint matches the request path, it is directly responsible for creating a [response] to that request. If
 * it does not match, but one of its [children] matches the next path segment to be processed, it will delegate its
 * responsibility to that child.
 */
open class Endpoint(
    private vararg val children: Pair<SegmentQualifier<*>, Endpoint>,
    private val response: HttpResponder.() -> Unit = {}
) {

    private var responseOverride: (HttpResponder.() -> Unit)? = null

    /** Overrides the response (including authorization check) for the next request made to this endpoint. */
    open fun overrideNextResponse(override: HttpResponder.() -> Unit) {
        responseOverride = override
    }

    open val authModel: AuthModel = CanvasAuthModel

    open fun routeRequest(currentPath: List<String>, vars: PathVars, request: Request): Response {
        if (currentPath.isEmpty()) {
            // This likely won't happen, but could occur if an endpoint incorrectly overrides routeRequest
            throw IllegalStateException("Encountered empty path while processing request ${request.url}")
        }

        // Handle here if we match the current path (i.e. stop condition - this is the last segment)
        if (currentPath.size == 1) {
            // Handle via override, if one has been set
            responseOverride?.let {
                responseOverride = null
                val responder = HttpResponder(
                    MockCanvas.data,
                    currentPath,
                    vars,
                    request
                )
                it.invoke(responder)
                return responder.handle()
            }

            // Ensure the user is authorized
            if (!authModel.isAuthorized(request)) return request.unauthorizedResponse()

            // Handle request
            return handleRequest(currentPath, vars, request)
        }

        // Attempt to delegate to the first child that matches the next path segment
        val nextSegment = currentPath[1]
        children.forEach { (qualifier, endpoint) ->
            if (qualifier.matches(nextSegment)) {
                qualifier.appendVars(nextSegment, vars, request)
                return endpoint.routeRequest(currentPath.drop(1), vars, request)
            }
        }

        // If not handled, return a 404
        val body = """No mock endpoint implemented for request: ${request.url}"""
            .toResponseBody("application/json".toMediaTypeOrNull())
        return Response.Builder()
            .request(request)
            .body(body)
            .message("No mock endpoint implemented for request")
            .protocol(Protocol.HTTP_1_1)
            .code(404)
            .build()
    }

    open fun handleRequest(currentPath: List<String>, vars: PathVars, request: Request): Response {
        val responder = HttpResponder(
            MockCanvas.data,
            currentPath,
            vars,
            request
        )
        responder.response()
        return responder.handle()
    }

    /** For dev purposes only. Prints the covered endpoints. */
    @Suppress("MemberVisibilityCanBePrivate")
    fun printTree(current: String) {
        val fakeRequest = Request.Builder()
            .url("http://mock-data.instructure.com")
            .build()
        val responder = HttpResponder(
            MockCanvas(),
            emptyList(),
            PathVars(),
            fakeRequest
        )
        responder.response()
        if (!responder.printTree(current)) println("       $current\n")
        children.forEach {
            it.second.printTree("$current/${it.first.printName}")
        }
    }
}

/** Convenience function for creating an anonymous endpoint */
fun endpoint(
    vararg children: Pair<SegmentQualifier<*>, Endpoint>,
    configure: HttpResponder.() -> Unit = {}
) = Endpoint(*children, response = configure)

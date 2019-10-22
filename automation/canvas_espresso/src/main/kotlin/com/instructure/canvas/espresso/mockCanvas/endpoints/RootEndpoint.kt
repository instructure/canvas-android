package com.instructure.canvas.espresso.mockCanvas.endpoints

import com.instructure.canvas.espresso.mockCanvas.Endpoint
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.endpoint
import com.instructure.canvas.espresso.mockCanvas.utils.PathVars
import com.instructure.canvas.espresso.mockCanvas.utils.Segment
import com.instructure.canvas.espresso.mockCanvas.utils.successResponse
import com.instructure.canvas.espresso.mockCanvas.utils.unauthorizedResponse
import com.instructure.canvasapi2.models.AuthenticatedSession
import okhttp3.Request
import okhttp3.Response
import retrofit2.http.GET

/**
 * The root endpoint of [MockCanvas]. It does not return anything itself, but prepends the request path with an
 * empty root segment to ensure path routing works correctly.
 *
 * ROUTES:
 * - `oauth` -> [OAuthEndpoint]
 * - `api/v1` -> [ApiEndpoint]
 * - 'files' -> [FileListEndpoint]
 * - 'login' -> inlined login endpoints
 */
object RootEndpoint : Endpoint(
    Segment("oauth") to OAuthEndpoint,
    Segment("api") to endpoint(
        Segment("v1") to ApiEndpoint
    ),
    Segment("files") to FileListEndpoint,
    Segment("login") to endpoint(
            Segment("session_token") to endpoint {
                GET {
                    // Just echo the request's "return_to" parameter back to the caller.
                    // It won't contain all of the information that a normal /login/session_token
                    // response would have, but that information is unnecessary for mocked API calls.
                    val sessionUrl = request.url().queryParameter("return_to")
                    request.successResponse(AuthenticatedSession(sessionUrl=sessionUrl!!))
                }
            }
    )

) {
    override fun routeRequest(currentPath: List<String>, vars: PathVars, request: Request): Response {
        // Prepend with empty 'root' segment
        val pathSegments = listOf("") + currentPath
        return super.routeRequest(pathSegments, vars, request)
    }
}

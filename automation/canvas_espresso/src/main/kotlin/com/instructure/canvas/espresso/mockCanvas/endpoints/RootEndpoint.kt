package com.instructure.canvas.espresso.mockCanvas.endpoints

import com.instructure.canvas.espresso.mockCanvas.Endpoint
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.endpoint
import com.instructure.canvas.espresso.mockCanvas.utils.PathVars
import com.instructure.canvas.espresso.mockCanvas.utils.Segment
import okhttp3.Request
import okhttp3.Response

/**
 * The root endpoint of [MockCanvas]. It does not return anything itself, but prepends the request path with an
 * empty root segment to ensure path routing works correctly.
 *
 * ROUTES:
 * - `oauth` -> [OAuthEndpoint]
 * - `api/v1` -> [ApiEndpoint]
 */
object RootEndpoint : Endpoint(
    Segment("oauth") to OAuthEndpoint,
    Segment("api") to endpoint(
        Segment("v1") to ApiEndpoint
    )
) {
    override fun routeRequest(currentPath: List<String>, vars: PathVars, request: Request): Response {
        // Prepend with empty 'root' segment
        val pathSegments = listOf("") + currentPath
        return super.routeRequest(pathSegments, vars, request)
    }
}

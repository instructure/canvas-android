package com.instructure.canvas.espresso.mockCanvas.endpoints

import android.util.Log
import com.instructure.canvas.espresso.mockCanvas.Endpoint
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.endpoint
import com.instructure.canvas.espresso.mockCanvas.utils.PathVars
import com.instructure.canvas.espresso.mockCanvas.utils.Segment
import com.instructure.canvas.espresso.mockCanvas.utils.successResponse
import com.instructure.canvasapi2.models.AuthenticatedSession
import okhttp3.Request
import okhttp3.Response
import java.net.URL

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
                        // We primarily hit this endpoint for webviews wishing to establish a
                        // valid session for their content.  Instead of just echoing back the
                        // "return_to" url to the sender, this is where we swap out our
                        // "mock-data.instructure.com" domain, and swap in the domain from our
                        // special webserver.  This will cause the Webview's ensuing request
                        // to hit our special webserver.
                        //
                        // The response here won't contain all of the information that a normal
                        // /login/session_token response would have (e.g., actual tokens), but that
                        // information is unnecessary for mocked API calls.
                        val sessionUrlString = request.url().queryParameter("return_to")
                        val originalSessionUrl = URL(sessionUrlString)
                        val sessionUrl = "${data.webViewServer.url(originalSessionUrl.path)}"
                        Log.d("WebView", "original url=$sessionUrlString, new url=$sessionUrl")
                        request.successResponse(AuthenticatedSession(sessionUrl = sessionUrl))
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

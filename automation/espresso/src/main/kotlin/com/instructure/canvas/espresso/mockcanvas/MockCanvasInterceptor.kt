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

import com.instructure.canvas.espresso.mockcanvas.endpoints.RootEndpoint
import com.instructure.canvas.espresso.mockcanvas.utils.PathVars
import okhttp3.Interceptor
import okhttp3.Response

/**
 * An [Interceptor] which provides mocked network responses using MockCanvas. If the mock data has not been
 * initialized (e.g. by calling [MockCanvas.Companion.init]) then this interceptor has no effect.
 */
class MockCanvasInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Skip if data is not initialized
        if (!MockCanvas.isInitialized) return chain.proceed(chain.request())
        val request = chain.request()
        val segments = request.url.pathSegments
        return RootEndpoint.routeRequest(
            segments,
            PathVars(), request
        )
    }
}

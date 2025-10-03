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

import com.instructure.canvasapi2.utils.validOrNull
import okhttp3.Request

/**
 * An interface for providing authentication verification of various mocked auth implementations
 */
interface AuthModel {
    fun getAuth(request: Request): String?
    fun isAuthorized(request: Request): Boolean
}

/**
 * An AuthModel for standard Canvas authentication which uses a bearer token auth header
 */
object CanvasAuthModel : AuthModel {
    override fun getAuth(request: Request): String? {
        val header = request.header("Authorization") ?: return null
        if (!header.startsWith("Bearer ")) return null
        return header.substringAfter("Bearer ").validOrNull()
    }

    override fun isAuthorized(request: Request): Boolean {
        return request.user != null
    }
}

/**
 * A "don't care" AuthModel.  Initially used for downloading files.
 */
object DontCareAuthModel : AuthModel {
    override fun getAuth(request: Request) : String? {
        return null
    }

    override fun isAuthorized(request: Request) : Boolean {
        return true
    }
}

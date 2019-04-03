//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//


package api.bitrise.private

import retrofit2.Response
import java.net.URLDecoder

/**
 * Track global state associated with the signed in Bitrise user
 * Used by CookieRetrofit to access the Bitrise private APIs
 */
object State {
    var cookies: List<String> = listOf() // Cookie header
    var xsrfToken = "" // X-XSRF-TOKEN header

    fun updateState(response: Response<User>) {
        //  0 = "logged_in=
        //  1 = "remember_user_token=
        //  2 = "XSRF-TOKEN=
        //  3 = "_concrete_website_session=

        cookies = response.headers().values("Set-Cookie")
        val xsrfPrefix = "XSRF-TOKEN="
        cookies.forEach { cookie ->
            if (cookie.startsWith(xsrfPrefix)) {
                xsrfToken = cookie.substring(xsrfPrefix.length, cookie.indexOf(';'))
                xsrfToken = URLDecoder.decode(xsrfToken, "UTF-8")
            }
        }
    }
}

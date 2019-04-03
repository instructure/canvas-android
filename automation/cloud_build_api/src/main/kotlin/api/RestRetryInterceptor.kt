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


package api

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class RestRetryInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var lastErr: IOException? = null

        for (i in 1..3) {
            try {
                return chain.proceed(chain.request())
            } catch (err: IOException) {
                lastErr = err
                System.err.println("Request failed, retrying ${i}x $err")
            }
        }

        throw IOException("Request failed", lastErr)
    }
}

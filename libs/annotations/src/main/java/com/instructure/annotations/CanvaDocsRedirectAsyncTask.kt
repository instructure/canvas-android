/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
 */
package com.instructure.annotations

import android.os.AsyncTask
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.utils.ApiPrefs
import okhttp3.Request

/**
 * Given an appropriate canvadocs url (typically the preview_url from an attachment), this async
 * task will attempt return a redirect url which can be used to start a canvadocs session
 */
class CanvaDocsRedirectAsyncTask(
        private val canvaDocsUrl: String,
        private val onSuccess: (String) -> Unit,
        private val onException: (Throwable) -> Unit,
        private val domain: String? = null
) : AsyncTask<Unit, Unit, String>() {

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    @Throws(InterruptedException::class)
    override fun doInBackground(vararg params: Unit?): String {
        try {
            val client = CanvasRestAdapter.okHttpClient
                    .newBuilder()
                    .followRedirects(false)
                    .cache(null)
                    .build()

            val request = Request.Builder()
                    .url((domain ?: ApiPrefs.fullDomain) + canvaDocsUrl)
                    .build()

            val response = client.newCall(request).execute()

            val redirectUrl: String
            return if (response.isRedirect) {
                redirectUrl = response.header("Location") ?: ""

                // Let's parse out what we don't want
                redirectUrl.substringBefore("/view")
            } else ""
        } catch (e: Throwable) {
            onException(e)
            return ""
        }
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        onSuccess(result)
    }
}

/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.canvasapi2.utils

import android.annotation.TargetApi
import android.content.Context

import com.instructure.canvasapi2.models.ApiHttpResponse

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object HttpHelper {

    /**
     * externalHttpGet is a way to make  HTTPRequests to APIs other than the CanvasAPI.
     * The ENTIRE url must be specified including domain.
     *
     * @param context
     * @param getURL
     * @param includeAuthentication whether or not the should be authenticated using the CanvasToken saved.
     * @return
     */
    fun externalHttpGet(context: Context?, getURL: String, includeAuthentication: Boolean): ApiHttpResponse {
        var getURL = getURL
        // Explicit check for null.
        if (context == null) {
            return ApiHttpResponse()
        }

        try {
            getURL = MasqueradeHelper.addMasqueradeId(getURL)
            // Remove spaces from the URL
            getURL = getURL.replace(" ", "%20")

            val api_protocol = ApiPrefs.protocol
            // Make sure the URL begins with http(s)://
            if (!getURL.startsWith("https://") && !getURL.startsWith("http://")) {
                getURL = "$api_protocol://$getURL"
            }

            val urlConnection = URL(getURL).openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"

            if (includeAuthentication) {
                val token = ApiPrefs.getValidToken()
                if (token.isNotEmpty()) {
                    val headerValue = String.format("Bearer %s", token)
                    urlConnection.setRequestProperty("Authorization", headerValue)
                }
            }

            return parseLinkHeaderResponse(urlConnection)
        } catch (e: Exception) {
            Logger.e("Error externalHttpGet: " + e.message)
            return ApiHttpResponse()
        }

    }

    /**
     * redirectURL tries its best to follow http redirects until there are no more.
     *
     * @param urlConnection
     * @return
     */
    @TargetApi(9)
    fun redirectURL(urlConnection: HttpURLConnection): HttpURLConnection {
        var urlConnection = urlConnection
        HttpURLConnection.setFollowRedirects(true)
        try {
            urlConnection.connect()

            var currentURL: String
            do {
                urlConnection.responseCode
                currentURL = urlConnection.url.toString()
                urlConnection = URL(currentURL).openConnection() as HttpURLConnection
            } while (urlConnection.url.toString() != currentURL)
        } catch (E: Exception) {
        }

        return urlConnection

    }

    private fun parseLinkHeaderResponse(urlConnection: HttpURLConnection): ApiHttpResponse {
        var httpResponse = ApiHttpResponse()
        var inputStream: InputStream? = null
        try {
            httpResponse = httpResponse.copy(responseCode = urlConnection.responseCode)

            // Check if response is supposed to have a body
            if (httpResponse.responseCode != 204) {
                inputStream = urlConnection.inputStream
                val isReader = InputStreamReader(inputStream!!)
                val br = BufferedReader(isReader)
                val sb = StringBuilder()
                var inputLine = br.readLine()
                while (inputLine != null) {
                    sb.append(inputLine)
                    inputLine = br.readLine()
                }
                httpResponse = httpResponse.copy(responseBody = sb.toString())
            }

            httpResponse = httpResponse.copy(linkHeaders = APIHelper.parseLinkHeaderResponse(urlConnection.getHeaderField("link")))
        } catch (e: Exception) {
            Logger.e("Failed to get response: " + e.message)
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                Logger.e("Could not close input stream: " + e.message)
            }
        }

        return httpResponse
    }

}

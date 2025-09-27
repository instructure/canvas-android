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
 */

package com.instructure.pandautils.utils

import android.net.Uri
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import androidx.core.net.toUri
import com.instructure.canvasapi2.builders.RestParams
import okhttp3.Response

object RouteUtils {
    fun retrieveFileUrl(
        route: Route,
        fileId: String?,
        block: (url: String, canvasContext: CanvasContext, needsAuth: Boolean) -> Unit
    ) {
        var needsAuth = true
        var fileUrl = ApiPrefs.fullDomain
        var context = CanvasContext.currentUserContext(ApiPrefs.user!!)
        route.paramsHash[RouterParams.COURSE_ID]?.let {
            context = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, it.toLong())
            fileUrl += "/courses/$it"
        }
        fileUrl += "/files/$fileId/preview"
        route.queryParamsHash[RouterParams.VERIFIER]?.let {
            needsAuth = false
            fileUrl += "?verifier=$it"
        }

        block.invoke(fileUrl, context, needsAuth)
    }

    suspend fun getMediaUri(uri: Uri): Uri {
        var response: Response? = null
        val responseUri = withContext(Dispatchers.IO) {
            try {
                val client = CanvasRestAdapter.okHttpClient
                    .newBuilder()
                    .followRedirects(true)
                    .cache(null)
                    .build()

                val request = Request.Builder()
                    .head()
                    .url(uri.toString())
                    .tag(RestParams(disableFileVerifiers = false))
                    .build()

                response = client.newCall(request).execute()
                response.use {
                    var responseUrl = response.request.url.toString().toUri()
                    if (responseUrl.toString().isEmpty()) {
                        responseUrl = uri
                    }
                    val contentTypeHeader = response.header("content-type")
                    if (contentTypeHeader != null) {
                        if (contentTypeHeader.contains("dash") && !responseUrl.toString()
                                .endsWith(".mpd")
                        ) {
                            ("$responseUrl.mpd").toUri()
                        } else {
                            responseUrl
                        }
                    } else {
                        responseUrl
                    }
                }
            } catch (e: Exception) {
                response?.close()
                return@withContext uri
            }
        }
        response?.close()
        return responseUri
    }
}

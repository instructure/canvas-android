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

    suspend fun getRedirectUrl(uri: Uri): Uri {
        var response: Response? = null
        val responseUri = withContext(Dispatchers.IO) {
            try {
                val client = CanvasRestAdapter.okHttpClient
                    .newBuilder()
                    .followRedirects(false)
                    .cache(null)
                    .build()

                val request = Request.Builder()
                    .head()
                    .url(uri.toString())
                    .build()

                response = client.newCall(request).execute()
                response.use {
                    return@withContext if (response.isRedirect) {
                        val header = response.header("Location")
                        if (header != null) {
                            getRedirectUrl(header.toUri())
                        } else {
                            uri
                        }
                    } else {
                        val contentTypeHeader = response.header("content-type")
                        if (contentTypeHeader != null) {
                            if (contentTypeHeader.contains("dash") && !uri.toString().endsWith(".mpd")) {
                                ("$uri.mpd").toUri()
                            } else {
                                uri
                            }
                        } else {
                            uri
                        }
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

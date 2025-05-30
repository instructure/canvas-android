/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.moduleitemsequence.content.page

import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.apis.PageAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import javax.inject.Inject

class PageDetailsRepository @Inject constructor(
    private val pageApi: PageAPI.PagesInterface,
    private val oAuthInterface: OAuthAPI.OAuthInterface
) {
    suspend fun getPageDetails(courseId: Long, pageId: String, forceNetwork: Boolean = false): Page {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return pageApi.getDetailedPage(CanvasContext.Type.COURSE.apiString, courseId, pageId, params).dataOrThrow
    }

    suspend fun authenticateUrl(url: String): String {
        return oAuthInterface.getAuthenticatedSession(
            url,
            RestParams(isForceReadFromNetwork = true)
        ).dataOrNull?.sessionUrl ?: url
    }
}
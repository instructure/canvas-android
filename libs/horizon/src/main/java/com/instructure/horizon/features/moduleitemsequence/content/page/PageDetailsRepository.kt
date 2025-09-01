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

import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.apis.PageAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.RedwoodApiManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.horizon.features.notebook.common.model.Note
import com.instructure.horizon.features.notebook.common.model.mapToNotes
import com.instructure.redwood.type.LearningObjectFilter
import com.instructure.redwood.type.NoteFilterInput
import javax.inject.Inject

class PageDetailsRepository @Inject constructor(
    private val pageApi: PageAPI.PagesInterface,
    private val oAuthInterface: OAuthAPI.OAuthInterface,
    private val redwoodApi: RedwoodApiManager,
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

    suspend fun getNotes(courseId: Long, pageId: Long): List<Note> {
        return redwoodApi.getNotes(
            filter = NoteFilterInput(
                courseId = Optional.present(courseId.toString()),
                learningObject = Optional.present(LearningObjectFilter(
                    type = "Page",
                    id = pageId.toString()
                )),
            ),
            firstN = null,
            after = null,
        ).mapToNotes()
    }
}
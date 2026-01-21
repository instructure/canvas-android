/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.features.inbox.compose

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.apis.RecipientAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.pandautils.features.inbox.compose.InboxComposeRepository
import com.instructure.pandautils.utils.orDefault
import com.instructure.parentapp.util.ParentPrefs

class ParentInboxComposeRepository(
    private val courseAPI: CourseAPI.CoursesInterface,
    private val groupAPI: GroupAPI.GroupInterface,
    private val parentPrefs: ParentPrefs,
    private val featuresApi: FeaturesAPI.FeaturesInterface,
    recipientAPI: RecipientAPI.RecipientInterface,
    inboxAPI: InboxApi.InboxInterface,
    inboxSettingsManager: InboxSettingsManager
): InboxComposeRepository(courseAPI, groupAPI, recipientAPI, inboxAPI, inboxSettingsManager) {

    override suspend fun getCourses(forceRefresh: Boolean): DataResult<List<Course>> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceRefresh)

        val coursesResult = courseAPI.firstPageObserveeCourses(params)
            .depaginate { nextUrl -> courseAPI.next(nextUrl, params) }

        val currentStudent = parentPrefs.currentStudent

        val courses = coursesResult.dataOrNull ?: return coursesResult

        val validCourses = courses.filter { it.isObserver && it.enrollments?.any { enrollment -> enrollment.userId == currentStudent?.id }.orDefault() }

        return DataResult.Success(validCourses)
    }

    override suspend fun getGroups(forceRefresh: Boolean): DataResult<List<Group>> {
        return DataResult.Success(emptyList())
    }

    override suspend fun isInboxSignatureFeatureEnabled(): Boolean {
        return featuresApi.getAccountSettingsFeatures(RestParams()).dataOrNull?.enableInboxSignatureBlock.orDefault()
    }
}
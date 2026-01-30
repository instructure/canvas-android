/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.pandautils.features.lti

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.LaunchDefinitionsAPI
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.utils.ApiPrefs

class LtiLaunchRepository(
    private val launchDefinitionsApi: LaunchDefinitionsAPI.LaunchDefinitionsInterface,
    private val assignmentApi: AssignmentAPI.AssignmentInterface,
    private val oAuthInterface: OAuthAPI.OAuthInterface,
    private val apiPrefs: ApiPrefs
) {
    suspend fun getLtiFromAuthenticationUrl(url: String, ltiTool: LTITool?): LTITool {
        return ltiTool?.let {
            val courseId = if (ltiTool.courseId == 0L) ltiTool.contextId ?: 0L else ltiTool.courseId
            val params = RestParams(isForceReadFromNetwork = true, domain = apiPrefs.overrideDomains[courseId])
            assignmentApi.getExternalToolLaunchUrl(courseId, ltiTool.id, ltiTool.assignmentId, restParams = params).dataOrNull
        } ?: launchDefinitionsApi.getLtiFromAuthenticationUrl(url, RestParams(isForceReadFromNetwork = true)).dataOrThrow
    }

    suspend fun authenticateUrl(url: String): String {
        return oAuthInterface.getAuthenticatedSession(
            url,
            RestParams(isForceReadFromNetwork = true)
        ).dataOrNull?.sessionUrl ?: url
    }
}
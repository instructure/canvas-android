/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
package com.instructure.student.features.discussion.list

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.isCourse
import com.instructure.student.features.discussion.list.datasource.DiscussionListDataSource
import com.instructure.student.features.discussion.list.datasource.DiscussionListLocalDataSource
import com.instructure.student.features.discussion.list.datasource.DiscussionListNetworkDataSource

class DiscussionListRepository(localDataSource: DiscussionListLocalDataSource,
                               networkDataSource: DiscussionListNetworkDataSource,
                               networkStateProvider: NetworkStateProvider,
                               featureFlagProvider: FeatureFlagProvider
) : Repository<DiscussionListDataSource>(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider) {

    suspend fun getCreationPermission(canvasContext: CanvasContext, isAnnouncements: Boolean): Boolean {
        val permissions = if (canvasContext.isCourse) {
            dataSource().getPermissionsForCourse(canvasContext as Course)
        } else {
            dataSource().getPermissionsForGroup(canvasContext as Group)
        }

        return if (isAnnouncements) {
            permissions?.canCreateAnnouncement ?: false
        } else {
            permissions?.canCreateDiscussionTopic ?: false
        }
    }

    suspend fun getDiscussionTopicHeaders(canvasContext: CanvasContext, isAnnouncements: Boolean, forceNetwork: Boolean): List<DiscussionTopicHeader> {
        return if (isAnnouncements) {
            dataSource().getAnnouncements(canvasContext, forceNetwork)
        } else {
            dataSource().getDiscussions(canvasContext, forceNetwork)
        }
    }
}
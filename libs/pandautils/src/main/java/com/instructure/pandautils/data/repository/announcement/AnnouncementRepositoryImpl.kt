/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.data.repository.announcement

import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider

class AnnouncementRepositoryImpl(
    localDataSource: AnnouncementLocalDataSource,
    networkDataSource: AnnouncementNetworkDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider
) : Repository<AnnouncementDataSource>(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider),
    AnnouncementRepository {

    override suspend fun getCourseAnnouncements(courseId: Long, forceRefresh: Boolean): DataResult<List<DiscussionTopicHeader>> {
        return dataSource().getCourseAnnouncements(courseId, forceRefresh)
    }
}
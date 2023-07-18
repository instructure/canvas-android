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
package com.instructure.student.di.feature

import com.instructure.canvasapi2.apis.AnnouncementAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.DiscussionAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.pandautils.room.offline.facade.DiscussionTopicHeaderFacade
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.discussion.list.DiscussionListRepository
import com.instructure.student.features.discussion.list.datasource.DiscussionListLocalDataSource
import com.instructure.student.features.discussion.list.datasource.DiscussionListNetworkDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class DiscussionListModule {

    @Provides
    fun provideDiscussionListLocalDataSource(discussionTopicHeaderFacade: DiscussionTopicHeaderFacade): DiscussionListLocalDataSource {
        return DiscussionListLocalDataSource(discussionTopicHeaderFacade)
    }

    @Provides
    fun provideDiscussionListNetworkDataSource(
        courseApi: CourseAPI.CoursesInterface,
        groupApi: GroupAPI.GroupInterface,
        discussionApi: DiscussionAPI.DiscussionInterface,
        announcementApi: AnnouncementAPI.AnnouncementInterface
    ): DiscussionListNetworkDataSource {
        return DiscussionListNetworkDataSource(courseApi, groupApi, discussionApi, announcementApi)
    }

    @Provides
    fun provideDiscussionListRepository(
        localDataSource: DiscussionListLocalDataSource,
        networkDataSource: DiscussionListNetworkDataSource,
        networkStateProvider: NetworkStateProvider,
        featureFlagProvider: FeatureFlagProvider
    ): DiscussionListRepository {
        return DiscussionListRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)
    }
}
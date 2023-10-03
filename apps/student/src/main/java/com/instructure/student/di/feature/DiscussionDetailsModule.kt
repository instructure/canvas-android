package com.instructure.student.di.feature

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.DiscussionAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.pandautils.room.offline.daos.CourseSettingsDao
import com.instructure.pandautils.room.offline.facade.DiscussionTopicFacade
import com.instructure.pandautils.room.offline.facade.DiscussionTopicHeaderFacade
import com.instructure.pandautils.room.offline.facade.GroupFacade
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.discussion.details.DiscussionDetailsRepository
import com.instructure.student.features.discussion.details.datasource.DiscussionDetailsLocalDataSource
import com.instructure.student.features.discussion.details.datasource.DiscussionDetailsNetworkDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class DiscussionDetailsModule {
    @Provides
    fun provideDiscussionDetailsLocalDataSource(
        discussionTopicHeaderFacade: DiscussionTopicHeaderFacade,
        discussionTopicFacade: DiscussionTopicFacade,
        courseSettingsDao: CourseSettingsDao,
        groupFacade: GroupFacade,
    ): DiscussionDetailsLocalDataSource {
        return DiscussionDetailsLocalDataSource(discussionTopicHeaderFacade, discussionTopicFacade, courseSettingsDao, groupFacade)
    }

    @Provides
    fun provideDiscussionDetailsNetworkDataSource(
        discussionApi: DiscussionAPI.DiscussionInterface,
        oAuthApi: OAuthAPI.OAuthInterface,
        courseApi: CourseAPI.CoursesInterface,
        groupApi: GroupAPI.GroupInterface,
    ): DiscussionDetailsNetworkDataSource {
        return DiscussionDetailsNetworkDataSource(discussionApi, oAuthApi, courseApi, groupApi)
    }

    @Provides
    fun provideDiscussionDetailsRepository(
        localDataSource: DiscussionDetailsLocalDataSource,
        networkDataSource: DiscussionDetailsNetworkDataSource,
        networkStateProvider: NetworkStateProvider,
        featureFlagProvider: FeatureFlagProvider
    ): DiscussionDetailsRepository {
        return DiscussionDetailsRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)
    }
}
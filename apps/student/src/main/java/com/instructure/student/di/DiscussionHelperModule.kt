package com.instructure.student.di

import com.instructure.canvasapi2.apis.DiscussionAPI
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperRepository
import com.instructure.pandautils.room.offline.facade.DiscussionTopicHeaderFacade
import com.instructure.pandautils.room.offline.facade.GroupFacade
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.discussion.routing.DiscussionRouteHelperStudentRepository
import com.instructure.student.features.discussion.routing.datasource.DiscussionRouteHelperStudentLocalDataSource
import com.instructure.student.features.discussion.routing.datasource.DiscussionRouteHelperStudentNetworkDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class DiscussionHelperModule {
    @Provides
    fun provideDiscussionRouteHelperStudentLocalDataSource(
        discussionTopicHeaderFacade: DiscussionTopicHeaderFacade,
        groupFacade: GroupFacade
    ): DiscussionRouteHelperStudentLocalDataSource {
        return DiscussionRouteHelperStudentLocalDataSource(discussionTopicHeaderFacade, groupFacade)
    }

    @Provides
    fun provideDiscussionRouteHelperStudentNetworkDataSource(
        discussionApi: DiscussionAPI.DiscussionInterface,
        groupApi: GroupAPI.GroupInterface,
        featuresApi: FeaturesAPI.FeaturesInterface,
        featureFlagProvider: FeatureFlagProvider
    ): DiscussionRouteHelperStudentNetworkDataSource {
        return DiscussionRouteHelperStudentNetworkDataSource(discussionApi, groupApi, featuresApi, featureFlagProvider)
    }

    @Provides
    fun provideDiscussionRouteHelperStudentRepository(
        localDataSource: DiscussionRouteHelperStudentLocalDataSource,
        networkDataSource: DiscussionRouteHelperStudentNetworkDataSource,
        networkStateProvider: NetworkStateProvider,
        featureFlagProvider: FeatureFlagProvider
    ): DiscussionRouteHelperRepository {
        return DiscussionRouteHelperStudentRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)
    }
}
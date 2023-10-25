package com.instructure.pandautils.di

import com.instructure.canvasapi2.apis.DiscussionAPI
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelper
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperLocalDataSource
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperNetworkDataSource
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperRepository
import com.instructure.pandautils.room.offline.facade.DiscussionTopicHeaderFacade
import com.instructure.pandautils.room.offline.facade.GroupFacade
import com.instructure.pandautils.utils.FeatureFlagProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class DiscussionModule {

    @Provides
    fun provideDiscussionRouteHelper(
        discussionRouteHelperRepository: DiscussionRouteHelperRepository
    ): DiscussionRouteHelper {
        return DiscussionRouteHelper(discussionRouteHelperRepository)
    }

    @Provides
    fun provideDiscussionRouteHelperNetworkDataSource(
        discussionApi: DiscussionAPI.DiscussionInterface,
        groupApi: GroupAPI.GroupInterface,
        featuresApi: FeaturesAPI.FeaturesInterface,
        featureFlagProvider: FeatureFlagProvider
    ): DiscussionRouteHelperNetworkDataSource {
        return DiscussionRouteHelperNetworkDataSource(discussionApi, groupApi, featuresApi, featureFlagProvider)
    }

    @Provides
    fun provideDiscussionRouteHelperLocalDataSource(
        discussionTopicHeaderFacade: DiscussionTopicHeaderFacade,
        groupFacade: GroupFacade
    ): DiscussionRouteHelperLocalDataSource {
        return DiscussionRouteHelperLocalDataSource(discussionTopicHeaderFacade, groupFacade)
    }
}
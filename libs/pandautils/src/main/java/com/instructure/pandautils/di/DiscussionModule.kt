package com.instructure.pandautils.di

import com.instructure.canvasapi2.managers.DiscussionManager
import com.instructure.canvasapi2.managers.FeaturesManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelper
import com.instructure.pandautils.room.offline.facade.DiscussionTopicHeaderFacade
import com.instructure.pandautils.room.offline.facade.GroupFacade
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class DiscussionModule {

    @Provides
    fun provideDiscussionRouteHelper(
        featuresManager: FeaturesManager,
        featureFlagProvider: FeatureFlagProvider,
        discussionManager: DiscussionManager,
        groupManager: GroupManager,
        groupFacade: GroupFacade,
        networkStateProvider: NetworkStateProvider,
        discussionTopicHeaderFacade: DiscussionTopicHeaderFacade,
    ): DiscussionRouteHelper {
        return DiscussionRouteHelper(featuresManager, featureFlagProvider, discussionManager, groupManager, groupFacade, networkStateProvider, discussionTopicHeaderFacade)
    }
}
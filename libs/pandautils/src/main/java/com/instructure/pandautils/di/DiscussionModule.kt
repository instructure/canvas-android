package com.instructure.pandautils.di

import com.instructure.canvasapi2.apis.DiscussionAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.pandautils.features.discussion.DiscussionSharedEvents
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelper
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperLocalDataSource
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperNetworkDataSource
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperRepository
import com.instructure.pandautils.room.offline.facade.DiscussionTopicHeaderFacade
import com.instructure.pandautils.room.offline.facade.GroupFacade
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
    ): DiscussionRouteHelperNetworkDataSource {
        return DiscussionRouteHelperNetworkDataSource(discussionApi, groupApi)
    }

    @Provides
    fun provideDiscussionRouteHelperLocalDataSource(
        discussionTopicHeaderFacade: DiscussionTopicHeaderFacade,
        groupFacade: GroupFacade
    ): DiscussionRouteHelperLocalDataSource {
        return DiscussionRouteHelperLocalDataSource(discussionTopicHeaderFacade, groupFacade)
    }

    @Provides
    @Singleton
    fun provideDiscussionSharedEvents(): DiscussionSharedEvents {
        return DiscussionSharedEvents()
    }
}
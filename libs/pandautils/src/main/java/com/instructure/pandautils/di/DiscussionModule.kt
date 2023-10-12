package com.instructure.pandautils.di

import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelper
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperRepository
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
}
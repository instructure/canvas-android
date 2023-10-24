package com.instructure.teacher.di

import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperNetworkDataSource
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperRepository
import com.instructure.teacher.features.discussion.routing.DiscussionRouteHelperTeacherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class DiscussionHelperModule {

    @Provides
    fun provideDiscussionRouteHelperTeacherRepository(
        networkDataSource: DiscussionRouteHelperNetworkDataSource
    ): DiscussionRouteHelperRepository {
        return DiscussionRouteHelperTeacherRepository(networkDataSource)
    }
}
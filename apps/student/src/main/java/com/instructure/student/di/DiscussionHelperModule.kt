package com.instructure.student.di

import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperLocalDataSource
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperNetworkDataSource
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperRepository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.discussion.routing.DiscussionRouteHelperStudentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class DiscussionHelperModule {

    @Provides
    fun provideDiscussionRouteHelperStudentRepository(
        localDataSource: DiscussionRouteHelperLocalDataSource,
        networkDataSource: DiscussionRouteHelperNetworkDataSource,
        networkStateProvider: NetworkStateProvider,
        featureFlagProvider: FeatureFlagProvider
    ): DiscussionRouteHelperRepository {
        return DiscussionRouteHelperStudentRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)
    }
}
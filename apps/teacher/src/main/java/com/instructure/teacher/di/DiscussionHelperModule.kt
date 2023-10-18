package com.instructure.teacher.di

import com.instructure.canvasapi2.apis.DiscussionAPI
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperRepository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.teacher.features.discussion.routing.DiscussionRouteHelperTeacherRepository
import com.instructure.teacher.features.discussion.routing.datasource.DiscussionRouteHelperTeacherNetworkDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class DiscussionHelperModule {
    @Provides
    fun provideDiscussionRouteHelperTeacherNetworkDataSource(
        discussionApi: DiscussionAPI.DiscussionInterface,
        groupApi: GroupAPI.GroupInterface,
        featuresApi: FeaturesAPI.FeaturesInterface,
        featureFlagProvider: FeatureFlagProvider
    ): DiscussionRouteHelperTeacherNetworkDataSource {
        return DiscussionRouteHelperTeacherNetworkDataSource(discussionApi, groupApi, featuresApi, featureFlagProvider)
    }

    @Provides
    fun provideDiscussionRouteHelperTeacherRepository(
        networkDataSource: DiscussionRouteHelperTeacherNetworkDataSource
    ): DiscussionRouteHelperRepository {
        return DiscussionRouteHelperTeacherRepository(networkDataSource)
    }
}
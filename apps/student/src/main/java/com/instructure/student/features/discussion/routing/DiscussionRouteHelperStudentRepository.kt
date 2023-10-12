package com.instructure.student.features.discussion.routing

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperRepository
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.discussion.routing.datasource.DiscussionRouteHelperStudentDataSource
import com.instructure.student.features.discussion.routing.datasource.DiscussionRouteHelperStudentLocalDataSource
import com.instructure.student.features.discussion.routing.datasource.DiscussionRouteHelperStudentNetworkDataSource

class DiscussionRouteHelperStudentRepository(
    localDataSource: DiscussionRouteHelperStudentLocalDataSource,
    private val networkDataSource: DiscussionRouteHelperStudentNetworkDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider
) : DiscussionRouteHelperRepository, Repository<DiscussionRouteHelperStudentDataSource>(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider) {
    override suspend fun getEnabledFeaturesForCourse(
        canvasContext: CanvasContext,
        forceNetwork: Boolean
    ): Boolean {
        return networkDataSource.getEnabledFeaturesForCourse(canvasContext, forceNetwork)
    }

    override suspend fun getDiscussionTopicHeader(
        canvasContext: CanvasContext,
        discussionTopicHeaderId: Long,
        forceNetwork: Boolean
    ): DiscussionTopicHeader? {
        return dataSource().getDiscussionTopicHeader(canvasContext, discussionTopicHeaderId, forceNetwork)
    }

    override suspend fun getAllGroups(discussionTopicHeader: DiscussionTopicHeader, forceNetwork: Boolean): Pair<Group, Long>? {
        return dataSource().getAllGroups(discussionTopicHeader, forceNetwork)
    }
}
package com.instructure.student.features.discussion.routing

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperDataSource
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperLocalDataSource
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperNetworkDataSource
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperRepository
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider

class DiscussionRouteHelperStudentRepository(
    localDataSource: DiscussionRouteHelperLocalDataSource,
    private val networkDataSource: DiscussionRouteHelperNetworkDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider
) : DiscussionRouteHelperRepository, Repository<DiscussionRouteHelperDataSource>(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider) {
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

    override suspend fun getAllGroups(discussionTopicHeader: DiscussionTopicHeader, userId: Long, forceNetwork: Boolean): List<Group> {
        return dataSource().getAllGroups(discussionTopicHeader, userId, forceNetwork)
    }
}
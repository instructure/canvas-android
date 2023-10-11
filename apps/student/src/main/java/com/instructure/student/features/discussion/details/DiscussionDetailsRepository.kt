package com.instructure.student.features.discussion.details

import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.discussion.details.datasource.DiscussionDetailsDataSource
import com.instructure.student.features.discussion.details.datasource.DiscussionDetailsLocalDataSource
import com.instructure.student.features.discussion.details.datasource.DiscussionDetailsNetworkDataSource

class DiscussionDetailsRepository(localDataSource: DiscussionDetailsLocalDataSource,
                                  private val networkDataSource: DiscussionDetailsNetworkDataSource,
                                  networkStateProvider: NetworkStateProvider,
                                  featureFlagProvider: FeatureFlagProvider
) : Repository<DiscussionDetailsDataSource>(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider) {

    suspend fun markAsRead(canvasContext: CanvasContext, discussionTopicHeaderId: Long, discussionEntryIds: List<Long>): List<Long> {
        val successfullyMarkedAsReadIds: MutableList<Long> = mutableListOf()
        discussionEntryIds.forEach { entryId ->
            val result = networkDataSource.markAsRead(canvasContext, discussionTopicHeaderId, entryId)
            //TODO: Only successful results should be added to the list, but currently the API returns DataResult.Failed for all requests
            successfullyMarkedAsReadIds.add(entryId)
        }
        return successfullyMarkedAsReadIds
    }

    suspend fun deleteDiscussionEntry(canvasContext: CanvasContext, discussionTopicHeaderId: Long, entryId: Long): Void? {
        return networkDataSource.deleteDiscussionEntry(canvasContext, discussionTopicHeaderId, entryId).dataOrNull
    }

    suspend fun rateDiscussionEntry(canvasContext: CanvasContext, discussionTopicHeaderId: Long, discussionEntryId: Long, rating: Int): Void? {
        return networkDataSource.rateDiscussionEntry(canvasContext, discussionTopicHeaderId, discussionEntryId, rating).dataOrNull
    }

    suspend fun getAuthenticatedSession(url: String): AuthenticatedSession? {
        return networkDataSource.getAuthenticatedSession(url).dataOrNull
    }

    suspend fun getCourseSettings(courseId: Long, forceRefresh: Boolean): CourseSettings? {
        return dataSource().getCourseSettings(courseId, forceRefresh).dataOrNull
    }

    suspend fun getDetailedDiscussion(canvasContext: CanvasContext, discussionTopicHeaderId: Long, forceNetwork: Boolean): DiscussionTopicHeader {
        return dataSource().getDetailedDiscussion(canvasContext, discussionTopicHeaderId, forceNetwork).dataOrThrow
    }

    suspend fun getAllGroups(userId: Long, forceNetwork: Boolean): List<Group> {
        return dataSource().getFirstPageGroups(userId, forceNetwork).depaginate { nextUrl -> dataSource().getNextPageGroups(nextUrl, forceNetwork) }.dataOrNull.orEmpty()
    }

    suspend fun getFullDiscussionTopic(canvasContext: CanvasContext, topicId: Long, forceNetwork: Boolean): DiscussionTopic {
        return dataSource().getFullDiscussionTopic(canvasContext, topicId, forceNetwork).dataOrThrow
    }
}
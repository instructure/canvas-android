package com.instructure.student.features.discussion.details

import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.discussion.details.datasource.DiscussionDetailsDataSource
import com.instructure.student.features.discussion.details.datasource.DiscussionDetailsLocalDataSource
import com.instructure.student.features.discussion.details.datasource.DiscussionDetailsNetworkDataSource

class DiscussionDetailsRepository(localDataSource: DiscussionDetailsLocalDataSource,
                                  networkDataSource: DiscussionDetailsNetworkDataSource,
                                  networkStateProvider: NetworkStateProvider,
                                  featureFlagProvider: FeatureFlagProvider
) : Repository<DiscussionDetailsDataSource>(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider) {

    suspend fun markAsRead(canvasContext: CanvasContext, discussionTopicHeaderId: Long, discussionEntryIds: List<Long>): List<Long> {
        val successfullyMarkedAsReadIds: MutableList<Long> = ArrayList(discussionEntryIds.size)
        discussionEntryIds.forEach { entryId ->
            val result = dataSource().markAsRead(canvasContext, discussionTopicHeaderId, entryId)
            if (result is DataResult.Success){ successfullyMarkedAsReadIds.add(entryId) }
        }
        return successfullyMarkedAsReadIds
    }

    suspend fun deleteDiscussionEntry(canvasContext: CanvasContext, discussionTopicHeaderId: Long, entryId: Long) {
        dataSource().deleteDiscussionEntry(canvasContext, discussionTopicHeaderId, entryId).dataOrThrow
    }

    suspend fun rateDiscussionEntry(canvasContext: CanvasContext, discussionTopicHeaderId: Long, discussionEntryId: Long, rating: Int) {
        dataSource().rateDiscussionEntry(canvasContext, discussionTopicHeaderId, discussionEntryId, rating).dataOrThrow
    }

    suspend fun getAuthenticatedSession(url: String): AuthenticatedSession {
        return dataSource().getAuthenticatedSession(url).dataOrThrow
    }

    suspend fun getCourseSettings(courseId: Long, forceRefresh: Boolean): CourseSettings? {
        return dataSource().getCourseSettings(courseId, forceRefresh).dataOrNull
    }

    suspend fun getDetailedDiscussion(canvasContext: CanvasContext, discussionTopicHeaderId: Long, forceNetwork: Boolean): DiscussionTopicHeader {
        return dataSource().getDetailedDiscussion(canvasContext, discussionTopicHeaderId, forceNetwork).dataOrThrow
    }

    suspend fun getAllGroups(forceNetwork: Boolean): List<Group> {
        return dataSource().getFirstPageGroups(forceNetwork).depaginate { nextUrl -> dataSource().getNextPageGroups(nextUrl, forceNetwork) }.dataOrThrow
    }

    suspend fun getFullDiscussionTopic(canvasContext: CanvasContext, topicId: Long, forceNetwork: Boolean): DiscussionTopic {
        return dataSource().getFullDiscussionTopic(canvasContext, topicId, forceNetwork).dataOrThrow
    }
}
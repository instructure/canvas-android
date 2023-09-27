package com.instructure.student.features.discussion.details.datasource

import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.DataResult

class DiscussionDetailsLocalDataSource : DiscussionDetailsDataSource {
    override suspend fun markAsRead(
        canvasContext: CanvasContext,
        discussionTopicHeaderId: Long,
        discussionEntryId: Long
    ): DataResult<Void> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteDiscussionEntry(
        canvasContext: CanvasContext,
        discussionTopicHeaderId: Long,
        entryId: Long
    ): DataResult<Void> {
        TODO("Not yet implemented")
    }

    override suspend fun rateDiscussionEntry(
        canvasContext: CanvasContext,
        discussionTopicHeaderId: Long,
        discussionEntryId: Long,
        rating: Int
    ): DataResult<Void> {
        TODO("Not yet implemented")
    }

    override suspend fun getAuthenticatedSession(url: String): DataResult<AuthenticatedSession> {
        TODO("Not yet implemented")
    }

    override suspend fun getCourseSettings(
        courseId: Long,
        forceRefresh: Boolean
    ): DataResult<CourseSettings> {
        TODO("Not yet implemented")
    }

    override suspend fun getDetailedDiscussion(
        canvasContext: CanvasContext,
        discussionTopicHeaderId: Long,
        forceNetwork: Boolean
    ): DataResult<DiscussionTopicHeader> {
        TODO("Not yet implemented")
    }

    override suspend fun getFirstPageGroups(forceNetwork: Boolean): DataResult<List<Group>> {
        TODO("Not yet implemented")
    }

    override suspend fun getNextPageGroups(
        nextUrl: String,
        forceNetwork: Boolean
    ): DataResult<List<Group>> {
        TODO("Not yet implemented")
    }

    override suspend fun getFullDiscussionTopic(
        canvasContext: CanvasContext,
        topicId: Long,
        forceNetwork: Boolean
    ): DataResult<DiscussionTopic> {
        TODO("Not yet implemented")
    }
}
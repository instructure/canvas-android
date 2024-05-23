package com.instructure.student.features.discussion.details.datasource

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.DiscussionAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.DataResult

class DiscussionDetailsNetworkDataSource(
    private val discussionApi: DiscussionAPI.DiscussionInterface,
    private val oAuthApi: OAuthAPI.OAuthInterface,
    private val courseApi: CourseAPI.CoursesInterface,
    private val groupApi: GroupAPI.GroupInterface,
) : DiscussionDetailsDataSource {
    suspend fun markAsRead(
        canvasContext: CanvasContext,
        discussionTopicHeaderId: Long,
        discussionEntryId: Long
    ): DataResult<Unit> {
        val params = RestParams(isForceReadFromNetwork = true)
        return discussionApi.markDiscussionTopicEntryRead(canvasContext.apiContext(), canvasContext.id, discussionTopicHeaderId, discussionEntryId, params)
    }

    suspend fun deleteDiscussionEntry(canvasContext: CanvasContext, discussionTopicHeaderId: Long, entryId: Long): DataResult<Unit> {
        val params = RestParams(isForceReadFromNetwork = true)
        return discussionApi.deleteDiscussionEntry(canvasContext.apiContext(), canvasContext.id, discussionTopicHeaderId, entryId, params)
    }

    suspend fun rateDiscussionEntry(
        canvasContext: CanvasContext,
        discussionTopicHeaderId: Long,
        discussionEntryId: Long,
        rating: Int
    ): DataResult<Unit> {
        val params = RestParams(isForceReadFromNetwork = true)
        return discussionApi.rateDiscussionEntry(canvasContext.apiContext(), canvasContext.id, discussionTopicHeaderId, discussionEntryId, rating, params)
    }

    suspend fun getAuthenticatedSession(url: String): DataResult<AuthenticatedSession?> {
        val params = RestParams(isForceReadFromNetwork = true)
        return oAuthApi.getAuthenticatedSession(url, params)
    }

    override suspend fun getCourseSettings(
        courseId: Long,
        forceRefresh: Boolean
    ): DataResult<CourseSettings?> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return courseApi.getCourseSettings(courseId, params)
    }

    override suspend fun getDetailedDiscussion(
        canvasContext: CanvasContext,
        discussionTopicHeaderId: Long,
        forceNetwork: Boolean
    ): DataResult<DiscussionTopicHeader?> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return discussionApi.getDetailedDiscussion(canvasContext.apiContext(), canvasContext.id, discussionTopicHeaderId, params)
    }

    override suspend fun getFirstPageGroups(userId: Long, forceNetwork: Boolean): DataResult<List<Group>> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return groupApi.getFirstPageGroups(params)
    }

    override suspend fun getNextPageGroups(
        nextUrl: String,
        forceNetwork: Boolean
    ): DataResult<List<Group>> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return groupApi.getNextPageGroups(nextUrl, params)
    }

    override suspend fun getFullDiscussionTopic(
        canvasContext: CanvasContext,
        topicId: Long,
        forceNetwork: Boolean
    ): DataResult<DiscussionTopic?> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return discussionApi.getFullDiscussionTopic(canvasContext.apiContext(), canvasContext.id, topicId, 1, params)
    }
}
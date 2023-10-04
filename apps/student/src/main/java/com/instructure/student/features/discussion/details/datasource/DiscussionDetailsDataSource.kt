package com.instructure.student.features.discussion.details.datasource

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.DataResult

interface DiscussionDetailsDataSource {
    suspend fun getCourseSettings(courseId: Long, forceRefresh: Boolean): DataResult<CourseSettings?>

    suspend fun getDetailedDiscussion(canvasContext: CanvasContext, discussionTopicHeaderId: Long, forceNetwork: Boolean): DataResult<DiscussionTopicHeader?>

    suspend fun getFirstPageGroups(userId: Long, forceNetwork: Boolean): DataResult<List<Group>>

    suspend fun getNextPageGroups(nextUrl: String, forceNetwork: Boolean): DataResult<List<Group>>

    suspend fun getFullDiscussionTopic(canvasContext: CanvasContext, topicId: Long, forceNetwork: Boolean): DataResult<DiscussionTopic?>
}
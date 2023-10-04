package com.instructure.student.features.discussion.details.datasource

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.daos.CourseSettingsDao
import com.instructure.pandautils.room.offline.facade.DiscussionTopicFacade
import com.instructure.pandautils.room.offline.facade.DiscussionTopicHeaderFacade
import com.instructure.pandautils.room.offline.facade.GroupFacade

class DiscussionDetailsLocalDataSource(
    private val discussionTopicHeaderFacade: DiscussionTopicHeaderFacade,
    private val discussionTopicFacade: DiscussionTopicFacade,
    private val courseSettingsDao: CourseSettingsDao,
    private val groupFacade: GroupFacade,
) : DiscussionDetailsDataSource {

    override suspend fun getCourseSettings(
        courseId: Long,
        forceRefresh: Boolean
    ): DataResult<CourseSettings?> {
        return DataResult.Success(courseSettingsDao.findByCourseId(courseId)?.toApiModel())
    }

    override suspend fun getDetailedDiscussion(
        canvasContext: CanvasContext,
        discussionTopicHeaderId: Long,
        forceNetwork: Boolean
    ): DataResult<DiscussionTopicHeader?> {
        return DataResult.Success(discussionTopicHeaderFacade.getDiscussionTopicHeaderById(discussionTopicHeaderId))
    }

    override suspend fun getFirstPageGroups(userId: Long, forceNetwork: Boolean): DataResult<List<Group>> {
        return DataResult.Success(groupFacade.getGroupsByUserId(userId))
    }

    override suspend fun getNextPageGroups(
        nextUrl: String,
        forceNetwork: Boolean
    ): DataResult<List<Group>> {
        return DataResult.Success(emptyList())
    }

    override suspend fun getFullDiscussionTopic(
        canvasContext: CanvasContext,
        topicId: Long,
        forceNetwork: Boolean
    ): DataResult<DiscussionTopic?> {
        return DataResult.Success(discussionTopicFacade.getDiscussionTopic(topicId))
    }
}
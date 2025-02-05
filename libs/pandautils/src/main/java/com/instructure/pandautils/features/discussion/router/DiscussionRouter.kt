package com.instructure.pandautils.features.discussion.router

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group

interface DiscussionRouter {

    fun routeToDiscussion(canvasContext: CanvasContext, isRedesign: Boolean, discussionTopicHeader: DiscussionTopicHeader, isAnnouncement: Boolean)

    fun routeToGroupDiscussion(group: Group, id: Long, header: DiscussionTopicHeader, isRedesign: Boolean)

    fun routeToNativeSpeedGrader(courseId: Long, assignmentId: Long, submissionIds: List<Long>, selectedIdx: Int, anonymousGrading: Boolean?, discussionTopicEntryId: Long?)
}
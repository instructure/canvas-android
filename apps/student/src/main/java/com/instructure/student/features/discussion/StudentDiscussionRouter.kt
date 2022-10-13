package com.instructure.student.features.discussion

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.pandautils.features.discussion.router.DiscussionRouter

class StudentDiscussionRouter : DiscussionRouter {
    override fun routeToDiscussion(
        canvasContext: CanvasContext,
        isRedesign: Boolean,
        isAnnouncement: Boolean,
        discussionTopicHeader: DiscussionTopicHeader
    ) {

    }
}
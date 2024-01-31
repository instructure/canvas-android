package com.instructure.teacher.features.calendar

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.features.calendar.CalendarRouter

class TeacherCalendarRouter(val activity: FragmentActivity) : CalendarRouter {
    override fun openNavigationDrawer() {

    }

    override fun openAssignment(canvasContext: CanvasContext, assignmentId: Long) {
        TODO("Not yet implemented")
    }

    override fun openDiscussion(canvasContext: CanvasContext, discussionId: Long) {
        TODO("Not yet implemented")
    }

    override fun openQuiz(canvasContext: CanvasContext, htmlUrl: String) {
        TODO("Not yet implemented")
    }

    override fun openCalendarEvent(canvasContext: CanvasContext, eventId: Long) {
        TODO("Not yet implemented")
    }
}
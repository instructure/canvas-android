package com.instructure.pandautils.features.calendar

import com.instructure.canvasapi2.models.CanvasContext

interface CalendarRouter {

    fun openNavigationDrawer()

    fun openAssignment(canvasContext: CanvasContext, assignmentId: Long)

    fun openDiscussion(canvasContext: CanvasContext, discussionId: Long)

    fun openQuiz(canvasContext: CanvasContext, htmlUrl: String)

    fun openCalendarEvent(canvasContext: CanvasContext, eventId: Long)
}
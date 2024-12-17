/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.features.calendar

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.PlannerItem

interface CalendarRouter {

    fun openNavigationDrawer()

    fun openAssignment(canvasContext: CanvasContext, assignmentId: Long)

    fun openDiscussion(canvasContext: CanvasContext, discussionId: Long, assignmentId: Long?)

    fun openQuiz(canvasContext: CanvasContext, htmlUrl: String)

    fun openCalendarEvent(canvasContext: CanvasContext, eventId: Long)

    fun openToDo(plannerItem: PlannerItem)

    fun openCreateToDo(initialDateString: String?)

    fun openCreateEvent(initialDateString: String?)

    fun attachNavigationDrawer()
}
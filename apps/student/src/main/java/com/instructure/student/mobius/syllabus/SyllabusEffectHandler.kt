/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.mobius.syllabus

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.managers.CalendarEventManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.student.mobius.common.ui.EffectHandler
import com.instructure.student.mobius.syllabus.ui.SyllabusView
import kotlinx.coroutines.launch

class SyllabusEffectHandler : EffectHandler<SyllabusView, SyllabusEvent, SyllabusEffect>() {
    override fun accept(effect: SyllabusEffect) {
        when (effect) {
            is SyllabusEffect.LoadData -> loadData(effect)
            is SyllabusEffect.ShowAssignmentView -> view?.showAssignmentView(effect.assignmentId, effect.course)
            is SyllabusEffect.ShowScheduleItemView -> view?.showScheduleItemView(effect.scheduleItem, effect.course)
        }.exhaustive
    }

    private fun loadData(effect: SyllabusEffect.LoadData) {
        launch {

            val course = CourseManager.getCourseWithSyllabusAsync(effect.courseId, effect.forceNetwork).await()

            if (course.isFail) {
                consumer.accept(SyllabusEvent.DataLoaded(course, DataResult.Fail()))
                return@launch
            }

            val contextCodes = listOf(course.dataOrThrow.contextId)

            val assignmentsDeferred = CalendarEventManager.getCalendarEventsExhaustiveAsync(true, CalendarEventAPI.CalendarEventType.ASSIGNMENT, null, null, contextCodes, effect.forceNetwork)
            val calendarEventsDeferred = CalendarEventManager.getCalendarEventsExhaustiveAsync(true, CalendarEventAPI.CalendarEventType.CALENDAR, null, null, contextCodes, effect.forceNetwork)

            val assignments = assignmentsDeferred.await()
            val events = calendarEventsDeferred.await()
            val endList = mutableListOf<ScheduleItem>()

            assignments.map { endList.addAll(it) }
            events.map { endList.addAll(it) }

            endList.sort()

            consumer.accept(SyllabusEvent.DataLoaded(
                course,
                if (assignments.isFail && events.isFail) {
                    DataResult.Fail((assignments as? DataResult.Fail)?.failure)
                } else {
                    DataResult.Success(endList)
                }
            ))
        }
    }
}

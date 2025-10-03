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
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.student.mobius.common.ui.EffectHandler
import com.instructure.student.mobius.syllabus.ui.SyllabusView
import kotlinx.coroutines.launch

class SyllabusEffectHandler(private val repository: SyllabusRepository) : EffectHandler<SyllabusView, SyllabusEvent, SyllabusEffect>() {
    override fun accept(effect: SyllabusEffect) {
        when (effect) {
            is SyllabusEffect.LoadData -> loadData(effect)
            is SyllabusEffect.ShowAssignmentView -> view?.showAssignmentView(effect.assignment, effect.course)
            is SyllabusEffect.ShowScheduleItemView -> view?.showScheduleItemView(effect.scheduleItem, effect.course)
        }.exhaustive
    }

    private fun loadData(effect: SyllabusEffect.LoadData) {
        launch {

            val summaryAllowed = repository.getCourseSettings(effect.courseId, effect.forceNetwork)?.courseSummary == true

            val course = repository.getCourseWithSyllabus(effect.courseId, effect.forceNetwork)

            val summaryResult: DataResult<List<ScheduleItem>>
            if (course.isFail) {
                summaryResult = if (summaryAllowed) DataResult.Fail() else DataResult.Success(emptyList())
                consumer.accept(SyllabusEvent.DataLoaded(course, summaryResult))
                return@launch
            }

            if (!summaryAllowed) {
                summaryResult = DataResult.Success(emptyList())
            } else {
                val contextCodes = listOf(course.dataOrThrow.contextId)

                val assignments = repository.getCalendarEvents(true, CalendarEventAPI.CalendarEventType.ASSIGNMENT, null, null, contextCodes, effect.forceNetwork)
                val events = repository.getCalendarEvents(true, CalendarEventAPI.CalendarEventType.CALENDAR, null, null, contextCodes, effect.forceNetwork)
                val plannerItems = repository.getPlannerItems(null, null, contextCodes, "all_ungraded_todo_items", effect.forceNetwork)

                val endList = mutableListOf<ScheduleItem>()

                assignments.map { endList.addAll(it) }
                events.map { endList.addAll(it) }
                plannerItems.map { items ->
                    // Filter out assignments, quizzes, and calendar events as they're already fetched above
                    val filteredItems = items.filter {
                        it.plannableType != com.instructure.canvasapi2.models.PlannableType.ASSIGNMENT &&
                        it.plannableType != com.instructure.canvasapi2.models.PlannableType.QUIZ &&
                        it.plannableType != com.instructure.canvasapi2.models.PlannableType.CALENDAR_EVENT
                    }
                    endList.addAll(filteredItems.map { it.toScheduleItem() })
                }

                endList.sort()

                summaryResult = if (assignments.isFail && events.isFail && plannerItems.isFail) {
                    DataResult.Fail((assignments as? DataResult.Fail)?.failure)
                } else {
                    DataResult.Success(endList)
                }
            }

            consumer.accept(SyllabusEvent.DataLoaded(course, summaryResult))
        }
    }
}

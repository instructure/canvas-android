/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.teacher.features.syllabus

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.teacher.features.syllabus.ui.SyllabusView
import com.instructure.teacher.mobius.common.ui.EffectHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SyllabusEffectHandler(
    private val repository: SyllabusRepository
) : EffectHandler<SyllabusView, SyllabusEvent, SyllabusEffect>() {

    override fun accept(effect: SyllabusEffect) {
        when (effect) {
            is SyllabusEffect.LoadData -> loadData(effect)
            is SyllabusEffect.ShowAssignmentView -> view?.showAssignmentView(effect.assignment, effect.course)
            is SyllabusEffect.ShowScheduleItemView -> view?.showScheduleItemView(effect.scheduleItem, effect.course)
            is SyllabusEffect.OpenEditSyllabus -> view?.openEditSyllabus(effect.course, effect.summaryAllowed)
        }.exhaustive
    }

    private fun loadData(effect: SyllabusEffect.LoadData) {
        launch {
            val summaryAllowed = CourseManager
                    .getCourseSettingsAsync(effect.courseId, effect.forceNetwork)
                    .await()
                    .dataOrNull?.courseSummary == true

            val course = CourseManager.getCourseWithSyllabusAsync(effect.courseId, effect.forceNetwork).await()

            val summaryResult: DataResult<List<ScheduleItem>>
            if (course.isFail) {
                summaryResult = if (summaryAllowed) DataResult.Fail() else DataResult.Success(emptyList())
                consumer.accept(SyllabusEvent.DataLoaded(course, summaryResult, DataResult.Fail(), summaryAllowed))
                return@launch
            }

            if (!summaryAllowed) {
                summaryResult = DataResult.Success(emptyList())
            } else {
                val contextCodes = listOf(course.dataOrThrow.contextId)

                val assignmentsResult = async { repository.getCalendarEvents(true, CalendarEventAPI.CalendarEventType.ASSIGNMENT, null, null, contextCodes, effect.forceNetwork) }.await()
                val eventsResult = async { repository.getCalendarEvents(true, CalendarEventAPI.CalendarEventType.CALENDAR, null, null, contextCodes, effect.forceNetwork) }.await()
                val plannerItemsResult = async { repository.getPlannerItems(null, null, contextCodes, "all_ungraded_todo_items", effect.forceNetwork) }.await()

                summaryResult = if (assignmentsResult.isFail && eventsResult.isFail && plannerItemsResult.isFail) {
                    DataResult.Fail((assignmentsResult as? DataResult.Fail)?.failure)
                } else {
                    createSuccessResult(assignmentsResult, eventsResult, plannerItemsResult)
                }
            }

            val permissionsDeferred = CourseManager.getPermissionsAsync(course.dataOrThrow.id, listOf(CanvasContextPermission.MANAGE_CONTENT, CanvasContextPermission.MANAGE_COURSE_CONTENT_EDIT))
            val permissionsResult = permissionsDeferred.await()

            consumer.accept(SyllabusEvent.DataLoaded(course, summaryResult, permissionsResult, summaryAllowed))
        }
    }

    private fun createSuccessResult(
        assignmentsResult: DataResult<List<ScheduleItem>>,
        eventsResult: DataResult<List<ScheduleItem>>,
        plannerItemsResult: DataResult<List<PlannerItem>>
    ): DataResult.Success<List<ScheduleItem>> {
        val assignments = assignmentsResult.dataOrNull ?: emptyList()
        val events = eventsResult.dataOrNull ?: emptyList()
        val plannerItems = plannerItemsResult.dataOrNull
            ?.filter {
                it.plannableType != com.instructure.canvasapi2.models.PlannableType.ASSIGNMENT &&
                it.plannableType != com.instructure.canvasapi2.models.PlannableType.CALENDAR_EVENT
            }
            ?.map { it.toScheduleItem() } ?: emptyList()
        val combinedList = (assignments + events + plannerItems).sorted()

        return DataResult.Success(combinedList)
    }
}
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

import android.content.Context
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.isValid
import com.instructure.pandares.R
import com.instructure.pandautils.utils.color
import com.instructure.student.mobius.common.ui.Presenter
import com.instructure.student.mobius.syllabus.ui.EventsViewState
import com.instructure.student.mobius.syllabus.ui.ScheduleItemViewState
import com.instructure.student.mobius.syllabus.ui.SyllabusViewState
import com.instructure.student.util.toDueAtString
import java.util.Date

object SyllabusPresenter : Presenter<SyllabusModel, SyllabusViewState> {
    override fun present(model: SyllabusModel, context: Context): SyllabusViewState {
        if (model.isLoading) return SyllabusViewState.Loading

        if (model.course?.isFail == true && model.events?.isFail == true) {
            return SyllabusViewState.Loaded(eventsState = EventsViewState.Error)
        }

        val course = model.course?.dataOrNull
        val events = mapEventsResultToViewState(course?.color ?: 0, model.events, context)
        val body = model.syllabus?.description?.takeIf { it.isValid() }

        return SyllabusViewState.Loaded(
            syllabus = body,
            eventsState = events.takeUnless { it == EventsViewState.Empty && body != null }
        )
    }

    private fun mapEventsResultToViewState(color: Int, eventsResult: DataResult<List<ScheduleItem>>?, context: Context): EventsViewState {
        if (eventsResult == null) return EventsViewState.Empty

        return when {
            eventsResult.isFail -> EventsViewState.Error
            eventsResult.dataOrNull.isNullOrEmpty() -> EventsViewState.Empty
            else -> {
                EventsViewState.Loaded(eventsResult.dataOrThrow.filter { it.isHidden.not() }.map {
                    ScheduleItemViewState(
                        it.itemId,
                        it.title ?: "",
                        getDateString(it, context),
                        getIcon(it),
                        color
                    )
                })
            }
        }
    }

    private fun getDateString(event: ScheduleItem, context: Context): String {
        return if (event.startAt == null) {
            context.getString(R.string.toDoNoDueDate)
        } else if (event.itemType == ScheduleItem.Type.TYPE_ASSIGNMENT){
            event.startAt!!.toDueAtString(context)
        } else {
            event.startDate?.let { DateHelper.getMonthDayAtTime(context, it, R.string.at) } ?: ""
        }
    }

    private fun getIcon(event: ScheduleItem): Int {
        if (event.assignment?.isLocked == true || event.assignment?.lockExplanation?.takeIf {
                it.isValid() && event.assignment?.lockDate?.before(Date()) == true
            } != null) {
            return com.instructure.student.R.drawable.ic_lock_lined
        }

        return event.assignment?.let {
            getAssignmentIcon(it)
        } ?: com.instructure.student.R.drawable.ic_calendar
    }

    private fun getAssignmentIcon(assignment: Assignment) = when {
        assignment.getSubmissionTypes().contains(Assignment.SubmissionType.ONLINE_QUIZ) -> com.instructure.student.R.drawable.ic_quiz
        assignment.getSubmissionTypes().contains(Assignment.SubmissionType.EXTERNAL_TOOL) &&
            assignment.externalToolAttributes?.url?.contains("quiz-lti") == true -> com.instructure.student.R.drawable.ic_quiz
        assignment.getSubmissionTypes().contains(Assignment.SubmissionType.DISCUSSION_TOPIC) -> com.instructure.student.R.drawable.ic_discussion
        else -> com.instructure.student.R.drawable.ic_assignment
    }
}

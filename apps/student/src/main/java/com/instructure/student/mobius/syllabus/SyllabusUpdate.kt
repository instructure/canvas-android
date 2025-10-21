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

import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.student.mobius.common.ui.UpdateInit
import com.spotify.mobius.First
import com.spotify.mobius.Next

class SyllabusUpdate : UpdateInit<SyllabusModel, SyllabusEvent, SyllabusEffect>() {
    override fun performInit(model: SyllabusModel): First<SyllabusModel, SyllabusEffect> {
        return First.first(model.copy(isLoading = true), setOf<SyllabusEffect>(SyllabusEffect.LoadData(model.courseId, false)))
    }

    override fun update(model: SyllabusModel, event: SyllabusEvent): Next<SyllabusModel, SyllabusEffect> {
        return when (event) {
            SyllabusEvent.PullToRefresh -> Next.next(model.copy(isLoading = true), setOf<SyllabusEffect>(SyllabusEffect.LoadData(model.courseId, true)))
            is SyllabusEvent.DataLoaded -> {
                Next.next(model.copy(
                    isLoading = false,
                    course = event.course,
                    events = event.events,
                    syllabus = event.course.dataOrNull?.let { ScheduleItem.createSyllabus(it.name, it.syllabusBody) }
                ))
            }
            is SyllabusEvent.SyllabusItemClicked -> {
                val item = model.events!!.dataOrThrow.find { it.itemId == event.itemId }!!
                val assignment  = item.assignment ?: item.subAssignment
                Next.dispatch(setOf(
                    when {
                        assignment != null -> {
                            val assignmentId = assignment.discussionTopicHeader?.assignmentId?.takeIf { it != 0L } ?: assignment.id
                            SyllabusEffect.ShowAssignmentView(assignmentId, model.course!!.dataOrThrow)
                        }
                        else -> SyllabusEffect.ShowScheduleItemView(item, model.course!!.dataOrThrow)
                    }
                ))
            }
        }
    }
}

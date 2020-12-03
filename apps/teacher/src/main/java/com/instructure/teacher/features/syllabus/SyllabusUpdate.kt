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

import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.teacher.mobius.common.ui.UpdateInit
import com.spotify.mobius.First
import com.spotify.mobius.Next

class SyllabusUpdate : UpdateInit<SyllabusModel, SyllabusEvent, SyllabusEffect>() {

    override fun performInit(model: SyllabusModel): First<SyllabusModel, SyllabusEffect> {
        return First.first(model.copy(isLoading = true), setOf<SyllabusEffect>(SyllabusEffect.LoadData(model.courseId, false)))
    }

    override fun update(model: SyllabusModel, event: SyllabusEvent): Next<SyllabusModel, SyllabusEffect> {
        return when (event) {
            is SyllabusEvent.PullToRefresh -> handlePullToRefresh(model)
            is SyllabusEvent.DataLoaded -> handleDataLoaded(model, event)
            is SyllabusEvent.SyllabusItemClicked -> handleSyllabusItemClicked(model, event)
            is SyllabusEvent.EditClicked -> handleEditClicked(model)
            is SyllabusEvent.SyllabusUpdatedEvent -> handleSyllabusUpdatedEvent(model, event)
        }
    }

    private fun handlePullToRefresh(model: SyllabusModel): Next<SyllabusModel, SyllabusEffect> {
        return Next.next(model.copy(isLoading = true), setOf<SyllabusEffect>(SyllabusEffect.LoadData(model.courseId, true)))
    }

    private fun handleDataLoaded(model: SyllabusModel, event: SyllabusEvent.DataLoaded): Next<SyllabusModel, SyllabusEffect> {
        return Next.next(model.copy(
            isLoading = false,
            course = event.course,
            events = event.events,
            syllabus = event.course.dataOrNull?.let { ScheduleItem.createSyllabus(it.name, it.syllabusBody) },
            permissions = event.permissionsResult,
            summaryAllowed = event.summaryAllowed
        ))
    }

    private fun handleSyllabusItemClicked(model: SyllabusModel, event: SyllabusEvent.SyllabusItemClicked): Next<SyllabusModel, SyllabusEffect> {
        val item = model.events!!.dataOrThrow.find { it.itemId == event.itemId }!!
        return Next.dispatch(setOf(
            when {
                item.assignment != null -> SyllabusEffect.ShowAssignmentView(item.assignment!!, model.course!!.dataOrThrow)
                else -> SyllabusEffect.ShowScheduleItemView(item, model.course!!.dataOrThrow)
            }
        ))
    }

    private fun handleEditClicked(model: SyllabusModel): Next<SyllabusModel, SyllabusEffect> {
        return Next.dispatch(setOf(SyllabusEffect.OpenEditSyllabus(model.course!!.dataOrThrow, model.summaryAllowed)))
    }

    private fun handleSyllabusUpdatedEvent(model: SyllabusModel, event: SyllabusEvent.SyllabusUpdatedEvent): Next<SyllabusModel, SyllabusEffect> {
        return if (model.summaryAllowed == event.summaryAllowed) {
            val course = model.course?.dataOrNull?.copy(syllabusBody = event.content)
            val syllabus = ScheduleItem.createSyllabus(course?.name, event.content)
            val courseResult = course?.let {
                DataResult.Success(course)
            } ?: DataResult.Fail()

            Next.next(model.copy(course = courseResult, syllabus = syllabus))
        } else {
            Next.next(model.copy(isLoading = true), setOf<SyllabusEffect>(SyllabusEffect.LoadData(model.courseId, true)))
        }
    }
}
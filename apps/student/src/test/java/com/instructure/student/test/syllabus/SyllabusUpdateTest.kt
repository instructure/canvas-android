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
package com.instructure.student.test.syllabus

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.mobius.syllabus.SyllabusEffect
import com.instructure.student.mobius.syllabus.SyllabusEvent
import com.instructure.student.mobius.syllabus.SyllabusModel
import com.instructure.student.mobius.syllabus.SyllabusUpdate
import com.instructure.student.test.util.matchesEffects
import com.instructure.student.test.util.matchesFirstEffects
import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SyllabusUpdateTest : Assert() {
    private val initSpec = InitSpec(SyllabusUpdate()::init)
    private val updateSpec = UpdateSpec(SyllabusUpdate()::update)

    private lateinit var initModel: SyllabusModel
    private lateinit var course: Course
    private lateinit var syllabus: ScheduleItem
    private var courseId: Long = 0

    @Before
    fun setup() {
        courseId = 1234L
        course = Course(id = courseId, name = "Course Name", syllabusBody = "This is a syllabus")
        syllabus = ScheduleItem.createSyllabus(course.name, course.syllabusBody)
        initModel = SyllabusModel(courseId = courseId)
    }

    @Test
    fun `Initializes into a loading state`() {
        val expectedModel = initModel.copy(isLoading = true)
        initSpec
            .whenInit(initModel)
            .then(
                assertThatFirst(
                    FirstMatchers.hasModel(expectedModel),
                    matchesFirstEffects<SyllabusModel, SyllabusEffect>(SyllabusEffect.LoadData(course.id, false))
                )
            )
    }

    @Test
    fun `PullToRefresh event forces network reload`() {
        val expectedModel = initModel.copy(isLoading = true)
        updateSpec
            .given(initModel)
            .whenEvent(SyllabusEvent.PullToRefresh)
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    matchesEffects<SyllabusModel, SyllabusEffect>(SyllabusEffect.LoadData(course.id, true))
                )
            )
    }

    @Test
    fun `DataLoaded event updates the model`() {
        val course = DataResult.Success(course)
        val events = DataResult.Success(List(4) {
            ScheduleItem(itemId = it.toString())
        })

        val expectedModel = initModel.copy(isLoading = false, course = course, events = events, syllabus = syllabus)

        updateSpec
            .given(initModel.copy(isLoading = true))
            .whenEvent(SyllabusEvent.DataLoaded(course, events))
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel)
                )
            )
    }

    @Test
    fun `DataLoaded event updates the model when failed to get course`() {
        val initModel = initModel.copy(isLoading = true, course = DataResult.Success(course), events = DataResult.Success(emptyList()), syllabus = ScheduleItem.createSyllabus(null, null))

        val course = DataResult.Fail()
        val expectedModel = initModel.copy(isLoading = false, course = course, events = course, syllabus = null)

        updateSpec
            .given(initModel)
            .whenEvent(SyllabusEvent.DataLoaded(course, course))
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel)
                )
            )
    }

    @Test
    fun `DataLoaded event updates the model when failed to get events`() {
        val initModel = initModel.copy(isLoading = true)

        val events = DataResult.Fail()
        val course = DataResult.Success(course)
        val expectedModel = initModel.copy(isLoading = false, course = course, events = events, syllabus = syllabus)

        updateSpec
            .given(initModel)
            .whenEvent(SyllabusEvent.DataLoaded(course, events))
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel)
                )
            )
    }

    @Test
    fun `SyllabusItemClicked event dispatches ShowAssignmentView when item has an assignment`() {
        val assignmentId = 123L
        val courseResult = DataResult.Success(course)
        val events = DataResult.Success(List(4) {
            ScheduleItem(itemId = it.toString(), assignment = if (it == 0) Assignment(id = assignmentId) else null)
        })

        val initModel = initModel.copy(course = courseResult, events = events)

        updateSpec
            .given(initModel)
            .whenEvent(SyllabusEvent.SyllabusItemClicked("0"))
            .then(
                assertThatNext(
                    matchesEffects<SyllabusModel, SyllabusEffect>(SyllabusEffect.ShowAssignmentView(assignmentId, course))
                )
            )
    }

    @Test
    fun `SyllabusItemClicked event dispatches ShowScheduleItemView when item does not have an assignment`() {
        val assignmentId = 123L
        val courseResult = DataResult.Success(course)
        val events = DataResult.Success(List(4) {
            ScheduleItem(itemId = it.toString(), assignment = if (it == 0) Assignment(id = assignmentId) else null)
        })

        val initModel = initModel.copy(course = courseResult, events = events)

        updateSpec
            .given(initModel)
            .whenEvent(SyllabusEvent.SyllabusItemClicked("1"))
            .then(
                assertThatNext(
                    matchesEffects<SyllabusModel, SyllabusEffect>(SyllabusEffect.ShowScheduleItemView(events.data[1], course))
                )
            )
    }

}
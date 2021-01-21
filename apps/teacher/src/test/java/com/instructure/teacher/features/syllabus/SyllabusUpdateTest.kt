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

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.teacher.unit.utils.matchesEffects
import com.instructure.teacher.unit.utils.matchesFirstEffects
import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.UpdateSpec
import org.junit.Before
import org.junit.Test

class SyllabusUpdateTest {

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
    fun `Init should start loading data and `() {
        val expectedModel = initModel.copy(isLoading = true)
        initSpec
            .whenInit(initModel)
            .then(
                InitSpec.assertThatFirst(
                    FirstMatchers.hasModel(expectedModel),
                    matchesFirstEffects<SyllabusModel, SyllabusEffect>(SyllabusEffect.LoadData(course.id, false))
                )
            )
    }

    @Test
    fun `PullToRefresh event should force network reload`() {
        val expectedModel = initModel.copy(isLoading = true)
        updateSpec
            .given(initModel)
            .whenEvent(SyllabusEvent.PullToRefresh)
            .then(
                UpdateSpec.assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    matchesEffects<SyllabusModel, SyllabusEffect>(SyllabusEffect.LoadData(course.id, true))
                )
            )
    }

    @Test
    fun `DataLoaded event should update the model`() {
        val course = DataResult.Success(course)
        val events = DataResult.Success(List(4) {
            ScheduleItem(itemId = it.toString())
        })
        val permissionsResult = DataResult.Success(CanvasContextPermission(canManageContent = true))

        val expectedModel = initModel.copy(isLoading = false, course = course, events = events, syllabus = syllabus, permissions = permissionsResult)

        updateSpec
            .given(initModel.copy(isLoading = true))
            .whenEvent(SyllabusEvent.DataLoaded(course, events, permissionsResult))
            .then(
                UpdateSpec.assertThatNext(NextMatchers.hasModel(expectedModel))
            )
    }

    @Test
    fun `DataLoaded event should update the model without the syllabus when failed to get course`() {
        val initModel = initModel.copy(isLoading = true, course = DataResult.Success(course), events = DataResult.Success(emptyList()), syllabus = ScheduleItem.createSyllabus(null, null))

        val course = DataResult.Fail()
        val permissionsResult = DataResult.Success(CanvasContextPermission(canManageContent = true))

        val expectedModel = initModel.copy(isLoading = false, course = course, events = course, syllabus = null, permissions = permissionsResult)

        updateSpec
            .given(initModel)
            .whenEvent(SyllabusEvent.DataLoaded(course, course, permissionsResult))
            .then(
                UpdateSpec.assertThatNext(NextMatchers.hasModel(expectedModel))
            )
    }

    @Test
    fun `SyllabusItemClicked event should dispatch ShowAssignmentView effect when item has an assignment`() {
        val assignmentId = 123L
        val courseResult = DataResult.Success(course)
        val events = DataResult.Success(listOf(ScheduleItem(itemId = "1", assignment = Assignment(id = assignmentId))))

        val initModel = initModel.copy(course = courseResult, events = events)

        updateSpec
            .given(initModel)
            .whenEvent(SyllabusEvent.SyllabusItemClicked("1"))
            .then(
                UpdateSpec.assertThatNext(matchesEffects<SyllabusModel, SyllabusEffect>(SyllabusEffect.ShowAssignmentView(events.data[0].assignment!!, course)))
            )
    }

    @Test
    fun `SyllabusItemClicked event should dispatch ShowScheduleItemView when item does not have an assignment`() {
        val courseResult = DataResult.Success(course)
        val events = DataResult.Success(listOf(ScheduleItem(itemId = "1")))

        val initModel = initModel.copy(course = courseResult, events = events)

        updateSpec
            .given(initModel)
            .whenEvent(SyllabusEvent.SyllabusItemClicked("1"))
            .then(
                UpdateSpec.assertThatNext(matchesEffects<SyllabusModel, SyllabusEffect>(SyllabusEffect.ShowScheduleItemView(events.data[0], course)))
            )
    }

    @Test
    fun `EditClicked event should dispatch OpenEditSyllabus`() {
        val courseResult = DataResult.Success(course)

        val initModel = initModel.copy(course = courseResult, summaryAllowed = true)

        updateSpec
            .given(initModel)
            .whenEvent(SyllabusEvent.EditClicked)
            .then(
                UpdateSpec.assertThatNext(matchesEffects<SyllabusModel, SyllabusEffect>(SyllabusEffect.OpenEditSyllabus(course, true)))
            )
    }

    @Test
    fun `SyllabusUpdatedEvent should load data if syllabus was updated`() {
        val expectedModel = initModel.copy(isLoading = true)
        updateSpec
            .given(initModel)
            .whenEvent(SyllabusEvent.SyllabusUpdatedEvent(summaryAllowed = true, content = "Web Content"))
            .then(
                UpdateSpec.assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    matchesEffects<SyllabusModel, SyllabusEffect>(SyllabusEffect.LoadData(course.id, true))
                )
            )
    }
}
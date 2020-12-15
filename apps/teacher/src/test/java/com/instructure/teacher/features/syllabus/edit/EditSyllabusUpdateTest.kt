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
package com.instructure.teacher.features.syllabus.edit

import com.instructure.canvasapi2.models.Course
import com.instructure.teacher.events.SyllabusUpdatedEvent
import com.instructure.teacher.unit.utils.matchesEffects
import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.UpdateSpec
import org.greenrobot.eventbus.EventBus
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class EditSyllabusUpdateTest {

    private val initSpec = InitSpec(EditSyllabusUpdate()::init)
    private val updateSpec = UpdateSpec(EditSyllabusUpdate()::update)

    private lateinit var initModel: EditSyllabusModel
    private lateinit var course: Course

    @Before
    fun setup() {
        course = Course(id = 1234L, name = "Course Name", syllabusBody = "This is a syllabus")
        initModel = EditSyllabusModel(course, true)
    }

    @Test
    fun `Init should set init model and has no side effect`() {
        initSpec
            .whenInit(initModel)
            .then(
                InitSpec.assertThatFirst(
                    FirstMatchers.hasModel(initModel),
                    FirstMatchers.hasNoEffects())
            )
    }

    @Test
    fun `SaveClicked event should set the model to saving and save data`() {
        val content = "New syllabus"
        val summaryAllowed = false

        val expectedModel = initModel.copy(isSaving = true)
        updateSpec
            .given(initModel)
            .whenEvent(EditSyllabusEvent.SaveClicked(content, summaryAllowed))
            .then(
                UpdateSpec.assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    matchesEffects<EditSyllabusModel, EditSyllabusEffect>(EditSyllabusEffect.SaveData(course, content, summaryAllowed))
                )
            )
    }

    @Test
    fun `Save success event should create model with the saved data, close edit and show success message`() {
        val content = "New syllabus"
        val summaryAllowed = false

        val givenModel = initModel.copy(isSaving = true)
        val expectedModel = givenModel.copy(isSaving = false, course = course.copy(syllabusBody = content), summaryAllowed = summaryAllowed)
        updateSpec
            .given(givenModel)
            .whenEvent(EditSyllabusEvent.SyllabusSaveSuccess(content, summaryAllowed))
            .then(
                UpdateSpec.assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    matchesEffects<EditSyllabusModel, EditSyllabusEffect>(EditSyllabusEffect.CloseEdit, EditSyllabusEffect.ShowSaveSuccess)
                )
            )

        val stickyEvent = EventBus.getDefault().getStickyEvent(SyllabusUpdatedEvent::class.java)
        assertEquals(stickyEvent.content, content)
        assertEquals(stickyEvent.summaryAllowed, summaryAllowed)
    }

    @Test
    fun `Save error event should show error message and set saving to false`() {
        val givenModel = initModel.copy(isSaving = true)
        val expectedModel = givenModel.copy(isSaving = false)
        updateSpec
            .given(givenModel)
            .whenEvent(EditSyllabusEvent.SyllabusSaveError)
            .then(
                UpdateSpec.assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    matchesEffects<EditSyllabusModel, EditSyllabusEffect>(EditSyllabusEffect.ShowSaveError)
                )
            )
    }

    @Test
    fun `Back click event should close edit if nothing was changed`() {
        updateSpec
            .given(initModel)
            .whenEvent(EditSyllabusEvent.BackClicked(initModel.course.syllabusBody!!, initModel.summaryAllowed))
            .then(
                UpdateSpec.assertThatNext(
                    matchesEffects<EditSyllabusModel, EditSyllabusEffect>(EditSyllabusEffect.CloseEdit)
                )
            )
    }

    @Test
    fun `Back click event should show exit confirmation if summary allowed was changed`() {
        val summaryAllowed = false

        updateSpec
            .given(initModel)
            .whenEvent(EditSyllabusEvent.BackClicked(initModel.course.syllabusBody!!, summaryAllowed))
            .then(
                UpdateSpec.assertThatNext(
                    matchesEffects<EditSyllabusModel, EditSyllabusEffect>(EditSyllabusEffect.ShowCloseConfirmationDialog)
                )
            )
    }

    @Test
    fun `Back click event should show exit confirmation if syllabus content was changed`() {
        val content = "new syllabus"

        updateSpec
            .given(initModel)
            .whenEvent(EditSyllabusEvent.BackClicked(content, initModel.summaryAllowed))
            .then(
                UpdateSpec.assertThatNext(
                    matchesEffects<EditSyllabusModel, EditSyllabusEffect>(EditSyllabusEffect.ShowCloseConfirmationDialog)
                )
            )
    }

    @Test
    fun `Back click event should show exit confirmation if model was changed previously`() {
        updateSpec
            .given(initModel.copy(isChanged = true))
            .whenEvent(EditSyllabusEvent.BackClicked(initModel.course.syllabusBody!!, initModel.summaryAllowed))
            .then(
                UpdateSpec.assertThatNext(
                    matchesEffects<EditSyllabusModel, EditSyllabusEffect>(EditSyllabusEffect.ShowCloseConfirmationDialog)
                )
            )
    }

    @Test
    fun `Save state event should create model with the changed data`() {
        val content = "New syllabus"
        val summaryAllowed = false

        val expectedModel = initModel.copy(course = course.copy(syllabusBody = content), summaryAllowed = summaryAllowed, isChanged = true)
        updateSpec
            .given(initModel)
            .whenEvent(EditSyllabusEvent.SaveState(content, summaryAllowed))
            .then(
                UpdateSpec.assertThatNext(
                    NextMatchers.hasModel(expectedModel)
                )
            )
    }
}
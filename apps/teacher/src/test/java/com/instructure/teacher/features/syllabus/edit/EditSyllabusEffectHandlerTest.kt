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

import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.utils.DataResult
import com.spotify.mobius.functions.Consumer
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executors

private const val COURSE_ID: Long = 1L

class EditSyllabusEffectHandlerTest {

    private val editSyllabusView: EditSyllabusView = mockk(relaxed = true)
    private val eventConsumer: Consumer<EditSyllabusEvent> = mockk(relaxed = true)

    private val effectHandler = EditSyllabusEffectHandler().apply {
        view = this@EditSyllabusEffectHandlerTest.editSyllabusView
        connect(eventConsumer)
    }

    private lateinit var course: Course

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
        course = Course(id = COURSE_ID)
    }

    @Test
    fun `Save Data should produce error event if edit course request fails`() {
        // Given
        val newSyllabusBody = "Edited Syllabus"

        mockkObject(CourseManager)
        every { CourseManager.editCourseSyllabusAsync(COURSE_ID, newSyllabusBody) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }
        every { CourseManager.editCourseSettingsAsync(COURSE_ID, true) } returns mockk {
            coEvery { await() } returns DataResult.Success(CourseSettings(courseSummary = true))
        }

        // When
        effectHandler.accept(EditSyllabusEffect.SaveData(course, newSyllabusBody, true))

        // Then
        val expectedEvent = EditSyllabusEvent.SyllabusSaveError
        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `Save Data should produce error event if edit course settings request fails`() {
        // Given
        val newSyllabusBody = "Edited Syllabus"

        mockkObject(CourseManager)
        every { CourseManager.editCourseSyllabusAsync(COURSE_ID, newSyllabusBody) } returns mockk {
            coEvery { await() } returns DataResult.Success(course.copy(syllabusBody = newSyllabusBody))
        }
        every { CourseManager.editCourseSettingsAsync(COURSE_ID, true) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        // When
        effectHandler.accept(EditSyllabusEffect.SaveData(course, newSyllabusBody, true))

        // Then
        val expectedEvent = EditSyllabusEvent.SyllabusSaveError
        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `Save Data should produce save success event if both requests are successful`() {
        // Given
        val newSyllabusBody = "Edited Syllabus"

        mockkObject(CourseManager)
        every { CourseManager.editCourseSyllabusAsync(COURSE_ID, newSyllabusBody) } returns mockk {
            coEvery { await() } returns DataResult.Success(course.copy(syllabusBody = newSyllabusBody))
        }
        every { CourseManager.editCourseSettingsAsync(COURSE_ID, true) } returns mockk {
            coEvery { await() } returns DataResult.Success(CourseSettings(courseSummary = true))
        }

        // When
        effectHandler.accept(EditSyllabusEffect.SaveData(course, newSyllabusBody, true))

        // Then
        val expectedEvent = EditSyllabusEvent.SyllabusSaveSuccess(newSyllabusBody, true)
        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `CloseEdit results in closing the edit syllabus screen`() {
        // When
        effectHandler.accept(EditSyllabusEffect.CloseEdit)

        // Then
        verify(timeout = 100) {
            editSyllabusView.closeEditSyllabus()
        }

        confirmVerified(editSyllabusView)
    }

    @Test
    fun `ShowSaveSuccess results in showing save success message`() {
        // When
        effectHandler.accept(EditSyllabusEffect.ShowSaveSuccess)

        // Then
        verify(timeout = 100) {
            editSyllabusView.showSaveSuccess()
        }

        confirmVerified(editSyllabusView)
    }

    @Test
    fun `ShowSaveError results in showing save error message`() {
        // When
        effectHandler.accept(EditSyllabusEffect.ShowSaveError)

        // Then
        verify(timeout = 100) {
            editSyllabusView.showSaveError()
        }

        confirmVerified(editSyllabusView)
    }

    @Test
    fun `ShowCloseConfirmationDialog results in showing close confirmation dialog`() {
        // When
        effectHandler.accept(EditSyllabusEffect.ShowCloseConfirmationDialog)

        // Then
        verify(timeout = 100) {
            editSyllabusView.showCloseConfirmationDialog()
        }

        confirmVerified(editSyllabusView)
    }
}
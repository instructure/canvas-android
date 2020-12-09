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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Course
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditSyllabusPresenterTest {

    private val presenter = EditSyllabusPresenter()
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `Should return saving state if received model is saving`() {
        // Given
        val model = EditSyllabusModel(Course(syllabusBody = "syllabus"), summaryAllowed = true, isSaving = true)

        // When
        val viewState = presenter.present(model, context)

        // Then
        assertEquals(EditSyllabusViewState.Saving, viewState)
    }

    @Test
    fun `Should return loaded state with correct data if received model is not saving`() {
        // Given
        val model = EditSyllabusModel(Course(syllabusBody = "syllabus"), summaryAllowed = true, isSaving = false)

        // When
        val viewState = presenter.present(model, context)

        // Then
        val expectedViewState = EditSyllabusViewState.Loaded("syllabus", true)
        assertEquals(expectedViewState, viewState)
    }
}
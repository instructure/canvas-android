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
package com.instructure.student.test.assignment.details.submission

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.student.mobius.assignmentDetails.submissions.text.TextSubmissionUploadModel
import com.instructure.student.mobius.assignmentDetails.submissions.text.TextSubmissionUploadPresenter
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TextSubmissionUploadPresenterTest : Assert() {
    private lateinit var context: Context
    private lateinit var canvasContext: CanvasContext
    private lateinit var baseModel: TextSubmissionUploadModel

    private val assignmentId = 123L
    private val assignmentName = "Assignment"

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        canvasContext = CanvasContext.emptyCourseContext(0)
        baseModel = TextSubmissionUploadModel(
            canvasContext = canvasContext,
            assignmentId = assignmentId,
            assignmentName = assignmentName
        )
    }

    @Test
    fun `returns initial text when the model has it`() {
        val initText = "test"
        val model = baseModel.copy(initialText = initText)
        val actualState = TextSubmissionUploadPresenter.present(model, context)
        assertEquals(initText, actualState.text)
    }

    @Test
    fun `returns null initial text when the model has none`() {
        val initText = null
        val model = baseModel.copy(initialText = initText)
        val actualState = TextSubmissionUploadPresenter.present(model, context)
        assertEquals(initText, actualState.text)
    }

    @Test
    fun `returns submit enabled when the model is submittable`() {
        val model = baseModel.copy(isSubmittable = true)
        val actualState = TextSubmissionUploadPresenter.present(model, context)
        assertTrue(actualState.submitEnabled)
    }

    @Test
    fun `returns submit not enabled when the model is not submittable`() {
        val model = baseModel.copy(isSubmittable = false)
        val actualState = TextSubmissionUploadPresenter.present(model, context)
        assertFalse(actualState.submitEnabled)
    }

    @Test
    fun `returns is failure when the model is failure`() {
        val model = baseModel.copy(isFailure = true)
        val actualState = TextSubmissionUploadPresenter.present(model, context)
        assertTrue(actualState.isFailure)
    }

    @Test
    fun `returns is not failure when the model is not failure`() {
        val model = baseModel.copy(isFailure = false)
        val actualState = TextSubmissionUploadPresenter.present(model, context)
        assertFalse(actualState.isFailure)
    }
}
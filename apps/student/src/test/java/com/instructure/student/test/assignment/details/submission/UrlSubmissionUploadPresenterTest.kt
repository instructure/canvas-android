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
import com.instructure.canvasapi2.models.Course
import com.instructure.student.mobius.assignmentDetails.submission.url.MalformedUrlError
import com.instructure.student.mobius.assignmentDetails.submission.url.UrlSubmissionUploadModel
import com.instructure.student.mobius.assignmentDetails.submission.url.UrlSubmissionUploadPresenter
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UrlSubmissionUploadPresenterTest : Assert() {

    private lateinit var course: Course
    private lateinit var context: Context
    private lateinit var baseModel: UrlSubmissionUploadModel

    private val assignmentId = 12L

    @Before
    fun setup() {
        course = Course()
        context = ApplicationProvider.getApplicationContext()
        baseModel = UrlSubmissionUploadModel(course, assignmentId)
    }

    @Test
    fun `returns isFailure when model isFailure`() {
        val model = baseModel.copy(isFailure = true)
        val actual = UrlSubmissionUploadPresenter.present(model, context)
        assert(actual.isFailure)
    }

    @Test
    fun `returns isFailure when model urlError is CLEARTEXT`() {
        val model = baseModel.copy(urlError = MalformedUrlError.CLEARTEXT)
        val actual = UrlSubmissionUploadPresenter.present(model, context)
        assert(actual.isFailure)
    }

    @Test
    fun `returns not isFailure when model urlError is NONE`() {
        val model = baseModel.copy(urlError = MalformedUrlError.NONE, isFailure = false)
        val actual = UrlSubmissionUploadPresenter.present(model, context)
        assertFalse(actual.isFailure)
    }

    @Test
    fun `returns failed message when model is failure`() {
        val model = baseModel.copy(urlError = MalformedUrlError.NONE, isFailure = true)
        val actual = UrlSubmissionUploadPresenter.present(model, context)
        assertEquals(actual.failureText, "Something went wrong on submission upload. Submit again.")
    }

    @Test
    fun `returns failed message with http warning when model is failure`() {
        val model = baseModel.copy(urlError = MalformedUrlError.CLEARTEXT, isFailure = true)
        val actual = UrlSubmissionUploadPresenter.present(model, context)
        assertEquals(actual.failureText, "Something went wrong on submission upload. Submit again.\nNo preview available for URLs using 'http://'")
    }

    @Test
    fun `returns http warning when model is failure`() {
        val model = baseModel.copy(urlError = MalformedUrlError.CLEARTEXT, isFailure = false)
        val actual = UrlSubmissionUploadPresenter.present(model, context)
        assertEquals(actual.failureText, "No preview available for URLs using 'http://'")
    }
}

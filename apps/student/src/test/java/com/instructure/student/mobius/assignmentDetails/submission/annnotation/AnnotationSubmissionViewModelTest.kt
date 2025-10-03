/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.student.mobius.assignmentDetails.submission.annnotation

import android.content.res.Resources
import com.instructure.canvasapi2.managers.CanvaDocsManager
import com.instructure.canvasapi2.models.canvadocs.CanvaDocSessionResponseBody
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.student.R
import com.instructure.testutils.ViewModelTestRule
import com.instructure.testutils.LifecycleTestOwner
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AnnotationSubmissionViewModelTest {

    private lateinit var viewModel: AnnotationSubmissionViewModel

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val lifecycleTestOwner = LifecycleTestOwner()

    private val canvaDocsManager: CanvaDocsManager = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)

    @Before
    fun setUp() {

        every { resources.getString(R.string.failedToLoadSubmission) } returns "Error"
        viewModel = AnnotationSubmissionViewModel(canvaDocsManager, resources)
    }

    @Test
    fun `Error response doesnt update pdf url and produces error`() {
        // Given
        every { canvaDocsManager.createCanvaDocSessionAsync(any(), any()) } returns mockk() {
            coEvery { await() } returns DataResult.Fail()
        }

        // When
        viewModel.state.observe(lifecycleTestOwner.lifecycleOwner, {})
        viewModel.loadAnnotatedPdfUrl(1)

        // Then
        assertNull(viewModel.pdfUrl.value)
        assertEquals(ViewState.Error("Error"), viewModel.state.value)
    }

    @Test
    fun `Empty session url doesnt update pdf url and produces error`() {
        // Given
        val canvaDocSession = CanvaDocSessionResponseBody("", "")
        every { canvaDocsManager.createCanvaDocSessionAsync(any(), any()) } returns mockk() {
            coEvery { await() } returns DataResult.Success(canvaDocSession)
        }

        // When
        viewModel.state.observe(lifecycleTestOwner.lifecycleOwner, {})
        viewModel.loadAnnotatedPdfUrl(1)

        // Then
        assertNull(viewModel.pdfUrl.value)
        assertEquals(ViewState.Error("Error"), viewModel.state.value)
    }

    @Test
    fun `Valid session url response updates pdf url`() {
        // Given
        val canvaDocSession = CanvaDocSessionResponseBody("", "https://myschool.com/annotatedFile.pdf")
        every { canvaDocsManager.createCanvaDocSessionAsync(any(), any()) } returns mockk() {
            coEvery { await() } returns DataResult.Success(canvaDocSession)
        }

        // When
        viewModel.state.observe(lifecycleTestOwner.lifecycleOwner, {})
        viewModel.loadAnnotatedPdfUrl(1)

        // Then
        assertEquals("https://myschool.com/annotatedFile.pdf", viewModel.pdfUrl.value)
        assertEquals(ViewState.Success, viewModel.state.value)
    }
}

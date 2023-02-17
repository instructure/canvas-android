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
package com.emeritus.student.test.assignment.details.submission

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.emeritus.student.R
import com.emeritus.student.mobius.assignmentDetails.submission.picker.PickerSubmissionMode
import com.emeritus.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadModel
import com.emeritus.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadPresenter
import com.emeritus.student.mobius.assignmentDetails.submission.picker.ui.PickerListItemViewState
import com.emeritus.student.mobius.assignmentDetails.submission.picker.ui.PickerSubmissionUploadViewState
import com.emeritus.student.mobius.assignmentDetails.submission.picker.ui.PickerVisibilities
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PickerSubmissionUploadPresenterTest : Assert() {

    private lateinit var context: Context
    private lateinit var baseFile: FileSubmitObject
    private lateinit var baseModel: PickerSubmissionUploadModel
    private lateinit var baseVisibilities: PickerVisibilities

    @Before
    fun setup() {
        baseFile = FileSubmitObject("FileName", 135L, "ContentType", "FullPath")
        context = ApplicationProvider.getApplicationContext()
        baseModel = PickerSubmissionUploadModel(
            canvasContext = Course(),
            assignmentId = 123L,
            assignmentName = "AssignmentName",
            assignmentGroupCategoryId = 321L,
            allowedExtensions = emptyList(),
            mode = PickerSubmissionMode.FileSubmission
        )
        baseVisibilities = PickerVisibilities(
            sources = true,
            sourceGallery = true,
            sourceCamera = true,
            sourceFile = true,
            loading = false
        )
    }

    @Test
    fun `returns Empty state when files are empty`() {
        val model = baseModel
        val expectedState = PickerSubmissionUploadViewState.Empty(baseVisibilities)
        val actualState = PickerSubmissionUploadPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `returns Loading state with loading when files are empty`() {
        val model = baseModel.copy(isLoadingFile = true)
        val expectedState = PickerSubmissionUploadViewState.FileList(baseVisibilities.copy(loading = true), emptyList())
        val actualState = PickerSubmissionUploadPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `returns camera and gallery hidden when pictures are not allowed filetypes`() {
        val model = baseModel.copy(allowedExtensions = listOf("broken"))
        val expectedState = PickerSubmissionUploadViewState.Empty(
            baseVisibilities.copy(
                sourceCamera = false,
                sourceGallery = false
            )
        )
        val actualState = PickerSubmissionUploadPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `returns file view states and submit visible when there are files`() {
        val model = baseModel.copy(files = listOf(baseFile, baseFile, baseFile))
        val fileViewStates = listOf(
            PickerListItemViewState(
                0,
                R.drawable.ic_attachment,
                baseFile.name,
                "${baseFile.size} B"
            ),
            PickerListItemViewState(
                1,
                R.drawable.ic_attachment,
                baseFile.name,
                "${baseFile.size} B"
            ),
            PickerListItemViewState(
                2,
                R.drawable.ic_attachment,
                baseFile.name,
                "${baseFile.size} B"
            )
        )
        val expectedState = PickerSubmissionUploadViewState.FileList(
            baseVisibilities.copy(submit = true),
            fileViewStates
        )
        val actualState = PickerSubmissionUploadPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `returns file view states and submit and loading visible when there are files and it is loading`() {
        val model = baseModel.copy(files = listOf(baseFile), isLoadingFile = true)
        val fileViewStates = listOf(
            PickerListItemViewState(
                0,
                R.drawable.ic_attachment,
                baseFile.name,
                "${baseFile.size} B"
            )
        )
        val expectedState = PickerSubmissionUploadViewState.FileList(
            baseVisibilities.copy(submit = true, loading = true),
            fileViewStates
        )
        val actualState = PickerSubmissionUploadPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `returns correct file size`() {
        val smallFile = FileSubmitObject("name", 1000L, "type", "path")
        val mediumFile = FileSubmitObject("name", 1000000L, "type", "path")
        val largeFile = FileSubmitObject("name", 1000000000L, "type", "path")
        val model = baseModel.copy(files = listOf(smallFile, mediumFile, largeFile))
        val fileViewStates = listOf(
            PickerListItemViewState(0, R.drawable.ic_attachment, smallFile.name, "1 KB"),
            PickerListItemViewState(1, R.drawable.ic_attachment, mediumFile.name, "1 MB"),
            PickerListItemViewState(2, R.drawable.ic_attachment, largeFile.name, "1 GB")
        )
        val expectedState = PickerSubmissionUploadViewState.FileList(
            baseVisibilities.copy(submit = true),
            fileViewStates
        )
        val actualState = PickerSubmissionUploadPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `returns file view states with canDelete false when it is a media submission`() {
        val model = baseModel.copy(mode = PickerSubmissionMode.MediaSubmission, files = listOf(baseFile))
        val fileViewStates = listOf(
            PickerListItemViewState(
                0,
                R.drawable.ic_attachment,
                baseFile.name,
                "${baseFile.size} B",
                false)
        )
        val expectedState = PickerSubmissionUploadViewState.FileList(
            baseVisibilities.copy(
                submit = true,
                sources = false,
                sourceCamera = false,
                sourceGallery = false,
                sourceFile = false
            ), fileViewStates
        )
        val actualState = PickerSubmissionUploadPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }
}

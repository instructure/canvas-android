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
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionModel
import com.instructure.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionPresenter
import com.instructure.student.mobius.assignmentDetails.submission.file.ui.UploadListItemViewState
import com.instructure.student.mobius.assignmentDetails.submission.file.ui.UploadStatusSubmissionViewState
import com.instructure.student.mobius.assignmentDetails.submission.file.ui.UploadVisibilities
import com.instructure.student.room.entities.CreateFileSubmissionEntity
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UploadStatusSubmissionPresenterTest : Assert() {

    private lateinit var context: Context
    private lateinit var baseFile: CreateFileSubmissionEntity
    private lateinit var baseModel: UploadStatusSubmissionModel
    private lateinit var baseVisibilities: UploadVisibilities

    private val submissionId = 1L
    private val assignmentName = "AssignmentName"

    @Before
    fun setup() {
        baseFile = submission(10L)
        context = ApplicationProvider.getApplicationContext()
        baseModel = UploadStatusSubmissionModel(
            submissionId = submissionId,
            assignmentName = assignmentName
        )
        baseVisibilities = UploadVisibilities()
    }

    private fun submission(id: Long, error: String? = null, errorFlag: Boolean = false) =
        CreateFileSubmissionEntity(id, submissionId, 20L, "File", 1L, "Content", "Path", error, errorFlag)

    @Test
    fun `returns Loading state when loading`() {
        val model = baseModel.copy(isLoading = true)
        val expectedState = UploadStatusSubmissionViewState.Loading
        val actualState = UploadStatusSubmissionPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `returns Success state when no failure and files are empty`() {
        val model = baseModel.copy(files = emptyList())
        val expectedState = UploadStatusSubmissionViewState.Succeeded(
            "Submission Success!",
            "Your assignment was successfully submitted. Enjoy your day!"
        )
        val actualState = UploadStatusSubmissionPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `returns Failed state when isFailed`() {
        val model = baseModel.copy(
            isFailed = true,
            files = listOf(baseFile, submission(11L, error = "ErRoR", errorFlag = true))
        )
        val expectedState = UploadStatusSubmissionViewState.Failed(
            "Submission Failed",
            "One or more files failed to upload. Check your internet connection and retry to submit.",
            listOf(
                UploadListItemViewState(
                    0,
                    R.drawable.ic_attachment,
                    ContextCompat.getColor(context, R.color.textInfo),
                    "File",
                    "1 B",
                    false,
                    null
                ),
                UploadListItemViewState(
                    1,
                    R.drawable.ic_warning,
                    ContextCompat.getColor(context, R.color.textDanger),
                    "File",
                    "1 B",
                    true,
                    "ErRoR"
                )
            )
        )
        val actualState = UploadStatusSubmissionPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `returns InProgress state when not failed, not loading, and has files`() {
        val model = baseModel.copy(
            uploadedBytes = 1L,
            files = listOf(baseFile, submission(11L))
        )
        val expectedState = UploadStatusSubmissionViewState.InProgress(
            "Uploading submission for $assignmentName",
            "1 B of 2 B",
            "50%",
            50.0,
            listOf(
                UploadListItemViewState(
                    0,
                    R.drawable.ic_attachment,
                    ContextCompat.getColor(context, R.color.textInfo),
                    "File",
                    "1 B",
                    false,
                    null
                ),
                UploadListItemViewState(
                    1,
                    R.drawable.ic_attachment,
                    ContextCompat.getColor(context, R.color.textInfo),
                    "File",
                    "1 B",
                    false,
                    null
                )
            )
        )
        val actualState = UploadStatusSubmissionPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `returns InProgress state when not failed, not loading, and has files, with a decimal percentage`() {
        val model = baseModel.copy(
            uploadedBytes = 1L,
            files = listOf(baseFile, submission(11L), submission(12L))
        )
        val expectedState = UploadStatusSubmissionViewState.InProgress(
            "Uploading submission for $assignmentName",
            "1 B of 3 B",
            "33.33%",
            (1 / 3.0) * 100,
            listOf(
                UploadListItemViewState(
                    0,
                    R.drawable.ic_attachment,
                    ContextCompat.getColor(context, R.color.textInfo),
                    "File",
                    "1 B",
                    false,
                    null
                ),
                UploadListItemViewState(
                    1,
                    R.drawable.ic_attachment,
                    ContextCompat.getColor(context, R.color.textInfo),
                    "File",
                    "1 B",
                    false,
                    null
                ),
                UploadListItemViewState(
                    2,
                    R.drawable.ic_attachment,
                    ContextCompat.getColor(context, R.color.textInfo),
                    "File",
                    "1 B",
                    false,
                    null
                )
            )
        )
        val actualState = UploadStatusSubmissionPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }
}

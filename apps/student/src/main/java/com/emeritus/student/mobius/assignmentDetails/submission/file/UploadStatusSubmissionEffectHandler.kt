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
package com.emeritus.student.mobius.assignmentDetails.submission.file

import android.content.Context
import com.instructure.canvasapi2.utils.exhaustive
import com.emeritus.student.FileSubmission
import com.emeritus.student.db.Db
import com.emeritus.student.db.getInstance
import com.emeritus.student.mobius.assignmentDetails.submission.file.ui.UploadStatusSubmissionView
import com.emeritus.student.mobius.common.ui.EffectHandler
import com.emeritus.student.mobius.common.ui.SubmissionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UploadStatusSubmissionEffectHandler(val context: Context, val submissionId: Long) :
    EffectHandler<UploadStatusSubmissionView, UploadStatusSubmissionEvent, UploadStatusSubmissionEffect>() {

    override fun accept(effect: UploadStatusSubmissionEffect) {
        when (effect) {
            is UploadStatusSubmissionEffect.LoadPersistedFiles -> {
                launch(Dispatchers.Main) {
                    val (name, error, files) = loadPersistedData(effect.submissionId, context)
                    consumer.accept(
                        UploadStatusSubmissionEvent.OnPersistedSubmissionLoaded(name, error, files)
                    )
                }
            }
            is UploadStatusSubmissionEffect.ShowCancelDialog -> {
                launch(Dispatchers.Main) { view?.showCancelSubmissionDialog() }
            }
            is UploadStatusSubmissionEffect.OnDeleteSubmission -> {
                launch { deleteSubmission(effect.submissionId) }
            }
            is UploadStatusSubmissionEffect.RetrySubmission -> {
                launch(Dispatchers.Main) { retrySubmission(effect.submissionId) }
            }
            is UploadStatusSubmissionEffect.OnDeleteFileFromSubmission -> {
                launch { deleteFileForSubmission(effect.fileId) }
            }
        }.exhaustive
    }

    private fun loadPersistedData(
        submissionId: Long,
        context: Context
    ): Triple<String?, Boolean, List<FileSubmission>> {
        // If we can't find the submissionId, it was successful and was deleted from the database
        val successSubmission =
            Triple<String?, Boolean, List<FileSubmission>>(null, false, emptyList())

        val db = Db.getInstance(context)
        val submission = db.submissionQueries.getSubmissionById(submissionId).executeAsOneOrNull()
            ?: return successSubmission
        val files = db.fileSubmissionQueries.getFilesForSubmissionId(submissionId).executeAsList()

        return Triple(submission.assignmentName, submission.errorFlag, files)
    }

    private fun deleteSubmission(submissionId: Long) {
        val db = Db.getInstance(context)
        db.fileSubmissionQueries.deleteFilesForSubmissionId(submissionId)
        db.submissionQueries.deleteSubmissionById(submissionId)

        view?.submissionDeleted()
    }

    private fun deleteFileForSubmission(fileId: Long) {
        val db = Db.getInstance(context)
        db.fileSubmissionQueries.deleteFileById(fileId)
    }

    /**
     * This doesn't work currently. The problem is that the FileUploadService will delete the temp
     * directory where the files are accessible when onDestroy is called. Any retry after the service
     * has "finished" will fail as it can no longer find the file on the device.
     */
    private fun retrySubmission(submissionId: Long) {
        SubmissionService.retryFileSubmission(context, submissionId)

        view?.submissionRetrying()
    }
}

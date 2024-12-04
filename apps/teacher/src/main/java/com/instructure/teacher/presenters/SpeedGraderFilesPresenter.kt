/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.presenters

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Submission
import com.instructure.teacher.utils.asMediaSubmissionPlaceholder
import com.instructure.teacher.viewinterface.SpeedGraderFilesView
import com.instructure.pandautils.blueprint.SyncPresenter

class SpeedGraderFilesPresenter(private var mSubmission: Submission?) : SyncPresenter<Attachment, SpeedGraderFilesView>(Attachment::class.java) {

    override fun loadData(forceNetwork: Boolean) {
        // Generate placeholder item for media submission
        if (Assignment.getSubmissionTypeFromAPIString(mSubmission?.submissionType ?: "") == Assignment.SubmissionType.MEDIA_RECORDING) {
            data.addOrUpdate(Attachment().asMediaSubmissionPlaceholder(mSubmission))
        }

        // URL submissions have the URL preview image as a submission attachment. We don't want to show this as a file.
        val type = mSubmission?.submissionType?.let { Assignment.getSubmissionTypeFromAPIString(it) }
        if (type != Assignment.SubmissionType.ONLINE_URL) {
            mSubmission?.attachments?.let { data.addOrUpdate(it) }
        }

        viewCallback?.checkIfEmpty()
    }

    override fun refresh(forceNetwork: Boolean) {
        data.clear()
        loadData(false)
    }

    fun getSubmission() = mSubmission

    fun setSubmission(submission: Submission?) {
        mSubmission = submission
        refresh(false)
    }
}
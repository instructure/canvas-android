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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsViewState
import com.instructure.student.mobius.common.ui.MobiusView
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.dialog_media_comment_picker.*
import kotlinx.android.synthetic.main.fragment_submission_comments.*

class SubmissionCommentsView(
        inflater: LayoutInflater,
        parent: ViewGroup
) : MobiusView<SubmissionCommentsViewState, SubmissionCommentsEvent>(R.layout.fragment_submission_comments, inflater, parent) {

    override fun render(state: SubmissionCommentsViewState) {
        addMediaCommentButton.setOnClickListener {
            consumer?.accept(SubmissionCommentsEvent.AddMediaCommentClicked)
        }
    }

    override fun onDispose() = Unit
    override fun applyTheme() = Unit
    override fun onConnect(output: Consumer<SubmissionCommentsEvent>) = Unit

    fun showMediaCommentDialog() {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.setView(R.layout.dialog_media_comment_picker).create()

        dialog.setOnShowListener {
            dialog.audioComment.setOnClickListener {
                consumer?.accept(SubmissionCommentsEvent.AddAudioCommentClicked)
                dialog.cancel()
            }
            dialog.videoComment.setOnClickListener {
                consumer?.accept(SubmissionCommentsEvent.AddVideoCommentClicked)
                dialog.cancel()
            }
        }

        dialog.show()
    }

    fun showMediaUploadToast() {
        Toast.makeText(context, "Media Upload Attempted! This is a placeholder", Toast.LENGTH_SHORT).show()
    }

    fun showPermissionDeniedToast() {
        Toast.makeText(context, com.instructure.pandautils.R.string.permissionDenied, Toast.LENGTH_LONG).show()
    }
}


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
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Submission
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.onTextChanged
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.student.activity.BaseRouterActivity
import com.instructure.student.databinding.DialogCommentFilePickerBinding
import com.instructure.student.databinding.FragmentSubmissionCommentsBinding
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionMode
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsViewState
import com.instructure.student.mobius.common.ui.MobiusView
import com.instructure.pandautils.room.studentdb.StudentDb
import com.instructure.student.router.RouteMatcher
import com.spotify.mobius.functions.Consumer

class SubmissionCommentsView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    studentDb: StudentDb
) : MobiusView<SubmissionCommentsViewState, SubmissionCommentsEvent, FragmentSubmissionCommentsBinding>(
    inflater,
    FragmentSubmissionCommentsBinding::inflate,
    parent
) {

    private val adapter = SubmissionCommentsAdapter(
        studentDb = studentDb,
        callback = object : SubmissionCommentsAdapterCallback {
        override fun onRetryPendingComment(pendingCommentId: Long) {
            consumer?.accept(SubmissionCommentsEvent.RetryCommentUploadClicked(pendingCommentId))
        }

        override fun onDeletePendingComment(pendingCommentId: Long) {
            consumer?.accept(SubmissionCommentsEvent.DeletePendingCommentClicked(pendingCommentId))
        }

        override fun onSubmissionClicked(submission: Submission) {
            consumer?.accept(SubmissionCommentsEvent.SubmissionClicked(submission))
        }

        override fun onSubmissionAttachmentClicked(submission: Submission, attachment: Attachment) {
            consumer?.accept(SubmissionCommentsEvent.SubmissionAttachmentClicked(submission, attachment))
        }

        override fun onCommentAttachmentClicked(attachment: Attachment) {
            consumer?.accept(SubmissionCommentsEvent.CommentAttachmentClicked(attachment))
        }
    })

    init {
        with (binding) {
            // Set up send button
            sendCommentButton.imageTintList = ViewStyler.generateColorStateList(
                intArrayOf(-android.R.attr.state_enabled) to ContextCompat.getColor(context, R.color.textDark),
                intArrayOf() to ThemePrefs.textButtonColor
            )
            sendCommentButton.isEnabled = false
            sendCommentButton.setGone()
            commentInput.onTextChanged {
                sendCommentButton.isEnabled = it.isNotBlank()
                sendCommentButton.setVisible(it.isNotBlank())
            }
            sendCommentButton.onClick {
                val message = commentInput.text.toString()
                consumer?.accept(SubmissionCommentsEvent.SendTextCommentClicked(message))
            }

            // Set up add files button
            addFileButton.onClickWithRequireNetwork {
                consumer?.accept(SubmissionCommentsEvent.AddFilesClicked)
            }

            // Set up RecyclerView
            recyclerView.layoutManager = LinearLayoutManager(context).apply { reverseLayout = true }
            recyclerView.adapter = adapter
            recyclerView.itemAnimator = null
        }
    }

    override fun render(state: SubmissionCommentsViewState) {
        binding.addFileButton.isEnabled = state.enableFilesButton
        adapter.data = state.commentStates
    }

    override fun onDispose() = Unit
    override fun applyTheme() = Unit
    override fun onConnect(output: Consumer<SubmissionCommentsEvent>) = Unit

    fun showMediaCommentDialog() {
        val builder = AlertDialog.Builder(context)
        val binding = DialogCommentFilePickerBinding.inflate(LayoutInflater.from(context), null, false)
        val dialog = builder.setView(binding.root).create()

        dialog.setOnShowListener {
            binding.audioComment.setOnClickListener {
                consumer?.accept(SubmissionCommentsEvent.AddAudioCommentClicked)
                dialog.cancel()
            }
            binding.videoComment.setOnClickListener {
                consumer?.accept(SubmissionCommentsEvent.AddVideoCommentClicked)
                dialog.cancel()
            }
            binding.fileComment.onClick {
                consumer?.accept(SubmissionCommentsEvent.UploadFilesClicked)
                dialog.cancel()
            }
        }

        dialog.setOnCancelListener {
            consumer?.accept(SubmissionCommentsEvent.AddFilesDialogClosed)
        }

        dialog.show()
    }

    fun showFilePicker(canvasContext: CanvasContext, assignment: Assignment, attemptId: Long?) {
        RouteMatcher.route(
            activity as FragmentActivity,
            PickerSubmissionUploadFragment.makeRoute(
                canvasContext,
                assignment,
                PickerSubmissionMode.CommentAttachment,
                attemptId
            )
        )
    }

    fun clearTextInput() {
        binding.commentInput.setText("")
    }

    fun scrollToBottom() {
        binding.root.postDelayed({
            binding.recyclerView.smoothScrollToPosition(0)
        }, 100)
    }

    fun openMedia(canvasContext: CanvasContext, contentType: String, url: String, fileName: String) {
        (activity as? BaseRouterActivity)?.openMedia(canvasContext, contentType, url, fileName, null)
    }

    fun showPermissionDeniedToast() {
        Toast.makeText(context, com.instructure.pandautils.R.string.permissionDenied, Toast.LENGTH_LONG).show()
    }
}


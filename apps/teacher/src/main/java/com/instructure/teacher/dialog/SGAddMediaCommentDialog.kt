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
package com.instructure.teacher.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.instructure.pandautils.blueprint.BaseCanvasAppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.activities.SpeedGraderActivity
import com.instructure.teacher.databinding.DialogSgAddAttachmentCommentBinding
import com.instructure.teacher.view.MediaCommentDialogClosedEvent
import org.greenrobot.eventbus.EventBus

class SGAddMediaCommentDialog : BaseCanvasAppCompatDialogFragment() {

    var assignmentId: Long by LongArg()
    var courseId: Long by LongArg()
    var studentId: Long by LongArg()
    var isGroup: Boolean by BooleanArg()
    var assignment: Assignment? = null
    var onUploadFileClick: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogSgAddAttachmentCommentBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
                .setView(binding.root)
                .create()

        dialog.setOnShowListener {
            // Setup clicks
            binding.audioComment.onClick {
                (activity as SpeedGraderActivity).requestAudioPermissions(studentId)
                dismiss()
            }

            binding.videoComment.onClick {
                (activity as SpeedGraderActivity).requestVideoPermissions(studentId)
                dismiss()
            }

            binding.fileComment.onClick {
                onUploadFileClick?.invoke()
                dismiss()
            }

            // Setting these here rather than in XMl so TalkBack doesn't read them automatically without selecting them
            binding.audioText.text = getString(R.string.addAudioComment)
            binding.videoText.text = getString(R.string.addVideoComment)
            binding.fileText.text = getString(R.string.addFile)

            binding.videoComment.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            binding.videoComment.requestAccessibilityFocus()
        }

        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        EventBus.getDefault().post(MediaCommentDialogClosedEvent())
    }

    companion object {
        fun show(
            fm: FragmentManager,
            assignmentId: Long,
            courseId: Long,
            studentId: Long,
            isGroup: Boolean,
            onUploadFileClick: () -> Unit
        ) {
            fm.dismissExisting<SGAddMediaCommentDialog>()

            SGAddMediaCommentDialog().apply {
                this.assignment = Assignment(
                        id = assignmentId,
                        courseId = courseId
                )

                this.studentId = studentId
                this.isGroup = isGroup
                this.onUploadFileClick = onUploadFileClick
            }.show(fm, SGAddMediaCommentDialog::class.java.simpleName)
        }
    }
}
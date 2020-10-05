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
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import android.view.View
import com.instructure.canvasapi2.models.Assignment
import com.instructure.pandautils.utils.onClick
import com.instructure.teacher.R
import com.instructure.teacher.activities.SpeedGraderActivity
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.dismissExisting
import com.instructure.pandautils.utils.requestAccessibilityFocus
import com.instructure.teacher.view.MediaCommentDialogClosedEvent
import kotlinx.android.synthetic.main.dialog_sg_add_attachment_comment.*
import org.greenrobot.eventbus.EventBus

class SGAddMediaCommentDialog : AppCompatDialogFragment() {

    var assignmentId: Long by LongArg()
    var courseId: Long by LongArg()
    var studentId: Long by LongArg()
    var isGroup: Boolean by BooleanArg()
    var assignment: Assignment? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(requireContext())
                .setView(R.layout.dialog_sg_add_attachment_comment)
                .create()

        dialog.setOnShowListener {
            // Setup clicks
            dialog.audioComment.onClick {
                (activity as SpeedGraderActivity).requestAudioPermissions(studentId)
                dismiss()
            }

            dialog.videoComment.onClick {
                (activity as SpeedGraderActivity).requestVideoPermissions(studentId)
                dismiss()
            }

            // Setting these here rather than in XMl so TalkBack doesn't read them automatically without selecting them
            dialog.audioText.text = getString(R.string.addAudioComment)
            dialog.videoText.text = getString(R.string.addVideoComment)

            dialog.videoComment.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            dialog.videoComment.requestAccessibilityFocus()
        }

        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        EventBus.getDefault().post(MediaCommentDialogClosedEvent())
    }

    companion object {
        fun show(fm: FragmentManager, assignmentId: Long, courseId: Long, studentId: Long, isGroup: Boolean) {
            fm.dismissExisting<SGAddMediaCommentDialog>()

            SGAddMediaCommentDialog().apply {
                this.assignment = Assignment(
                        id = assignmentId,
                        courseId = courseId
                )

                this.studentId = studentId
                this.isGroup = isGroup
            }.show(fm, SGAddMediaCommentDialog::class.java.simpleName)
        }
    }
}
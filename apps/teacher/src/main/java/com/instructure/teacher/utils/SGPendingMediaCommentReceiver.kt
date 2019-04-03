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
package com.instructure.teacher.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.canvasapi2.models.postmodels.CommentSendStatus
import com.instructure.canvasapi2.models.postmodels.PendingSubmissionComment
import com.instructure.pandautils.utils.Const
import com.instructure.teacher.events.UploadMediaCommentUpdateEvent
import com.instructure.teacher.events.post
import org.greenrobot.eventbus.EventBus
import java.io.File

/**
 * Captures broadcasts from the NotoriousUploadService
 *
 * Handles successful as well as failed uploads
 */
class SGPendingMediaCommentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val extras = intent?.extras ?: return

        val mediaPath = extras.getString(Const.MEDIA_FILE_PATH) ?: return
        val pageId = extras.getString(Const.PAGE_ID) ?: return

        // This can be null if the comment failed to send
        val submissionComments = extras.getParcelableArrayList<SubmissionComment>(Const.SUBMISSION_COMMENT_LIST)

        var pendingComments = TeacherPrefs.pendingSubmissionComments
        val pendingSubmissionComment: PendingSubmissionComment = pendingComments.find { it.filePath == mediaPath } ?: return

        if (extras.containsKey(Const.ERROR)) {
            // There was an error sending the media file

            // Update the status of the pending comment to reflect failed statue
            pendingComments.find { it.id == pendingSubmissionComment.id }?.let {
                pendingComments -= it
                it.status = CommentSendStatus.ERROR
                pendingComments += it
                TeacherPrefs.pendingSubmissionComments = pendingComments
            }

            // Let the world know the shame of this failed comment
            sendUploadMediaCommentUpdateEvent(pageId, pendingSubmissionComment)
        } else {
            // No errors sending media comment

            // Remove pending comment from prefs
            submissionComments?.lastOrNull()?.let { submissionComment ->

                // Remove pending comment from sharedPrefs
                pendingComments.find { it.id == pendingSubmissionComment.id }?.let {

                    // No error, remove pending comment
                    pendingComments -= it
                    TeacherPrefs.pendingSubmissionComments = pendingComments
                }

                // Delete media file
                File(pendingSubmissionComment.filePath).delete()

                sendUploadMediaCommentUpdateEvent(pageId, pendingSubmissionComment, submissionComment)
            }
        }
    }
}

private fun sendUploadMediaCommentUpdateEvent(pageId: String, pendingSubmissionComment: PendingSubmissionComment, submissionComment: SubmissionComment? = null) {
    val event = EventBus.getDefault().getStickyEvent(UploadMediaCommentUpdateEvent::class.java)
    if (event == null) {
        // Event doesn't exist, launch the first one
        UploadMediaCommentUpdateEvent(mutableMapOf(Pair(pageId, mutableListOf(Pair(pendingSubmissionComment, submissionComment))))).post()
    } else {
        // Event is hanging around already - update it
        event.get {
            if (it.containsKey(pageId)) {
                // We already have updates for this pageId; Add MOAR
                it[pageId]?.add(Pair(pendingSubmissionComment, submissionComment))
            } else {
                // No updates for this pageId, add the pioneers
                it.put(pageId, mutableListOf(Pair(pendingSubmissionComment, submissionComment)))
            }
        }
        // Post the updated event
        event.post()
    }
}

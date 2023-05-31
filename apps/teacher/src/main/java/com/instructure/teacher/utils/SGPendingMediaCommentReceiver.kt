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
import com.instructure.pandautils.room.appdatabase.daos.PendingSubmissionCommentDao
import com.instructure.pandautils.utils.Const
import com.instructure.teacher.events.UploadMediaCommentUpdateEvent
import com.instructure.teacher.events.post
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

/**
 * Captures broadcasts from the NotoriousUploadService
 *
 * Handles successful as well as failed uploads
 */
@AndroidEntryPoint
class SGPendingMediaCommentReceiver : BroadcastReceiver() {

    @Inject
    lateinit var pendingSubmissionCommentDao: PendingSubmissionCommentDao

    private val scope = CoroutineScope(SupervisorJob())

    override fun onReceive(context: Context?, intent: Intent?) {
        scope.launch {
            val extras = intent?.extras ?: return@launch

            val mediaPath = extras.getString(Const.MEDIA_FILE_PATH) ?: return@launch
            val pageId = extras.getString(Const.PAGE_ID) ?: return@launch
            val mediaCommentId = extras.getLong(Const.ID)

            // This can be null if the comment failed to send
            val submissionComments = extras.getParcelableArrayList<SubmissionComment>(Const.SUBMISSION_COMMENT_LIST)

            val pendingSubmissionCommentEntity = pendingSubmissionCommentDao.findById(mediaCommentId) ?: return@launch

            if (extras.containsKey(Const.ERROR)) {
                // There was an error sending the media file
                // Update the status of the pending comment to reflect failed statue
                pendingSubmissionCommentEntity.status =
                    CommentSendStatus.ERROR.toString()
                pendingSubmissionCommentDao.update(pendingSubmissionCommentEntity)

                // Let the world know the shame of this failed comment
                sendUploadMediaCommentUpdateEvent(pageId, pendingSubmissionCommentEntity.toApiModel())
            } else {
                // No errors sending media comment

                // Remove pending comment from db
                submissionComments?.lastOrNull()?.let { submissionComment ->
                    // Remove pending comment from sharedPrefs
                    pendingSubmissionCommentDao.delete(pendingSubmissionCommentEntity)

                    sendUploadMediaCommentUpdateEvent(
                        pageId,
                        pendingSubmissionCommentEntity.toApiModel(),
                        submissionComment
                    )
                }
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

/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.pandautils.services

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Parcelable
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.instructure.canvasapi2.managers.DiscussionManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.notorious.NotoriousResult
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.NotoriousUploader
import org.greenrobot.eventbus.EventBus

class NotoriousUploadService : IntentService(NotoriousUploadService::class.java.simpleName) {

    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    private lateinit var builder: NotificationCompat.Builder

    private lateinit var mediaPath: String

    private lateinit var action: ACTION

    private var assignment: Assignment? = null
    private var discussionEntry: DiscussionEntry? = null
    private var discussionId: Long = 0
    private var message: String = ""
    private var canvasContext: CanvasContext? = null
    private var studentId: Long = 0
    private var isGroupComment: Boolean = false

    private var pageId: String? = null

    private var uploadJob: WeaveJob? = null

    private var notificationId: Int = 0

    private var mediaCommentId: Long = -1L

    private var attemptId: Long? = null

    private val context: Context
        get() = applicationContext

    override fun onHandleIntent(intent: Intent?) {
        if (intent?.getSerializableExtra(Const.ACTION) == null) return

        val submissionId = if (intent.hasExtra(Const.SUBMISSION_ID)) intent.getLongExtra(Const.SUBMISSION_ID, 0) else null
        notificationId = submissionId?.toInt() ?: NOTIFICATION_ID

        if (intent.hasExtra(Const.ID)) mediaCommentId = intent.extras?.getLong(Const.ID) ?: -1L

        action = intent.getSerializableExtra(Const.ACTION) as ACTION

        when (action) {
            ACTION.SUBMISSION_COMMENT -> {
                assignment = intent.getParcelableExtra(Const.ASSIGNMENT)
                studentId = intent.getLongExtra(Const.STUDENT_ID, ApiPrefs.user!!.id)
                isGroupComment = intent.getBooleanExtra(Const.IS_GROUP, false)
                pageId = intent.getStringExtra(Const.PAGE_ID)
                attemptId = intent.getLongExtra(Const.SUBMISSION_ATTEMPT, -1L).takeIf { it != -1L }
            }
            ACTION.ASSIGNMENT_SUBMISSION -> assignment = intent.getParcelableExtra(Const.ASSIGNMENT)
            ACTION.DISCUSSION_COMMENT -> {
                discussionEntry = intent.getParcelableExtra(Const.DISCUSSION_ENTRY)
                message = intent.getStringExtra(Const.MESSAGE).orEmpty()
                discussionId = intent.getLongExtra(Const.DISCUSSION_ID, 0)
                canvasContext = intent.getParcelableExtra(Const.CANVAS_CONTEXT)
            }
        }

        builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_canvas_logo)
            .setContentTitle(getString(R.string.notificationTitle))
            .setContentText(getString(R.string.preparingUpload))
            .setOngoing(true)

        createNotificationChannel(CHANNEL_ID)

        mediaPath = intent.getStringExtra(Const.MEDIA_FILE_PATH) ?: ""

        if (mediaPath.isNotEmpty()) {
            notificationManager.notify(notificationId, builder.build())
            startForeground(notificationId, builder.build())
            startFileUpload(submissionId)
        } else {
            handleFailure(null)
        }
    }

    private fun createNotificationChannel(channelId: String) {
        // Prevents recreation of notification channel if it exists.
        val channelList = notificationManager.notificationChannels
        for (channel in channelList) {
            if (channelId == channel.id) {
                return
            }
        }

        val name = ContextKeeper.appContext.getString(R.string.notificationChannelNameFileUploadsName)
        val description = ContextKeeper.appContext.getString(R.string.notificationChannelNameFileUploadsDescription)

        // Create the channel and add the group
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance)
        channel.description = description
        channel.enableLights(false)
        channel.enableVibration(false)

        // Create the channel
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        // Clean up
        builder.setOngoing(false)
        notificationManager.cancel(notificationId)
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        // Clean up again in case of swipe to dismiss
        builder.setOngoing(false)
        notificationManager.cancel(notificationId)
        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun startFileUpload(submissionId: Long?) {
        uploadJob = weave(true) {
            // Set initial progress in notification
            builder.setContentText(getString(R.string.uploadingFile))
            builder.setProgress(0, 0, true)
            notificationManager.notify(notificationId, builder.build())
            uploadStartedToast()

            NotoriousUploader.performUpload(mediaPath, object : ProgressRequestUpdateListener {
                override fun onProgressUpdated(progressPercent: Float, length: Long): Boolean {
                    // Update progress in notification
                    builder.setProgress(1000, (progressPercent * 1000).toInt(), false)
                    notificationManager.notify(notificationId, builder.build())
                    if (submissionId != null) {
                        val event = ProgressEvent(0, submissionId, (progressPercent * length).toLong(), length)
                        EventBus.getDefault().postSticky(event)
                    }
                    return true
                }
            }).onSuccess {
                handleSuccess(it)
            }.onFailure {
                handleFailure(it)
            }
        }
    }

    private fun handleFailure(failure: Failure?) {
        when(failure) {
            is Failure.Network -> uploadError(failure.message)
            is Failure.Exception -> uploadError(failure.exception)
            else -> uploadError("Unknown error")
        }
    }

    private suspend fun handleSuccess(result: NotoriousResult) {
        // Create course context
        val course = if (assignment != null) {
            CanvasContext.getGenericContext(CanvasContext.Type.COURSE, assignment!!.courseId, Const.COURSE)
        } else {
            CanvasContext.getGenericContext(CanvasContext.Type.COURSE, canvasContext!!.id, Const.COURSE)
        }

        val mediaType = FileUtils.mediaTypeFromNotoriousCode(result.mediaType)
        try {
            when (action) {
                ACTION.SUBMISSION_COMMENT -> broadcastSubmission(awaitApi {
                    SubmissionManager.postMediaSubmissionComment(
                        course, assignment?.id ?: 0, studentId, result.id!!, mediaType, attemptId, isGroupComment, it
                    )
                })
                ACTION.ASSIGNMENT_SUBMISSION -> broadcastSubmission(awaitApi {
                    SubmissionManager.postMediaSubmission(
                        course, assignment!!.id, Const.MEDIA_RECORDING, result.id!!, mediaType, it
                    )
                })
                ACTION.DISCUSSION_COMMENT -> {
                    // This is the format that Canvas expects
                    val attachment = "<p><a id='media_comment_${result.id}' " +
                            "class='instructure_inline_media_comment $mediaType'" +
                            "href='/media_objects/${result.id}'>this is a media comment</a></p>\n$message"

                    if (discussionEntry!!.parent == null) {
                        broadcastDiscussion(awaitApi {
                            DiscussionManager.postToDiscussionTopic(canvasContext!!, discussionId, attachment, it)
                        })
                    } else {
                        broadcastDiscussion(awaitApi {
                            DiscussionManager.replyToDiscussionEntry(canvasContext!!, discussionId, discussionEntry!!.id, attachment, it)
                        })
                    }
                }
            }
        } catch (e: Throwable) {
            if (!APIHelper.hasNetworkConnection()) {
                onNoNetwork()
            } else {
                uploadError(e)
            }
        }

        stopSelf()
    }


    private fun broadcastSubmission(submission: Submission) {
        builder.setContentText(getString(R.string.fileUploadSuccess))
            .setProgress(100, 100, false)
            .setOngoing(false)
        notificationManager.notify(notificationId, builder.build())

        val intent = Intent(Const.SUBMISSION_COMMENT_SUBMITTED)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

        // TODO: This is caught in ParentFragment, which we are moving away from as we integrate Mobius into more parts of the app.
        //       We'll want to change out this intent with something like FileUploadService.ALL_UPLOADS_COMPLETED, which is caught in the
        //       SubmissionFileUploadReceiver
        val successIntent = Intent(Const.UPLOAD_SUCCESS)
        LocalBroadcastManager.getInstance(context).sendBroadcast(successIntent)

        val uploadCompleteIntent = Intent(ALL_UPLOADS_COMPLETED)
        uploadCompleteIntent.putExtra(Const.SUBMISSION_ID, notificationId.toLong())
        assignment?.name?.let { uploadCompleteIntent.putExtra(Const.ASSIGNMENT_NAME, it)}
        sendBroadcast(uploadCompleteIntent)

        val mediaUploadIntent = Intent(Const.ACTION_MEDIA_UPLOAD_SUCCESS)
        mediaUploadIntent.putExtra(Const.MEDIA_FILE_PATH, mediaPath)
        mediaUploadIntent.putExtra(Const.PAGE_ID, pageId)
        mediaUploadIntent.putExtra(Const.ID, mediaCommentId)
        mediaUploadIntent.putParcelableArrayListExtra(
            Const.SUBMISSION_COMMENT_LIST,
            ArrayList(submission.submissionComments)
        )
        LocalBroadcastManager.getInstance(context).sendBroadcast(mediaUploadIntent)
    }

    private fun broadcastDiscussion(entry: DiscussionEntry) {
        builder.setContentText(getString(R.string.fileUploadSuccess))
            .setProgress(100, 100, false)
            .setOngoing(false)
        notificationManager.notify(notificationId, builder.build())

        val intent = Intent(Const.DISCUSSION_REPLY_SUBMITTED)
        intent.putExtra(Const.DISCUSSION_ENTRY, entry as Parcelable?)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

        val successIntent = Intent(Const.UPLOAD_SUCCESS)
        LocalBroadcastManager.getInstance(context).sendBroadcast(successIntent)
    }

    private fun onNoNetwork() {
        uploadJob?.cancel()
        sendErrorBroadcast()
        builder.setContentText(getString(R.string.noNetwork)).setOngoing(false)
        notificationManager.notify(NOTIFICATION_ID, builder.build())
        stopSelf()
    }

    private fun uploadError(e: Throwable) {
        uploadJob?.cancel()
        Logger.e("NOTORIOUS EXCEPTION: $e")
        e.printStackTrace()
        sendErrorBroadcast()
        showErrorNotification()
        stopSelf()
    }

    private fun uploadError(message: String? = null) {
        uploadJob?.cancel()
        Logger.e(message ?: "UNKNOWN ERROR CAUSE")
        sendErrorBroadcast()
        showErrorNotification()
        stopSelf()
    }

    private fun showErrorNotification() {
        builder.setContentText(getString(R.string.errorUploadingFile))
            .setProgress(100, 100, false)
            .setOngoing(false)
        notificationManager.notify(
            notificationId,
            builder.build()
        )
    }

    private fun uploadStartedToast() {
        val handler = Handler(Looper.getMainLooper())
        handler.post { Toast.makeText(context, R.string.uploadMessage, Toast.LENGTH_SHORT).show() }
    }

    private fun sendErrorBroadcast() {
        val errorIntent = Intent(Const.ACTION_MEDIA_UPLOAD_FAIL).apply {
            putExtra(Const.MEDIA_FILE_PATH, mediaPath)
            putExtra(Const.PAGE_ID, pageId)
            putExtra(Const.ERROR, true)
            putExtra(Const.ID, mediaCommentId)
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(errorIntent)
    }

    enum class ACTION {
        SUBMISSION_COMMENT,
        ASSIGNMENT_SUBMISSION,
        DISCUSSION_COMMENT
    }

    companion object {
        private const val NOTIFICATION_ID = 777

        // Upload broadcasts, extended string to ensure unique strings across the device
        private const val BROADCAST_BASE = "com.instructure.pandautils.services"
        const val ALL_UPLOADS_COMPLETED = "$BROADCAST_BASE.ALL_UPLOADS_COMPLETED"
        const val QUIZ_UPLOAD_COMPLETE = "$BROADCAST_BASE.QUIZ_UPLOAD_COMPLETE"
        const val UPLOAD_COMPLETED = "$BROADCAST_BASE.UPLOAD_COMPLETED"
        const val UPLOAD_ERROR = "$BROADCAST_BASE.UPLOAD_ERROR"

        const val CHANNEL_ID = "uploadChannel"
    }
}

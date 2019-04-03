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
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.widget.Toast
import com.instructure.canvasapi2.managers.DiscussionManager
import com.instructure.canvasapi2.managers.NotoriousManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.models.notorious.NotoriousResultWrapper
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.R
import com.instructure.pandautils.services.FileUploadService.Companion.CHANNEL_ID
import com.instructure.pandautils.utils.Const
import java.io.File
import java.util.*

class NotoriousUploadService : IntentService(NotoriousUploadService::class.java.simpleName) {

    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    private lateinit var builder: NotificationCompat.Builder

    private var notoriousDomain: String = ""
    private var uploadToken: String = ""

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

    private val context: Context
        get() = applicationContext

    override fun onHandleIntent(intent: Intent?) {
        if (intent?.getSerializableExtra(Const.ACTION) == null) return

        action = intent.getSerializableExtra(Const.ACTION) as ACTION

        when (action) {
            ACTION.SUBMISSION_COMMENT -> {
                assignment = intent.getParcelableExtra(Const.ASSIGNMENT)
                studentId = intent.getLongExtra(Const.STUDENT_ID, ApiPrefs.user!!.id)
                isGroupComment = intent.getBooleanExtra(Const.IS_GROUP, false)
                pageId = intent.getStringExtra(Const.PAGE_ID)
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

        mediaPath = intent.getStringExtra(Const.MEDIA_FILE_PATH)

        notificationManager.notify(NOTIFICATION_ID, builder.build())

        startForeground(NOTIFICATION_ID, builder.build())

        startFileUpload()
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        // Prevents recreation of notification channel if it exists.
        val channelList = notificationManager.notificationChannels
        for (channel in channelList) {
            if (channelId == channel.id) {
                return
            }
        }

        val name = ContextKeeper.appContext.getString(R.string.notificationChannelNameFileUploadsName)
        val description = ContextKeeper.appContext.getString(R.string.notificationChannelNameFileUploadsDescription)

        //Create the channel and add the group
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance)
        channel.description = description
        channel.enableLights(false)
        channel.enableVibration(false)

        //create the channel
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        //clean up
        builder.setOngoing(false)
        notificationManager.cancel(NOTIFICATION_ID)
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        //clean up again in case of swipe to dismiss
        builder.setOngoing(false)
        notificationManager.cancel(NOTIFICATION_ID)
        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun startFileUpload() {
        uploadJob = tryWeave(true) {
            // Get NotoriousConfig
            val config = awaitApi<NotoriousConfig> { NotoriousManager.getConfiguration(it) }
            if (config.isEnabled) {
                notoriousDomain = config.domain.orEmpty()
            } else {
                uploadError("NOTORIOUS CONFIG/RESPONSE ERROR: $config")
                return@tryWeave
            }

            // Start session
            val session = awaitApi<NotoriousSession> { NotoriousManager.startSession(it) }
            ApiPrefs.notoriousDomain = notoriousDomain
            ApiPrefs.notoriousToken = session.token.orEmpty()
            notificationManager.notify(NOTIFICATION_ID, builder.build())

            // Get upload token
            val resultWrapper = awaitApi<NotoriousResultWrapper> { NotoriousManager.getUploadToken(it) }
            builder.setContentText(getString(R.string.uploadingFile))
            builder.setProgress(0, 0, true)
            notificationManager.notify(NOTIFICATION_ID, builder.build())
            uploadToken = resultWrapper.result?.id.orEmpty()
            resultWrapper.result?.error?.let {
                uploadError("NOTORIOUS XML/RESPONSE ERROR: $resultWrapper")
                return@tryWeave
            }

            // Perform upload
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
            uploadStartedToast()
            val contentType = FileUtils.getMimeType(mediaPath) ?: "application/octet-stream"
            val file = File(mediaPath)
            val response = NotoriousManager.uploadFileSynchronous(uploadToken, file, contentType)
            if (response == null || response.code() != 201) {
                uploadError("NOTORIOUS RESPONSE ERROR: ${response?.message()}, CODE: ${response?.code()}")
                return@tryWeave
            }

            // Get uploaded media ID
            val mediaInfo = NotoriousManager.getMediaIdSynchronous(uploadToken, file.name, contentType)!!.result!!

            // Create course context
            val course = if (assignment != null) {
                CanvasContext.getGenericContext(CanvasContext.Type.COURSE, assignment!!.courseId, Const.COURSE)
            } else {
                CanvasContext.getGenericContext(CanvasContext.Type.COURSE, canvasContext!!.id, Const.COURSE)
            }

            val mediaType = FileUtils.mediaTypeFromNotoriousCode(mediaInfo.mediaType)
            when (action) {
                ACTION.SUBMISSION_COMMENT -> broadcastSubmission(awaitApi {
                    SubmissionManager.postMediaSubmissionComment(
                        course, assignment?.id ?: 0, studentId, mediaInfo.id!!, mediaType, isGroupComment, it
                    )
                })
                ACTION.ASSIGNMENT_SUBMISSION -> broadcastSubmission(awaitApi {
                    SubmissionManager.postMediaSubmission(
                        course, assignment!!.id, Const.MEDIA_RECORDING, mediaInfo.id!!, mediaType, it
                    )
                })
                ACTION.DISCUSSION_COMMENT -> {
                    // This is the format that Canvas expects
                    val attachment = "<p><a id='media_comment_${mediaInfo.id}' " +
                            "class='instructure_inline_media_comment $mediaType'" +
                            "href='/media_objects/${mediaInfo.id}'>this is a media comment</a></p>\n$message"

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

            stopSelf()

        } catch {
            if (!APIHelper.hasNetworkConnection()) {
                onNoNetwork()
            } else {
                uploadError(it)
            }
        }
    }

    private fun broadcastSubmission(submission: Submission) {
        builder.setContentText(getString(R.string.fileUploadSuccess))
            .setProgress(100, 100, false)
            .setOngoing(false)
        notificationManager.notify(NOTIFICATION_ID, builder.build())

        val intent = Intent(Const.SUBMISSION_COMMENT_SUBMITTED)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

        val successIntent = Intent(Const.UPLOAD_SUCCESS)
        LocalBroadcastManager.getInstance(context).sendBroadcast(successIntent)

        val mediaUploadIntent = Intent(Const.ACTION_MEDIA_UPLOAD_SUCCESS)
        mediaUploadIntent.putExtra(Const.MEDIA_FILE_PATH, mediaPath)
        mediaUploadIntent.putExtra(Const.PAGE_ID, pageId)
        mediaUploadIntent.putParcelableArrayListExtra(
            Const.SUBMISSION_COMMENT_LIST,
            ArrayList<SubmissionComment>(submission.submissionComments)
        )
        LocalBroadcastManager.getInstance(context).sendBroadcast(mediaUploadIntent)
    }

    private fun broadcastDiscussion(entry: DiscussionEntry) {
        builder.setContentText(getString(R.string.fileUploadSuccess))
            .setProgress(100, 100, false)
            .setOngoing(false)
        notificationManager.notify(NOTIFICATION_ID, builder.build())

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
            NOTIFICATION_ID,
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
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(errorIntent)
    }

    enum class ACTION {
        SUBMISSION_COMMENT,
        ASSIGNMENT_SUBMISSION,
        DISCUSSION_COMMENT
    }

    companion object {
        private const val NOTIFICATION_ID = 666
    }
}

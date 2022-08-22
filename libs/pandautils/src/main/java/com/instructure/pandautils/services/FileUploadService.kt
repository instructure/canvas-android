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
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.core.app.NotificationCompat
import com.instructure.canvasapi2.managers.FileUploadConfig
import com.instructure.canvasapi2.managers.FileUploadManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.ProgressEvent
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import kotlin.collections.ArrayList

/**
 * A service to upload files for various parts of canvas. All results of this service are posted sticky via an EventBus event.
 * The event posted is a [FileUploadEvent] which contains a [FileUploadNotification].
 * [FileUploadNotification] May contain a nullable intent and arrayList of [Attachment] objects.
 * [Attachment] objects are the lowest common denominator for file object types used in Canvas. Data loss may be a side effect.
 */
class FileUploadService @JvmOverloads constructor(name: String = FileUploadService::class.java.simpleName) : IntentService(name) {

    private var uploadCount: Int = 0
    private var isCanceled = false

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle 'cancel' action in onStartCommand instead of onHandleIntent, because threading.
        if (ACTION_CANCEL_UPLOAD == intent?.action) isCanceled = true
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {
        if (isCanceled || intent == null) return

        val action = intent.action
        val bundle = intent.extras

        val notificationId = notificationId(bundle)
        val fileSubmitObjects = bundle?.getParcelableArrayList<FileSubmitObject>(Const.FILES) ?: return
        val assignment = bundle.getParcelable<Assignment>(Const.ASSIGNMENT)
        val submissionId = if (bundle.containsKey(Const.SUBMISSION_ID)) bundle.getLong(Const.SUBMISSION_ID) else null

        uploadCount = fileSubmitObjects.size
        showNotification(notificationId, uploadCount)

        if (assignment != null && assignment.groupCategoryId != 0L) {
            // This is a group assignment, we need to get the list of groups before starting uploads
            GroupManager.getGroupsSynchronous(true)
                    .find { it.groupCategoryId == assignment.groupCategoryId }
                    ?.let { startUploads(action!!, fileSubmitObjects, bundle, it.id) }
                    ?: broadcastError(notificationId, getString(R.string.errorSubmittingFiles), submissionId, assignment.name)
        } else {
            startUploads(action!!, fileSubmitObjects, bundle, null)
        }
    }

    // region Upload
    private fun startUploads(action: String, fileSubmitObjects: ArrayList<FileSubmitObject>, bundle: Bundle, groupId: Long?) {
        val courseId = bundle.getLong(Const.COURSE_ID)
        val assignment = bundle.getParcelable<Assignment>(Const.ASSIGNMENT)
        val quizQuestionId = bundle.getLong(Const.QUIZ_ANSWER_ID, INVALID_ID)
        val quizId = bundle.getLong(Const.QUIZ)
        val position = bundle.getInt(Const.POSITION)
        val parentFolderId = if (bundle.containsKey(Const.PARENT_FOLDER_ID)) bundle.getLong(Const.PARENT_FOLDER_ID) else null
        val notificationId = notificationId(bundle)
        val submissionId = if (bundle.containsKey(Const.SUBMISSION_ID)) bundle.getLong(Const.SUBMISSION_ID) else null

        val attachments = mutableListOf<Attachment>()

        try {
            fileSubmitObjects.forEachIndexed { idx, fso ->
                updateNotificationCount(notificationId, fso.name, idx + 1)
                val config: FileUploadConfig = when (action) {
                    ACTION_ASSIGNMENT_SUBMISSION -> {
                        if (groupId == null) {
                            FileUploadConfig.forSubmission(fso, courseId, assignment!!.id)
                        } else {
                            FileUploadConfig.forGroup(fso, groupId)
                        }
                    }
                    ACTION_COURSE_FILE -> FileUploadConfig.forCourse(fso, courseId, parentFolderId)
                    ACTION_GROUP_FILE -> FileUploadConfig.forGroup(fso, courseId, parentFolderId)
                    ACTION_USER_FILE -> FileUploadConfig.forUser(fso, parentFolderId)
                    ACTION_MESSAGE_ATTACHMENTS -> FileUploadConfig.forUser(fso, parentFolderPath = MESSAGE_ATTACHMENT_PATH)
                    ACTION_QUIZ_FILE -> FileUploadConfig.forQuiz(fso, courseId, quizId)
                    ACTION_DISCUSSION_ATTACHMENT -> FileUploadConfig.forUser(fso, parentFolderPath = DISCUSSION_ATTACHMENT_PATH)
                    ACTION_SUBMISSION_COMMENT -> FileUploadConfig.forSubmissionComment(fso, courseId, assignment!!.id)
                    else -> throw IllegalArgumentException("Unknown file upload action: $action")
                }
                attachments += FileUploadManager.uploadFile(config).dataOrThrow
            }
            // Submit fileIds to the assignment
            val attachmentsIds = attachments.map { it.id }.plus(bundle.getLongArray(Const.ATTACHMENTS)?.toList() ?: emptyList())
            when (action) {
                ACTION_ASSIGNMENT_SUBMISSION -> submitAttachmentsForSubmission(courseId, assignment!!, attachments, attachmentsIds, bundle)
                ACTION_DISCUSSION_ATTACHMENT -> broadcastDiscussionSuccess(notificationId, attachments)
                ACTION_QUIZ_FILE -> broadcastQuizSuccess(notificationId, attachments[0], quizQuestionId, position)
                ACTION_MESSAGE_ATTACHMENTS -> broadcastAllUploadsCompleted(attachments)
                ACTION_SUBMISSION_COMMENT -> broadcastSubmissionCommentSuccess(notificationId, attachments)
                else -> {
                    FileUploadEvent(FileUploadNotification(null, attachments)).postSticky()
                    updateNotificationComplete(notificationId)
                }
            }
        } catch (exception: Exception) {
            updateNotification(notificationId, getString(R.string.errorUploadingFile))

            if (quizQuestionId != INVALID_ID) {
                broadcastQuizError(notificationId, exception.message.orEmpty(), quizQuestionId, position)
            } else {
                broadcastError(notificationId, exception.message.orEmpty(), submissionId, assignment?.name, attachments)
            }
        }
    }

    private fun submitAttachmentsForSubmission(
        courseId: Long,
        assignment: Assignment,
        attachments: List<Attachment>,
        attachmentsIds: List<Long>,
        bundle: Bundle
    ) {
        val notificationId = notificationId(bundle)
        val submissionId = if (bundle.containsKey(Const.SUBMISSION_ID)) bundle.getLong(Const.SUBMISSION_ID) else null
        SubmissionManager.postSubmissionAttachmentsSynchronous(courseId, assignment.id, attachmentsIds)?.let {
            updateSubmissionComplete(notificationId)
            broadcastAllUploadsCompleted(attachments, submissionId, assignment.name)
        } ?:
        broadcastError(notificationId, getString(R.string.errorSubmittingFiles), submissionId, assignment.name, attachments)
    }
    // endregion Upload

    // region Notifications
    private fun broadcastAllUploadsCompleted(attachments: List<Attachment>, submissionId: Long? = null, assignmentName: String? = null) {
        val status = Intent(ALL_UPLOADS_COMPLETED)
        status.putParcelableArrayListExtra(Const.ATTACHMENTS, ArrayList(attachments))
        submissionId?.let { status.putExtra(Const.SUBMISSION_ID, it) }
        assignmentName?.let { status.putExtra(Const.ASSIGNMENT_NAME, it) }

        FileUploadEvent(FileUploadNotification(status, attachments)).postSticky()
        sendBroadcast(status)
    }

    private fun broadcastMessageSuccess(conversation: Conversation) {
        val status = Intent(ALL_UPLOADS_COMPLETED)
        status.putExtra(Const.CONVERSATION, conversation as Parcelable)
        FileUploadEvent(FileUploadNotification(status, ArrayList(0))).postSticky()
    }

    private fun broadcastNewMessageSuccess() {
        val status = Intent(ALL_UPLOADS_COMPLETED)
        FileUploadEvent(FileUploadNotification(status, ArrayList(0))).postSticky()
    }

    private fun broadcastSubmissionCommentSuccess(notificationId: Int, attachments: List<Attachment>) {
        updateNotificationComplete(notificationId)
        val status = Intent(ALL_UPLOADS_COMPLETED)
        status.putParcelableArrayListExtra(Const.ATTACHMENTS, ArrayList(attachments))
        sendBroadcast(status)
    }

    private fun broadcastDiscussionSuccess(notificationId: Int, attachments: List<Attachment>) {
        updateNotificationComplete(notificationId)
        val status = Intent(ALL_UPLOADS_COMPLETED)
        status.putParcelableArrayListExtra(Const.ATTACHMENTS, ArrayList(attachments))
        FileUploadEvent(FileUploadNotification(status, attachments)).postSticky()
    }

    private fun broadcastQuizSuccess(notificationId: Int, attachment: Attachment, quizQuestionId: Long, position: Int) {
        updateNotificationComplete(notificationId)
        val status = Intent(QUIZ_UPLOAD_COMPLETE)
        status.putExtra(Const.ATTACHMENT, attachment as Parcelable)
        status.putExtra(Const.QUIZ_ANSWER_ID, quizQuestionId)
        status.putExtra(Const.POSITION, position)
        FileUploadEvent(FileUploadNotification(status, ArrayList<Attachment>(1).apply { add(attachment) })).postSticky()
    }

    private fun broadcastQuizError(notificationId: Int, message: String, quizQuestionId: Long, position: Int) {
        updateNotificationError(notificationId, message)
        val bundle = Bundle()
        val status = Intent(UPLOAD_ERROR)
        status.putExtra(Const.QUIZ_ANSWER_ID, quizQuestionId)
        status.putExtra(Const.POSITION, position)
        bundle.putString(Const.MESSAGE, message)
        status.putExtras(bundle)
        FileUploadEvent(FileUploadNotification(status, ArrayList(0))).postSticky()
    }

    private fun broadcastError(notificationId: Int, message: String, submissionId: Long? = null, assignmentName: String? = null, attachments: List<Attachment>? = null) {
        updateNotificationError(notificationId, message)

        val bundle = Bundle()
        bundle.putString(Const.MESSAGE, message)
        submissionId?.let { bundle.putLong(Const.SUBMISSION_ID, it) }
        assignmentName?.let { bundle.putString(Const.ASSIGNMENT_NAME, it) }
        attachments?.let { bundle.putParcelableArrayList(Const.ATTACHMENTS, ArrayList(it)) }

        val status = Intent(UPLOAD_ERROR)
        status.putExtras(bundle)

        FileUploadEvent(FileUploadNotification(status, ArrayList(0))).postSticky()
        sendBroadcast(status)
    }

    private fun showNotification(notificationId: Int, size: Int) {
        createNotificationChannel(notificationManager)
        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_canvas_logo)
                .setContentTitle(String.format(Locale.US, getString(R.string.uploadingFileNum), 1, size))
                .setProgress(0, 0, true)
        startForeground(notificationId, notificationBuilder.build())
    }

    private fun updateNotificationCount(notificationId: Int, fileName: String, currentItem: Int) {
        notificationBuilder.setContentTitle(String.format(Locale.US, getString(R.string.uploadingFileNum), currentItem, uploadCount))
                .setContentText(fileName)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun updateNotification(notificationId: Int, message: String) {
        notificationBuilder.setContentText(message)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun updateNotificationError(notificationId: Int, message: String) {
        notificationBuilder.setContentText(message)
                .setProgress(0, 0, false)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun updateNotificationComplete(notificationId: Int) {
        notificationBuilder.setProgress(0, 0, false)
                .setContentTitle(getString(R.string.filesUploadedSuccessfully))
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun updateSubmissionComplete(notificationId: Int) {
        notificationBuilder.setProgress(0, 0, false)
                .setContentTitle(getString(R.string.filesSubmittedSuccessfully))
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun notificationId(extras: Bundle?): Int {
        return if (extras?.containsKey(Const.SUBMISSION_ID) == true) {
            extras.getLong(Const.SUBMISSION_ID).toInt()
        } else {
            NOTIFICATION_ID
        }
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe
    fun onUploadProgress(event: ProgressEvent) {
        notificationBuilder
            .setProgress(100, (event.uploaded * 100f / event.contentLength).toInt(), false)
            .setOnlyAlertOnce(true)
        notificationManager.notify(event.submissionId.toInt(), notificationBuilder.build())
    }

    // endregion Notifications

    override fun onDestroy() {
        shutDown(applicationContext)
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = -2
        private const val INVALID_ID = -1L
        const val CHANNEL_ID = "uploadChannel"

        // Upload broadcasts, extended string to ensure unique strings across the device
        private const val BROADCAST_BASE = "com.instructure.pandautils.services"
        const val ALL_UPLOADS_COMPLETED = "$BROADCAST_BASE.ALL_UPLOADS_COMPLETED"
        const val QUIZ_UPLOAD_COMPLETE = "$BROADCAST_BASE.QUIZ_UPLOAD_COMPLETE"
        const val UPLOAD_COMPLETED = "$BROADCAST_BASE.UPLOAD_COMPLETED"
        const val UPLOAD_ERROR = "$BROADCAST_BASE.UPLOAD_ERROR"

        // Upload Actions
        const val ACTION_ASSIGNMENT_SUBMISSION = "ACTION_ASSIGNMENT_SUBMISSION"
        const val ACTION_MESSAGE_ATTACHMENTS = "ACTION_MESSAGE_ATTACHMENTS"
        const val ACTION_COURSE_FILE = "ACTION_COURSE_FILE"
        const val ACTION_GROUP_FILE = "ACTION_GROUP_FILE"
        const val ACTION_USER_FILE = "ACTION_USER_FILE"
        const val ACTION_QUIZ_FILE = "ACTION_QUIZ_FILE"
        const val ACTION_DISCUSSION_ATTACHMENT = "ACTION_DISCUSSION_ATTACHMENT"
        const val ACTION_SUBMISSION_COMMENT = "ACTION_SUBMISSION_COMMENT"

        const val MESSAGE_ATTACHMENT_PATH = "conversation attachments"
        const val DISCUSSION_ATTACHMENT_PATH = "discussion attachments"
        const val ACTION_CANCEL_UPLOAD = "ACTION_CANCEL_UPLOAD"

        fun shutDown(context: Context) {
            // We won't want to cancel the notification if it's shared with another service, so only cancel our NOTIFICATION_ID
            try {
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(
                    NOTIFICATION_ID
                )
            } catch (e: Exception) {}
            FileUploadUtils.deleteTempDirectory(context)
        }

        fun createNotificationChannel(notificationManager: NotificationManager, channelId: String = CHANNEL_ID) {
            // Prevents recreation of notification channel if it exists.
            if (notificationManager.notificationChannels.any { it.id == channelId }) return

            val name = ContextKeeper.appContext.getString(R.string.notificationChannelNameFileUploadsName)
            val description = ContextKeeper.appContext.getString(R.string.notificationChannelNameFileUploadsDescription)

            // Create the channel and add the group
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description
            channel.enableLights(false)
            channel.enableVibration(false)
            channel.setSound(null, null)

            // Create the channel
            notificationManager.createNotificationChannel(channel)
        }

        fun getUserFilesBundle(
                fileSubmitObjects: ArrayList<FileSubmitObject>,
                parentFolderId: Long?
        ) = Bundle().apply {
            putParcelableArrayList(Const.FILES, fileSubmitObjects)
            parentFolderId?.let { putLong(Const.PARENT_FOLDER_ID, it) }
        }

        fun getQuizFileBundle(
                fileSubmitObjects: ArrayList<FileSubmitObject>,
                parentFolderId: Long?,
                quizQuestionId: Long,
                position: Int,
                courseId: Long,
                quizId: Long
        ) = Bundle().apply {
            putParcelableArrayList(Const.FILES, fileSubmitObjects)
            putLong(Const.QUIZ_ANSWER_ID, quizQuestionId)
            putLong(Const.QUIZ, quizId)
            putLong(Const.COURSE_ID, courseId)
            putInt(Const.POSITION, position)
            parentFolderId?.let { putLong(Const.PARENT_FOLDER_ID, it) }
        }

        fun getCourseFilesBundle(
                fileSubmitObjects: ArrayList<FileSubmitObject>,
                courseId: Long,
                parentFolderId: Long?
        ) = Bundle().apply {
            putParcelableArrayList(Const.FILES, fileSubmitObjects)
            putLong(Const.COURSE_ID, courseId)
            parentFolderId?.let { putLong(Const.PARENT_FOLDER_ID, it) }
        }

        fun getAssignmentSubmissionBundle(
                fileSubmitObjects: ArrayList<FileSubmitObject>,
                courseId: Long,
                assignment: Assignment,
                dbSubmissionId: Long? = null,
                additionalAttachmentIds: ArrayList<Long>? = null
        ) = Bundle().apply {
            putParcelableArrayList(Const.FILES, fileSubmitObjects)
            putLong(Const.COURSE_ID, courseId)
            putParcelable(Const.ASSIGNMENT, assignment)
            dbSubmissionId?.let { putLong(Const.SUBMISSION_ID, dbSubmissionId) }
            additionalAttachmentIds?.let { putLongArray(Const.ATTACHMENTS, it.toLongArray()) }
        }

        fun getMessageBundle(
                fileSubmitObjects: ArrayList<FileSubmitObject>,
                messageText: String,
                conversationId: Long
        ) = Bundle().apply {
            putParcelableArrayList(Const.FILES, fileSubmitObjects)
            putLong(Const.CONVERSATION, conversationId)
            putString(Const.MESSAGE, messageText)
        }

        fun getNewMessageBundle(
                fileSubmitObjects: ArrayList<FileSubmitObject>,
                userIds: ArrayList<String>,
                subject: String,
                messageText: String,
                isGroup: Boolean,
                contextId: String
        ) = Bundle().apply {
            putParcelableArrayList(Const.FILES, fileSubmitObjects)
            putStringArrayList(Const.USER_IDS, userIds)
            putString(Const.SUBJECT, subject)
            putString(Const.MESSAGE, messageText)
            putBoolean(Const.IS_GROUP, isGroup)
            putString(Const.CONTEXT_ID, contextId)
        }

        fun getSubmissionCommentBundle(
                fileSubmitObjects: ArrayList<FileSubmitObject>,
                courseId: Long,
                assignment: Assignment
        ) = Bundle().apply {
            putParcelableArrayList(Const.FILES, fileSubmitObjects)
            putLong(Const.COURSE_ID, courseId)
            putParcelable(Const.ASSIGNMENT, assignment)
        }
    }
}

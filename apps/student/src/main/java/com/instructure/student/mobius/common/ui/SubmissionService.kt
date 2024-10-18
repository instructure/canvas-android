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
package com.instructure.student.mobius.common.ui

import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.managers.FileUploadConfig
import com.instructure.canvasapi2.managers.FileUploadManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.canvasapi2.models.notorious.NotoriousResult
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.FileUtils
import com.instructure.canvasapi2.utils.ProgressRequestUpdateListener
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.canvasapi2.utils.weave.apiAsync
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.pandautils.models.PushNotification
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FileUploadUtils
import com.instructure.pandautils.utils.NotoriousUploader
import com.instructure.student.R
import com.instructure.student.activity.NavigationActivity
import com.instructure.student.events.ShowConfettiEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsSharedEvent
import com.instructure.student.mobius.common.FlowSource
import com.instructure.student.mobius.common.trySend
import com.instructure.student.room.StudentDb
import com.instructure.student.room.entities.CreateFileSubmissionEntity
import com.instructure.student.room.entities.CreatePendingSubmissionCommentEntity
import com.instructure.student.room.entities.CreateSubmissionEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.greenrobot.eventbus.EventBus
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import javax.inject.Inject

@OptIn(DelicateCoroutinesApi::class)
@AndroidEntryPoint
class SubmissionService : IntentService(SubmissionService::class.java.simpleName) {

    @Inject
    lateinit var studentDb: StudentDb

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    init {
        setIntentRedelivery(true)
    }

    override fun onHandleIntent(intent: Intent?) = runBlocking {
        val action = intent!!.action!!

        lateinit var submission: CreateSubmissionEntity

        // Try to get the submission, won't have one for comment uploads
        if (intent.hasExtra(Const.SUBMISSION_ID)) {
            val dbSubmissionId = intent.getLongExtra(Const.SUBMISSION_ID, 0)
            submission = studentDb.submissionDao().findSubmissionById(dbSubmissionId)
                ?: return@runBlocking // Return early if deleted, means it was canceled
        }

        when (Action.valueOf(action)) {
            Action.TEXT_ENTRY -> uploadText(submission)
            Action.FILE_ENTRY -> uploadFileSubmission(submission)
            Action.MEDIA_ENTRY -> uploadMedia(submission)
            Action.URL_ENTRY -> uploadUrl(submission, false)
            Action.STUDIO_ENTRY -> uploadUrl(submission, true)
            Action.COMMENT_ENTRY -> uploadComment(intent)
            Action.STUDENT_ANNOTATION -> uploadStudentAnnotation(submission)
        }.exhaustive
    }

    private fun showConfetti() {
        GlobalScope.launch {
            UserManager.getSelfFeatures().await().onSuccess { features ->
                features.find { it.feature == "disable_celebrations" }?.let {
                    if (it.flag.state == "off" || it.flag.state == "allowed") {
                        EventBus.getDefault().post(ShowConfettiEvent)
                    }
                }
            }
        }
    }

    private fun uploadText(submission: CreateSubmissionEntity) {
        showProgressNotification(submission.assignmentName, submission.id)
        val textToSubmit = try {
            URLEncoder.encode(submission.submissionEntry, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            submission.submissionEntry!!
        }

        val result = apiAsync {
            SubmissionManager.postTextSubmission(
                submission.canvasContext,
                submission.assignmentId,
                textToSubmit,
                it
            )
        }

        GlobalScope.launch {
            val uploadResult = result.await()
            uploadResult.onSuccess {
                studentDb.submissionDao().deleteSubmissionById(submission.id)
                if (!it.late) showConfetti()
            }.onFailure {
                studentDb.submissionDao().setSubmissionError(true, submission.id)
                showErrorNotification(this@SubmissionService, submission)
            }
        }
    }

    private fun uploadUrl(submission: CreateSubmissionEntity, isLti: Boolean) {
        showProgressNotification(submission.assignmentName, submission.id)
        val result = apiAsync {
            SubmissionManager.postUrlSubmission(
                submission.canvasContext,
                submission.assignmentId,
                submission.submissionEntry!!,
                isLti,
                it
            )
        }

        GlobalScope.launch {
            val uploadResult = result.await()
            uploadResult.onSuccess {
                studentDb.submissionDao().deleteSubmissionById(submission.id)
                if (!it.late) showConfetti()
            }.onFailure {
                studentDb.submissionDao().setSubmissionError(true, submission.id)
                showErrorNotification(this@SubmissionService, submission)
            }
        }
    }

    private suspend fun uploadMedia(submission: CreateSubmissionEntity) {
        showProgressNotification(submission.assignmentName, submission.id)

        // Upload file
        val mediaFile =
            studentDb.fileSubmissionDao().findFileForSubmissionId(submission.id) ?: return
        // The file is deleted, so no need to upload anything

        runBlocking {
            NotoriousUploader.performUpload(
                mediaFile.fullPath!!,
                object : ProgressRequestUpdateListener {
                    override fun onProgressUpdated(progressPercent: Float, length: Long): Boolean {
                        runBlocking { studentDb.submissionDao().findSubmissionById(submission.id) }
                            ?: return false // Stop uploading and don't show error if submission was deleted,

                        updateFileProgress(submission.id, progressPercent, 0, 1, 0)
                        return true
                    }
                }).onSuccess { result ->
                updateFileProgress(submission.id, 1.0f, 0, 1, 0)
                studentDb.fileSubmissionDao()
                    .setFileAttachmentIdAndError(null, false, null, mediaFile.id)

                // Update the notification to show that we're doing the actual submission now
                showProgressNotification(
                    submission.assignmentName,
                    submission.id,
                    alertOnlyOnce = true
                )
                val uploadedSubmission = try {
                    awaitApi<Submission> { callback ->
                        SubmissionManager.postMediaSubmission(
                            submission.canvasContext,
                            submission.assignmentId,
                            Const.MEDIA_RECORDING,
                            result.id!!,
                            FileUtils.mediaTypeFromNotoriousCode(result.mediaType),
                            callback
                        )
                    }
                } catch (e: Throwable) {
                    detachForegroundNotification()
                    studentDb.submissionDao().setSubmissionError(true, submission.id)
                    showErrorNotification(this@SubmissionService, submission)
                    return@runBlocking
                }

                // Clear out the db for the successful submission
                studentDb.fileSubmissionDao().deleteFilesForSubmissionId(submission.id)
                studentDb.submissionDao().deleteSubmissionById(submission.id)

                detachForegroundNotification()
                showCompleteNotification(
                    this@SubmissionService,
                    submission,
                    uploadedSubmission.late
                )
            }.onFailure {
                runBlocking {
                    handleFileError(studentDb, submission, 0, listOf(mediaFile), it?.message)
                }
            }
        }
    }

    private suspend fun uploadFileSubmission(submission: CreateSubmissionEntity) {
        showProgressNotification(submission.assignmentName, submission.id)

        val (completed, pending) = studentDb.fileSubmissionDao()
            .findFilesForSubmissionId(submission.id).partition { it.attachmentId != null }
        val uploadedAttachmentIds = uploadFiles(submission, completed.size, pending)
            ?: return // Cancel submitting if any of the files failed to upload

        // Update the notification to show that we're doing the actual submission now
        showProgressNotification(submission.assignmentName, submission.id, alertOnlyOnce = true)

        val attachmentIds = completed.mapNotNull { it.attachmentId } + uploadedAttachmentIds
        SubmissionManager.postSubmissionAttachmentsSynchronous(
            submission.canvasContext.id,
            submission.assignmentId,
            attachmentIds
        )?.let {
            // Clear out the db for the successful submission
            deleteSubmissionsForAssignment(submission.assignmentId, studentDb)

            detachForegroundNotification()
            showCompleteNotification(this, submission, it.late)
        } ?: run {
            detachForegroundNotification()
            studentDb.submissionDao().setSubmissionError(true, submission.id)
            showErrorNotification(this, submission)
        }
    }

    private fun uploadFiles(
        submission: CreateSubmissionEntity,
        completedAttachmentCount: Int,
        attachments: List<CreateFileSubmissionEntity>
    ): List<Long>? {
        val attachmentIds = ArrayList<Long>(attachments.size)

        // This is a group assignment, we need to get the list of groups before starting uploads
        val groupId = if (submission.assignmentGroupCategoryId == 0L) null else {
            GroupManager.getGroupsSynchronous(true)
                .find { it.groupCategoryId == submission.assignmentGroupCategoryId }?.id
        }

        attachments.forEachIndexed { index, pendingAttachment ->
            updateFileProgress(submission.id, 0f, index, attachments.size, completedAttachmentCount)

            // Upload config setup
            val fso = FileSubmitObject(
                pendingAttachment.name!!,
                pendingAttachment.size!!,
                pendingAttachment.contentType!!,
                pendingAttachment.fullPath!!
            )
            val config = if (groupId == null) {
                FileUploadConfig.forSubmission(
                    fso,
                    submission.canvasContext.id,
                    submission.assignmentId
                )
            } else {
                FileUploadConfig.forGroup(fso, groupId)
            }

            // Perform upload
            FileUploadManager.uploadFile(config, object : ProgressRequestUpdateListener {
                override fun onProgressUpdated(progressPercent: Float, length: Long): Boolean {
                    runBlocking { studentDb.submissionDao().findSubmissionById(submission.id) }
                        ?: return false // Stop uploading and don't show error if submission was deleted,

                    updateFileProgress(
                        submission.id,
                        progressPercent,
                        index,
                        attachments.size,
                        completedAttachmentCount
                    )
                    return true
                }
            }).onSuccess { attachment ->
                Analytics.logEvent(AnalyticsEventConstants.SUBMIT_FILEUPLOAD_SUCCEEDED)
                updateFileProgress(
                    submission.id,
                    1.0f,
                    index,
                    attachments.size,
                    completedAttachmentCount
                )
                runBlocking {
                    studentDb.fileSubmissionDao()
                        .setFileAttachmentIdAndError(attachment.id, false, null, pendingAttachment.id)
                }

                attachmentIds.add(attachment.id)
            }.onFailure {
                Analytics.logEvent(AnalyticsEventConstants.SUBMIT_FILEUPLOAD_FAILED)
                runBlocking {
                    handleFileError(studentDb, submission, index, attachments, it?.message)
                }
                return null
            }
        }
        return attachmentIds
    }

    @ObsoleteCoroutinesApi
    private suspend fun uploadComment(intent: Intent) {
        runBlocking {
            val commentDao = studentDb.pendingSubmissionCommentDao()
            val fileDao = studentDb.submissionCommentFileDao()

            // Get existing pending comment from db, or return if it was deleted
            val comment =
                commentDao.findCommentById(intent.getLongExtra(Const.ID, 0)) ?: return@runBlocking

            // Get attachments, partition by completed vs pending
            val (completed, pending) = fileDao
                .findFilesForPendingComment(comment.id)
                .partition { it.attachmentId != null }

            // Use foreground if there are files and/or media to upload
            val foreground = comment.mediaPath != null || pending.isNotEmpty()

            // Set up notifications
            createNotificationChannel(notificationManager, COMMENT_CHANNEL_ID)
            val notification =
                NotificationCompat.Builder(this@SubmissionService, COMMENT_CHANNEL_ID)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_notification_canvas_logo)
                    .setContentTitle(
                        getString(
                            R.string.assignmentSubmissionCommentUpload,
                            comment.assignmentName
                        )
                    )
                    .setProgress(0, 0, true)

            if (foreground) startForegroundWithCheck(comment.assignmentId.toInt(), notification.build())

            suspend fun setError() {
                val pendingIntent = getSubmissionIntent(
                    this@SubmissionService,
                    comment.canvasContext,
                    comment.assignmentId,
                    true
                )

                // Update notification
                notification.setContentTitle(
                    getString(
                        R.string.assignmentSubmissionCommentError,
                        comment.assignmentName
                    )
                )
                notification.setContentText("")
                notification.setProgress(0, 0, false)
                notification.setOngoing(false)
                notification.setContentIntent(pendingIntent)
                notification.setAutoCancel(true) // Still need auto cancel for clicks, false to ongoing only makes it possible to swipe away

                // Set error in db
                commentDao.setCommentError(true, comment.id)

                // Stop service
                if (foreground) {
                    detachForegroundNotification()
                }

                // Notify after we stop the service so that it shows on api versions less than N (stopForeground(false) is a little odd in behavior)
                notificationManager.notify(comment.assignmentId.toInt(), notification.build())
            }

            // Clear existing error state, if any
            commentDao.setCommentError(false, comment.id)

            // Upload pending attachments
            pending.forEachIndexed { index, pendingAttachment ->
                // Update notification
                notification.setContentText(
                    getString(
                        R.string.assignmentSubmissionUploadingFile,
                        index + 1,
                        pending.size
                    )
                )
                updateCommentProgress(
                    notification = notification,
                    comment = comment,
                    progressPercent = 0f,
                    completedSize = completed.size,
                    pendingSize = pending.size,
                    index = index
                )

                // Upload config setup
                val fso = FileSubmitObject(
                    pendingAttachment.name,
                    pendingAttachment.size,
                    pendingAttachment.contentType,
                    pendingAttachment.fullPath
                )
                val config = FileUploadConfig.forSubmissionComment(
                    fso,
                    comment.canvasContext.id,
                    comment.assignmentId
                )

                // Perform upload
                FileUploadManager.uploadFile(config, object : ProgressRequestUpdateListener {
                    override fun onProgressUpdated(progressPercent: Float, length: Long): Boolean {
                        updateCommentProgress(
                            notification = notification,
                            comment = comment,
                            progressPercent = progressPercent,
                            completedSize = completed.size,
                            pendingSize = pending.size,
                            index = index
                        )
                        return true
                    }
                }).onSuccess { attachment ->
                    // Update db
                    fileDao.setFileAttachmentId(attachment.id, pendingAttachment.id)
                }.onFailure {
                    setError()
                    return@runBlocking
                }
            }

            // Upload media file if present
            var notoriousResult: NotoriousResult? = null
            if (comment.mediaPath != null) {
                // Update notification
                notification.setContentText(getString(R.string.assignmentSubmissionCommentUploadingMedia))
                notification.setProgress(0, 0, true)
                notificationManager.notify(comment.assignmentId.toInt(), notification.build())

                NotoriousUploader.performUpload(
                    comment.mediaPath,
                    object : ProgressRequestUpdateListener {
                        override fun onProgressUpdated(
                            progressPercent: Float,
                            length: Long
                        ): Boolean {
                            updateCommentProgress(notification, comment, progressPercent)
                            return true
                        }
                    }).onSuccess {
                    notoriousResult = it
                }.onFailure {
                    setError()
                    return@runBlocking
                }
            }
            // Update notification
            if (foreground) {
                notification.setContentText("")
                notification.setProgress(0, 0, true)
                notificationManager.notify(comment.assignmentId.toInt(), notification.build())
            }

            try {
                val submission: Submission = notoriousResult?.let { result ->
                    awaitApi<Submission> { callback ->
                        SubmissionManager.postMediaSubmissionComment(
                            canvasContext = comment.canvasContext,
                            assignmentId = comment.assignmentId,
                            studentId = getUserId(),
                            mediaId = result.id!!,
                            mediaType = FileUtils.mediaTypeFromNotoriousCode(result.mediaType),
                            isGroupComment = comment.isGroupMessage,
                            callback = callback,
                            attemptId = comment.attemptId
                        )
                    }
                } ?: awaitApi { callback ->
                    val attachmentIds = runBlocking {
                        fileDao
                            .findFilesForPendingComment(comment.id)
                            .map { file -> file.attachmentId!! }
                    }
                    SubmissionManager.postSubmissionComment(
                        courseId = comment.canvasContext.id,
                        assignmentId = comment.assignmentId,
                        userId = getUserId(),
                        commentText = comment.message.orEmpty(),
                        isGroupMessage = comment.isGroupMessage,
                        attachments = attachmentIds,
                        callback = callback,
                        attemptId = comment.attemptId
                    )
                }

                val newComment = submission.submissionComments.last()
                FlowSource.getFlow<SubmissionComment>().trySend(newComment)

                FlowSource.getFlow<SubmissionDetailsSharedEvent>().trySend(
                    SubmissionDetailsSharedEvent.SubmissionCommentsUpdated(submission.submissionComments)
                )

                // Remove db entry
                commentDao.deleteCommentById(comment.id)

                // Clear network cache so we don't fetch a stale comment list
                CanvasRestAdapter.clearCacheUrls("assignments/${comment.assignmentId}")

                // Dismiss notification
                stopForeground(true)
            } catch (e: Throwable) {
                setError()
            }

        }

    }

    private fun uploadStudentAnnotation(submission: CreateSubmissionEntity) {
        showProgressNotification(submission.assignmentName, submission.id)
        val annotatableAttachmentIt = submission.annotatableAttachmentId

        if (annotatableAttachmentIt == null) {
            showErrorNotification(this, submission)
            return
        }

        GlobalScope.launch {
            val uploadResult = SubmissionManager.postStudentAnnotationSubmissionAsync(
                submission.canvasContext,
                submission.assignmentId,
                annotatableAttachmentIt
            ).await()
            uploadResult.onSuccess {
                studentDb.submissionDao().deleteSubmissionById(submission.id)
                if (!it.late) showConfetti()
            }.onFailure {
                studentDb.submissionDao().setSubmissionError(true, submission.id)
                showErrorNotification(this@SubmissionService, submission)
            }
        }
    }

    // region Notifications

    private suspend fun handleFileError(
        db: StudentDb,
        submission: CreateSubmissionEntity,
        completedCount: Int,
        attachments: List<CreateFileSubmissionEntity>,
        errorMessage: String?
    ) {
        if (db.submissionDao().findSubmissionById(submission.id) == null) {
            return // Not an error if the submission was deleted, it was canceled
        }

        db.submissionDao().setSubmissionError(true, submission.id)

        // Set all files that haven't been completed yet to error
        attachments.forEachIndexed { index, file ->
            if (index >= completedCount) {
                db.fileSubmissionDao().setFileError(true, errorMessage, file.id)
            }
        }

        detachForegroundNotification()
        showErrorNotification(this, submission)
    }

    private fun showProgressNotification(
        assignmentName: String?,
        submissionId: Long,
        inForeground: Boolean = true,
        alertOnlyOnce: Boolean = false
    ) {
        createNotificationChannel(notificationManager)
        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_canvas_logo)
            .setContentTitle(getString(R.string.assignmentSubmissionUpload, assignmentName))
            .setProgress(0, 0, true)
            .setOnlyAlertOnce(alertOnlyOnce)
        if (inForeground) {
            startForegroundWithCheck(submissionId.toInt(), notificationBuilder.build())
        } else {
            notificationManager.notify(submissionId.toInt(), notificationBuilder.build())
        }
    }

    private fun startForegroundWithCheck(id: Int, notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(id, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(id, notification)
        }
    }

    private fun updateFileProgress(
        dbSubmissionId: Long,
        uploaded: Float,
        fileIndex: Int,
        fileCount: Int,
        completedAttachmentCount: Int
    ) {
        // Update notification
        notificationBuilder
            .setContentTitle(
                getString(
                    R.string.assignmentSubmissionUploadingFile,
                    fileIndex + 1,
                    fileCount
                )
            )
            .setProgress(100, (uploaded * 100f).toInt(), false)
            .setOnlyAlertOnce(true)
        notificationManager.notify(dbSubmissionId.toInt(), notificationBuilder.build())

        // Set initial progress
        runBlocking {
            studentDb.submissionDao().updateProgress(
                currentFile = (completedAttachmentCount + fileIndex).toLong(),
                fileCount = (completedAttachmentCount + fileCount).toLong(),
                progress = uploaded.toDouble(),
                id = dbSubmissionId
            )
        }
    }

    private fun updateCommentProgress(
        notification: NotificationCompat.Builder,
        comment: CreatePendingSubmissionCommentEntity,
        progressPercent: Float,
        completedSize: Int = 0,
        pendingSize: Int = 1,
        index: Int = 0
    ) {
        // Update notification
        notification.setProgress(1000, (progressPercent * 1000).toInt(), false)
        notificationManager.notify(comment.assignmentId.toInt(), notification.build())

        // Update progress in db
        runBlocking {
            studentDb.pendingSubmissionCommentDao().updateCommentProgress(
                id = comment.id,
                currentFile = (completedSize + index).toLong(),
                fileCount = (completedSize + pendingSize).toLong(),
                progress = progressPercent.toDouble()
            )
        }
    }

    private fun detachForegroundNotification() {
        stopForeground(Service.STOP_FOREGROUND_DETACH)
    }

    private fun createNotificationChannel(
        notificationManager: NotificationManager,
        channelId: String = CHANNEL_ID
    ) {
        // Prevents recreation of notification channel if it exists.
        if (notificationManager.notificationChannels.any { it.id == channelId }) return

        val name =
            ContextKeeper.appContext.getString(R.string.notificationChannelNameFileUploadsName)
        val description =
            ContextKeeper.appContext.getString(R.string.notificationChannelNameFileUploadsDescription)

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

    private fun getSubmissionIntent(
        context: Context,
        canvasContext: CanvasContext,
        assignmentId: Long,
        goToSubmissionDetail: Boolean = false
    ): PendingIntent {
        val submissionPage = if (goToSubmissionDetail) "/submissions" else ""
        val path =
            "${ApiPrefs.fullDomain}/${canvasContext.apiContext()}/${canvasContext.id}/assignments/$assignmentId$submissionPage"
        val intent = Intent(context, NavigationActivity.startActivityClass).apply {
            putExtra(Const.LOCAL_NOTIFICATION, true)
            putExtra(PushNotification.HTML_URL, path)
        }

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun showErrorNotification(
        context: Context,
        submission: CreateSubmissionEntity
    ) {
        val pendingIntent =
            getSubmissionIntent(context, submission.canvasContext, submission.assignmentId)
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_canvas_logo)
            .setContentTitle(
                context.getString(
                    R.string.assignmentSubmissionError,
                    submission.assignmentName ?: ""
                )
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
        NotificationManagerCompat.from(context)
            .notify(submission.id.toInt(), notificationBuilder.build())
    }

    private fun showCompleteNotification(
        context: Context,
        submission: CreateSubmissionEntity,
        isLate: Boolean
    ) {
        val pendingIntent =
            getSubmissionIntent(context, submission.canvasContext, submission.assignmentId)
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_canvas_logo)
            .setContentTitle(
                context.getString(
                    R.string.assignmentSubmissionComplete,
                    submission.assignmentName ?: ""
                )
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
        NotificationManagerCompat.from(context)
            .notify(submission.id.toInt(), notificationBuilder.build())
        if (!isLate) showConfetti()
    }

    // endregion

    enum class Action {
        TEXT_ENTRY, URL_ENTRY, MEDIA_ENTRY, FILE_ENTRY, STUDIO_ENTRY, COMMENT_ENTRY, STUDENT_ANNOTATION
    }

    companion object {
        const val FILE_SUBMISSION_FINISHED = "file_submission_finished"

        private const val CHANNEL_ID = "submissionChannel"
        const val COMMENT_CHANNEL_ID = "commentUploadChannel"

        private fun getUserId() = ApiPrefs.user!!.id

        private suspend fun deleteSubmissionsForAssignment(
            id: Long,
            db: StudentDb,
            files: List<FileSubmitObject> = emptyList()
        ) {
            db.submissionDao().findSubmissionsByAssignmentId(id, getUserId())
                .forEach { submission ->
                    db.fileSubmissionDao().findFilesForSubmissionId(submission.id).forEach { file ->
                        // Delete the file for the old submission unless a new file or another database file depends on it (duplicate file being uploaded)
                        if (!files.any { it.fullPath == file.fullPath } && db.fileSubmissionDao()
                                .findFilesForPath(file.id, file.fullPath).isEmpty()) {
                            FileUploadUtils.deleteTempFile(file.fullPath)
                        }
                    }
                    db.fileSubmissionDao().deleteFilesForSubmissionId(submission.id)
                }
            db.submissionDao().deleteSubmissionsForAssignmentId(id, getUserId())
        }
    }
}

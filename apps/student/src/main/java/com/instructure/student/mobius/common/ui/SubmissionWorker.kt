/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.hasKeyWithValueOfType
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.FileUploadConfig
import com.instructure.canvasapi2.managers.FileUploadManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.canvasapi2.models.notorious.NotoriousResult
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.FileUtils
import com.instructure.canvasapi2.utils.ProgressRequestUpdateListener
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
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

@OptIn(DelicateCoroutinesApi::class)
@HiltWorker
class SubmissionWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val studentDb: StudentDb,
    private val notificationManager: NotificationManager,
    private val submissionApi: SubmissionAPI.SubmissionInterface
) : CoroutineWorker(context, workerParameters) {

    private lateinit var notificationBuilder: NotificationCompat.Builder

    override suspend fun doWork(): Result {
        try {
            val action = inputData.getString(Const.ACTION) ?: ""

            lateinit var submission: CreateSubmissionEntity

            if (inputData.hasKeyWithValueOfType<Long>(Const.SUBMISSION_ID)) {
                val dbSubmissionId = inputData.getLong(Const.SUBMISSION_ID, 0)
                submission = studentDb.submissionDao().findSubmissionById(dbSubmissionId)
                    ?: return Result.failure()// Return early if deleted, means it was canceled
            }

            return when (Action.valueOf(action)) {
                Action.TEXT_ENTRY -> uploadText(submission)
                Action.FILE_ENTRY -> uploadFileSubmission(submission)
                Action.MEDIA_ENTRY -> uploadMedia(submission)
                Action.URL_ENTRY -> uploadUrl(submission, false)
                Action.STUDIO_ENTRY -> uploadUrl(submission, true)
                Action.COMMENT_ENTRY -> uploadComment()
                Action.STUDENT_ANNOTATION -> uploadStudentAnnotation(submission)
            }
        } catch (e: IllegalArgumentException) {
            Log.e("SubmissionWorker", "Invalid Action")
            return Result.failure()
        }
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

    private suspend fun uploadText(submission: CreateSubmissionEntity): Result {
        showProgressNotification(submission.assignmentName, submission.id)
        val textToSubmit = try {
            withContext(Dispatchers.IO) {
                URLEncoder.encode(submission.submissionEntry, "UTF-8")
            }
        } catch (e: UnsupportedEncodingException) {
            submission.submissionEntry!!
        }
        val params = RestParams(
            canvasContext = submission.canvasContext,
            domain = ApiPrefs.overrideDomains[submission.canvasContext.id]
        )
        val result = submissionApi.postTextSubmission(
            submission.canvasContext.id,
            submission.assignmentId,
            SubmissionAPI.ONLINE_TEXT_ENTRY,
            textToSubmit,
            params
        )

        return when (result) {
            is DataResult.Success -> {
                studentDb.submissionDao().deleteSubmissionById(submission.id)
                if (!result.dataOrThrow.late) showConfetti()
                Result.success()
            }

            is DataResult.Fail -> {
                studentDb.submissionDao().setSubmissionError(true, submission.id)
                showErrorNotification(context, submission)
                Result.failure()
            }
        }
    }

    private suspend fun uploadUrl(submission: CreateSubmissionEntity, isLti: Boolean): Result {
        showProgressNotification(submission.assignmentName, submission.id)
        val params = RestParams(
            canvasContext = submission.canvasContext,
            domain = ApiPrefs.overrideDomains[submission.canvasContext.id]
        )
        val type = if (isLti) SubmissionAPI.BASIC_LTI_LAUNCH else SubmissionAPI.ONLINE_URL
        val result = submissionApi.postUrlSubmission(
            submission.canvasContext.id,
            submission.assignmentId,
            type,
            submission.submissionEntry!!,
            params
        )

        return when (result) {
            is DataResult.Success -> {
                studentDb.submissionDao().deleteSubmissionById(submission.id)
                if (!result.dataOrThrow.late) showConfetti()
                Result.success()
            }

            is DataResult.Fail -> {
                studentDb.submissionDao().setSubmissionError(true, submission.id)
                showErrorNotification(context, submission)
                Result.failure()
            }
        }
    }

    private suspend fun uploadMedia(submission: CreateSubmissionEntity): Result {
        showProgressNotification(submission.assignmentName, submission.id)

        // Upload file
        val mediaFile =
            studentDb.fileSubmissionDao().findFileForSubmissionId(submission.id)
                ?: return Result.failure() // The file is deleted, so no need to upload anything

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

            val params = RestParams(
                canvasContext = submission.canvasContext,
                domain = ApiPrefs.overrideDomains[submission.canvasContext.id]
            )
            val mediaSubmissionResult = submissionApi.postMediaSubmission(
                submission.canvasContext.id,
                submission.assignmentId,
                Const.MEDIA_RECORDING,
                result.id!!,
                FileUtils.mediaTypeFromNotoriousCode(result.mediaType),
                params
            )

            return when (mediaSubmissionResult) {
                is DataResult.Success -> {
                    // Clear out the db for the successful submission
                    studentDb.fileSubmissionDao().deleteFilesForSubmissionId(submission.id)
                    studentDb.submissionDao().deleteSubmissionById(submission.id)

                    showCompleteNotification(
                        context,
                        submission,
                        mediaSubmissionResult.dataOrThrow.late
                    )
                    Result.success()
                }

                is DataResult.Fail -> {
                    studentDb.submissionDao().setSubmissionError(true, submission.id)
                    showErrorNotification(context, submission)
                    Result.failure()
                }
            }
        }.onFailure {
            handleFileError(studentDb, submission, 0, listOf(mediaFile), it?.message)
            return Result.failure()
        }
        return Result.failure()
    }

    private suspend fun uploadFileSubmission(submission: CreateSubmissionEntity): Result {
        showProgressNotification(submission.assignmentName, submission.id)

        val (completed, pending) = studentDb.fileSubmissionDao()
            .findFilesForSubmissionId(submission.id).partition { it.attachmentId != null }
        val uploadedAttachmentIds = uploadFiles(submission, completed.size, pending)
            ?: return Result.failure() // Cancel submitting if any of the files failed to upload

        // Update the notification to show that we're doing the actual submission now
        showProgressNotification(submission.assignmentName, submission.id, alertOnlyOnce = true)

        val attachmentIds = completed.mapNotNull { it.attachmentId } + uploadedAttachmentIds
        val params = RestParams(
            canvasContext = submission.canvasContext,
            domain = ApiPrefs.overrideDomains[submission.canvasContext.id]
        )
        val result = submissionApi.postSubmissionAttachments(
            submission.canvasContext.id,
            submission.assignmentId,
            SubmissionAPI.ONLINE_UPLOAD,
            attachmentIds,
            params
        )

        return when (result) {
            is DataResult.Success -> {
                deleteSubmissionsForAssignment(submission.assignmentId, studentDb)
                showCompleteNotification(context, submission, result.dataOrThrow.late)
                Result.success()
            }

            is DataResult.Fail -> {
                studentDb.submissionDao().setSubmissionError(true, submission.id)
                showErrorNotification(context, submission)
                Result.failure()
            }
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
                        .setFileAttachmentIdAndError(
                            attachment.id,
                            false,
                            null,
                            pendingAttachment.id
                        )
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

    private suspend fun uploadComment(): Result {
        val commentDao = studentDb.pendingSubmissionCommentDao()
        val fileDao = studentDb.submissionCommentFileDao()

        // Get existing pending comment from db, or return if it was deleted
        val comment =
            commentDao.findCommentById(inputData.getLong(Const.ID, 0)) ?: return Result.failure()

        // Get attachments, partition by completed vs pending
        val (completed, pending) = fileDao
            .findFilesForPendingComment(comment.id)
            .partition { it.attachmentId != null }

        // Use foreground if there are files and/or media to upload
        val foreground = comment.mediaPath != null || pending.isNotEmpty()

        // Set up notifications
        createNotificationChannel(notificationManager, COMMENT_CHANNEL_ID)
        val notification =
            NotificationCompat.Builder(context, COMMENT_CHANNEL_ID)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_notification_canvas_logo)
                .setContentTitle(
                    context.getString(
                        R.string.assignmentSubmissionCommentUpload,
                        comment.assignmentName
                    )
                )
                .setProgress(0, 0, true)

        if (foreground) startForegroundWithCheck(comment.assignmentId.toInt(), notification.build())

        suspend fun setError() {
            val pendingIntent = getSubmissionIntent(
                context,
                comment.canvasContext,
                comment.assignmentId,
                true
            )

            // Update notification
            notification.setContentTitle(
                context.getString(
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

            // Notify after we stop the service so that it shows on api versions less than N (stopForeground(false) is a little odd in behavior)
            notificationManager.notify(comment.assignmentId.toInt(), notification.build())
        }

        // Clear existing error state, if any
        commentDao.setCommentError(false, comment.id)

        // Upload pending attachments
        pending.forEachIndexed { index, pendingAttachment ->
            // Update notification
            notification.setContentText(
                context.getString(
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
                return Result.failure()
            }
        }

        // Upload media file if present
        var notoriousResult: NotoriousResult? = null
        if (comment.mediaPath != null) {
            // Update notification
            notification.setContentText(context.getString(R.string.assignmentSubmissionCommentUploadingMedia))
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
                return Result.failure()
            }
        }
        // Update notification
        if (foreground) {
            notification.setContentText("")
            notification.setProgress(0, 0, true)
            notificationManager.notify(comment.assignmentId.toInt(), notification.build())
        }

        try {
            val params = RestParams(
                canvasContext = comment.canvasContext,
                domain = ApiPrefs.overrideDomains[comment.canvasContext.id]
            )
            val postCommentResult = notoriousResult?.let { result ->
                submissionApi.postMediaSubmissionComment(
                    courseId = comment.canvasContext.id,
                    assignmentId = comment.assignmentId,
                    userId = getUserId(),
                    mediaId = result.id!!,
                    commentType = FileUtils.mediaTypeFromNotoriousCode(result.mediaType),
                    isGroupComment = comment.isGroupMessage,
                    attemptId = comment.attemptId,
                    restParams = params
                )
            } ?: run {
                val attachmentIds = runBlocking {
                    fileDao
                        .findFilesForPendingComment(comment.id)
                        .map { file -> file.attachmentId!! }
                }
                submissionApi.postSubmissionComment(
                    courseId = comment.canvasContext.id,
                    assignmentId = comment.assignmentId,
                    userId = getUserId(),
                    comment = comment.message.orEmpty(),
                    isGroupComment = comment.isGroupMessage,
                    attachments = attachmentIds,
                    attemptId = comment.attemptId,
                    restParams = params
                )
            }

            return when (postCommentResult) {
                is DataResult.Success -> {
                    val submission = postCommentResult.dataOrThrow
                    val newComment = submission.submissionComments.last()
                    FlowSource.getFlow<SubmissionComment>().trySend(newComment)

                    FlowSource.getFlow<SubmissionDetailsSharedEvent>().trySend(
                        SubmissionDetailsSharedEvent.SubmissionCommentsUpdated(submission.submissionComments)
                    )

                    // Remove db entry
                    commentDao.deleteCommentById(comment.id)

                    // Clear network cache so we don't fetch a stale comment list
                    CanvasRestAdapter.clearCacheUrls("assignments/${comment.assignmentId}")

                    Result.success()
                }

                is DataResult.Fail -> {
                    setError()
                    Result.failure()
                }
            }
        } catch (e: Throwable) {
            setError()
            return Result.failure()
        }
    }

    private suspend fun uploadStudentAnnotation(submission: CreateSubmissionEntity): Result {
        showProgressNotification(submission.assignmentName, submission.id)
        val annotatableAttachmentIt = submission.annotatableAttachmentId

        if (annotatableAttachmentIt == null) {
            showErrorNotification(context, submission)
            return Result.failure()
        }

        val params = RestParams(
            canvasContext = submission.canvasContext,
            domain = ApiPrefs.overrideDomains[submission.canvasContext.id]
        )
        val result = submissionApi.postStudentAnnotationSubmission(
            submission.canvasContext.id,
            submission.assignmentId,
            Assignment.SubmissionType.STUDENT_ANNOTATION.apiString,
            annotatableAttachmentIt,
            params
        )

        return when (result) {
            is DataResult.Success -> {
                studentDb.submissionDao().deleteSubmissionById(submission.id)
                if (!result.dataOrThrow.late) showConfetti()
                Result.success()
            }

            is DataResult.Fail -> {
                studentDb.submissionDao().setSubmissionError(true, submission.id)
                showErrorNotification(context, submission)
                Result.failure()
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

        showErrorNotification(context, submission)
    }

    private suspend fun showProgressNotification(
        assignmentName: String?,
        submissionId: Long,
        inForeground: Boolean = true,
        alertOnlyOnce: Boolean = false
    ) {
        createNotificationChannel(notificationManager)
        notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_canvas_logo)
            .setContentTitle(context.getString(R.string.assignmentSubmissionUpload, assignmentName))
            .setProgress(0, 0, true)
            .setOnlyAlertOnce(alertOnlyOnce)
        if (inForeground) {
            startForegroundWithCheck(submissionId.toInt(), notificationBuilder.build())
        } else {
            notificationManager.notify(submissionId.toInt(), notificationBuilder.build())
        }
    }

    private suspend fun startForegroundWithCheck(id: Int, notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setForeground(
                ForegroundInfo(
                    id,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            )
        } else {
            setForeground(ForegroundInfo(id, notification))
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
                context.getString(
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

    private fun createNotificationChannel(
        notificationManager: NotificationManager,
        channelId: String = CHANNEL_ID
    ) {
        // Prevents recreation of notification channel if it exists.
        if (notificationManager.notificationChannels.any { it.id == channelId }) return

        val name =
            context.getString(R.string.notificationChannelNameFileUploadsName)
        val description =
            context.getString(R.string.notificationChannelNameFileUploadsDescription)

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
        notificationManager.notify(submission.id.toInt(), notificationBuilder.build())
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
        notificationManager.notify(submission.id.toInt(), notificationBuilder.build())
        if (!isLate) showConfetti()
    }

    // endregion

    enum class Action {
        TEXT_ENTRY, URL_ENTRY, MEDIA_ENTRY, FILE_ENTRY, STUDIO_ENTRY, COMMENT_ENTRY, STUDENT_ANNOTATION
    }

    companion object {
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

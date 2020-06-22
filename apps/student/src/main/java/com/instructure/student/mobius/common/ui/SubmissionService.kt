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

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.managers.*
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.canvasapi2.models.notorious.NotoriousResult
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.weave.apiAsync
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.pandautils.models.PushNotification
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FileUploadUtils
import com.instructure.pandautils.utils.NotoriousUploader
import com.instructure.student.FileSubmission
import com.instructure.student.PendingSubmissionComment
import com.instructure.student.R
import com.instructure.student.activity.NavigationActivity
import com.instructure.student.db.Db
import com.instructure.student.db.StudentDb
import com.instructure.student.db.getInstance
import com.instructure.student.db.sqlColAdapters.Date
import com.instructure.student.events.ShowConfettiEvent
import com.instructure.student.mobius.common.ChannelSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.greenrobot.eventbus.EventBus
import org.threeten.bp.OffsetDateTime
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class SubmissionService : IntentService(SubmissionService::class.java.simpleName) {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    init {
        setIntentRedelivery(true)
    }

    override fun onHandleIntent(intent: Intent) {
        val action = intent.action!!

        lateinit var submission: com.instructure.student.Submission
        val db = Db.getInstance(this)

        // Try to get the submission, won't have one for comment uploads
        if (intent.hasExtra(Const.SUBMISSION_ID)) {
            val dbSubmissionId = intent.getLongExtra(Const.SUBMISSION_ID, 0)
            submission = db.submissionQueries.getSubmissionById(dbSubmissionId).executeAsOneOrNull() ?: return // Return early if deleted, means it was canceled
        }

        when (Action.valueOf(action)) {
            Action.TEXT_ENTRY -> uploadText(db, submission)
            Action.FILE_ENTRY -> uploadFileSubmission(db, submission)
            Action.MEDIA_ENTRY -> uploadMedia(db, submission)
            Action.URL_ENTRY -> uploadUrl(db, submission, false)
            Action.STUDIO_ENTRY -> uploadUrl(db, submission, true)
            Action.COMMENT_ENTRY -> uploadComment(intent)
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

    private fun uploadText(db: StudentDb, submission: com.instructure.student.Submission) {
        showProgressNotification(submission.assignmentName, submission.id)
        val textToSubmit = try {
            URLEncoder.encode(submission.submissionEntry, "UTF-8")
        } catch (e: UnsupportedEncodingException) { submission.submissionEntry!! }

        val result = apiAsync<Submission> { SubmissionManager.postTextSubmission(submission.canvasContext, submission.assignmentId, textToSubmit, it) }

        GlobalScope.launch {
            val uploadResult = result.await()
            uploadResult.onSuccess {
                db.submissionQueries.deleteSubmissionById(submission.id)
                if (!it.late) showConfetti()
            }.onFailure {
                db.submissionQueries.setSubmissionError(true, submission.id)
                showErrorNotification(this@SubmissionService, submission)
            }
        }
    }

    private fun uploadUrl(db: StudentDb, submission: com.instructure.student.Submission, isLti: Boolean) {
        showProgressNotification(submission.assignmentName, submission.id)
        val result = apiAsync<Submission> { SubmissionManager.postUrlSubmission(submission.canvasContext, submission.assignmentId, submission.submissionEntry!!, isLti, it) }

        GlobalScope.launch {
            val uploadResult = result.await()
            uploadResult.onSuccess {
                db.submissionQueries.deleteSubmissionById(submission.id)
                if (!it.late) showConfetti()
            }.onFailure {
                db.submissionQueries.setSubmissionError(true, submission.id)
                showErrorNotification(this@SubmissionService, submission)
            }
        }
    }

    private fun uploadMedia(db: StudentDb, submission: com.instructure.student.Submission) {
        showProgressNotification(submission.assignmentName, submission.id)

        // Upload file
        val mediaFile = db.fileSubmissionQueries.getFilesForSubmissionId(submission.id).executeAsOneOrNull()
            ?: return // The file is deleted, so no need to upload anything

        runBlocking {
            NotoriousUploader.performUpload(mediaFile.fullPath!!, object : ProgressRequestUpdateListener {
                override fun onProgressUpdated(progressPercent: Float, length: Long): Boolean {
                    if (db.submissionQueries.getSubmissionById(submission.id).executeAsOneOrNull() == null) {
                        return false // Stop uploading and don't show error if submission was deleted,
                    }

                    updateFileProgress(db, submission.id, progressPercent, 0, 1, 0)
                    return true
                }
            }).onSuccess { result ->
                updateFileProgress(db, submission.id, 1.0f, 0, 1, 0)
                db.fileSubmissionQueries.setFileAttachmentIdAndError(null, false, null, mediaFile.id)

                // Update the notification to show that we're doing the actual submission now
                showProgressNotification(submission.assignmentName, submission.id, alertOnlyOnce = true)
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
                    db.submissionQueries.setSubmissionError(true, submission.id)
                    showErrorNotification(this@SubmissionService, submission)
                    return@runBlocking
                }

                // Clear out the db for the successful submission
                db.fileSubmissionQueries.deleteFilesForSubmissionId(submission.id)
                db.submissionQueries.deleteSubmissionById(submission.id)

                detachForegroundNotification()
                showCompleteNotification(this@SubmissionService, submission, uploadedSubmission.late)
            }.onFailure {
                handleFileError(db, submission, 0, listOf(mediaFile), it?.message)
            }
        }
    }

    private fun uploadFileSubmission(db: StudentDb, submission: com.instructure.student.Submission) {
        showProgressNotification(submission.assignmentName, submission.id)

        val (completed, pending) = db.fileSubmissionQueries.getFilesForSubmissionId(submission.id).executeAsList().partition { it.attachmentId != null }
        val uploadedAttachmentIds = uploadFiles(submission, completed.size, pending, db)
            ?: return // Cancel submitting if any of the files failed to upload

        // Update the notification to show that we're doing the actual submission now
        showProgressNotification(submission.assignmentName, submission.id, alertOnlyOnce = true)

        val attachmentIds = completed.mapNotNull { it.attachmentId } + uploadedAttachmentIds
        SubmissionManager.postSubmissionAttachmentsSynchronous(submission.canvasContext.id, submission.assignmentId, attachmentIds)?.let {
            // Clear out the db for the successful submission
            deleteSubmissionsForAssignment(submission.assignmentId, db)

            detachForegroundNotification()
            showCompleteNotification(this, submission, it.late)
        } ?: run {
            detachForegroundNotification()
            db.submissionQueries.setSubmissionError(true, submission.id)
            showErrorNotification(this, submission)
        }
    }

    private fun uploadFiles(submission: com.instructure.student.Submission, completedAttachmentCount: Int, attachments: List<FileSubmission>, db: StudentDb): List<Long>? {
        val attachmentIds = ArrayList<Long>(attachments.size)

        // This is a group assignment, we need to get the list of groups before starting uploads
        val groupId = if (submission.assignmentGroupCategoryId == 0L) null else {
            GroupManager.getGroupsSynchronous(true)
                .find { it.groupCategoryId == submission.assignmentGroupCategoryId }?.id
        }

        attachments.forEachIndexed { index, pendingAttachment ->
            updateFileProgress(db, submission.id, 0f, index, attachments.size, completedAttachmentCount)

            // Upload config setup
            val fso = FileSubmitObject(pendingAttachment.name!!, pendingAttachment.size!!, pendingAttachment.contentType!!, pendingAttachment.fullPath!!)
            val config = if (groupId == null) {
                FileUploadConfig.forSubmission(fso, submission.canvasContext.id, submission.assignmentId)
            } else {
                FileUploadConfig.forGroup(fso, groupId)
            }

            // Perform upload
            FileUploadManager.uploadFile(config, object : ProgressRequestUpdateListener {
                override fun onProgressUpdated(progressPercent: Float, length: Long): Boolean {
                    if (db.submissionQueries.getSubmissionById(submission.id).executeAsOneOrNull() == null) {
                        return false // Stop uploading and don't show error if submission was deleted,
                    }
                    updateFileProgress(db, submission.id, progressPercent, index, attachments.size, completedAttachmentCount)
                    return true
                }
            }).onSuccess { attachment ->
                Analytics.logEvent(AnalyticsEventConstants.SUBMIT_FILEUPLOAD_SUCCEEDED)
                updateFileProgress(db, submission.id, 1.0f, index, attachments.size, completedAttachmentCount)
                db.fileSubmissionQueries.setFileAttachmentIdAndError(attachment.id, false, null, pendingAttachment.id)

                attachmentIds.add(attachment.id)
            }.onFailure {
                Analytics.logEvent(AnalyticsEventConstants.SUBMIT_FILEUPLOAD_FAILED)
                handleFileError(db, submission, index, attachments, it?.message)
                return null
            }
        }
        return attachmentIds
    }

    @UseExperimental(ExperimentalCoroutinesApi::class)
    private fun uploadComment(intent: Intent) {
        runBlocking {
            val db = Db.getInstance(this@SubmissionService)
            val commentDb = db.pendingSubmissionCommentQueries
            val fileDb = db.submissionCommentFileQueries

            // Get existing pending comment from db, or return if it was deleted
            val comment = commentDb.getCommentById(intent.getLongExtra(Const.ID, 0)).executeAsOneOrNull() ?: return@runBlocking

            // Get attachments, partition by completed vs pending
            val (completed, pending) = fileDb
                .getFilesForPendingComment(comment.id)
                .executeAsList()
                .partition { it.attachmentId != null }

            // Use foreground if there are files and/or media to upload
            val foreground = comment.mediaPath != null || pending.isNotEmpty()

            // Set up notifications
            createNotificationChannel(notificationManager, COMMENT_CHANNEL_ID)
            val notification = NotificationCompat.Builder(this@SubmissionService, COMMENT_CHANNEL_ID)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_notification_canvas_logo)
                .setContentTitle(getString(R.string.assignmentSubmissionCommentUpload, comment.assignmentName))
                .setProgress(0, 0, true)

            if (foreground) startForeground(comment.assignmentId.toInt(), notification.build())

            fun setError() {
                val pendingIntent = getSubmissionIntent(this@SubmissionService, comment.canvasContext, comment.assignmentId, true)

                // Update notification
                notification.setContentTitle(getString(R.string.assignmentSubmissionCommentError, comment.assignmentName))
                notification.setContentText("")
                notification.setProgress(0, 0, false)
                notification.setOngoing(false)
                notification.setContentIntent(pendingIntent)
                notification.setAutoCancel(true) // Still need auto cancel for clicks, false to ongoing only makes it possible to swipe away

                // Set error in db
                commentDb.setCommentError(true, comment.id)

                // Stop service
                if (foreground) {
                    detachForegroundNotification()
                }

                // Notify after we stop the service so that it shows on api versions less than N (stopForeground(false) is a little odd in behavior)
                notificationManager.notify(comment.assignmentId.toInt(), notification.build())
            }

            // Clear existing error state, if any
            commentDb.setCommentError(false, comment.id)

            // Upload pending attachments
            pending.forEachIndexed { index, pendingAttachment ->
                // Update notification
                notification.setContentText(getString(R.string.assignmentSubmissionUploadingFile, index + 1, pending.size))
                updateCommentProgress(
                    notification = notification,
                    comment = comment,
                    progressPercent = 0f,
                    db = db,
                    completedSize = completed.size,
                    pendingSize = pending.size,
                    index = index
                )

                // Upload config setup
                val fso = FileSubmitObject(pendingAttachment.name, pendingAttachment.size, pendingAttachment.contentType, pendingAttachment.fullPath)
                val config = FileUploadConfig.forSubmissionComment(fso, comment.canvasContext.id, comment.assignmentId)

                // Perform upload
                FileUploadManager.uploadFile(config, object : ProgressRequestUpdateListener {
                    override fun onProgressUpdated(progressPercent: Float, length: Long): Boolean {
                        updateCommentProgress(
                            notification = notification,
                            comment = comment,
                            progressPercent = progressPercent,
                            db = db,
                            completedSize = completed.size,
                            pendingSize = pending.size,
                            index = index
                        )
                        return true
                    }
                }).onSuccess { attachment ->
                    // Update db
                    fileDb.setFileAttachmentId(attachment.id, pendingAttachment.id)
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

                NotoriousUploader.performUpload(comment.mediaPath!!, object : ProgressRequestUpdateListener {
                    override fun onProgressUpdated(progressPercent: Float, length: Long): Boolean {
                        updateCommentProgress(notification, comment, progressPercent, db)
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
                            callback = callback
                        )
                    }
                } ?: awaitApi { callback ->
                    val attachmentIds = fileDb
                        .getFilesForPendingComment(comment.id)
                        .executeAsList()
                        .map { file -> file.attachmentId!! }
                    SubmissionManager.postSubmissionComment(
                        courseId = comment.canvasContext.id,
                        assignmentId = comment.assignmentId,
                        userId = getUserId(),
                        commentText = comment.message.orEmpty(),
                        isGroupMessage = comment.isGroupMessage,
                        attachments = attachmentIds,
                        callback = callback
                    )
                }

                val newComment = submission.submissionComments.last()
                ChannelSource.getChannel<SubmissionComment>().offer(newComment)

                // Remove db entry
                commentDb.deleteCommentById(comment.id)

                // Clear network cache so we don't fetch a stale comment list
                CanvasRestAdapter.clearCacheUrls("assignments/${comment.assignmentId}")

                // Dismiss notification
                stopForeground(true)
            } catch (e: Throwable) {
                setError()
            }

        }

    }

    // region Notifications

    private fun handleFileError(db: StudentDb, submission: com.instructure.student.Submission, completedCount: Int, attachments: List<FileSubmission>, errorMessage: String?) {
        if (db.submissionQueries.getSubmissionById(submission.id).executeAsOneOrNull() == null) {
            return // Not an error if the submission was deleted, it was canceled
        }

        db.submissionQueries.setSubmissionError(true, submission.id)

        // Set all files that haven't been completed yet to error
        attachments.forEachIndexed { index, file ->
            if (index >= completedCount) {
                db.fileSubmissionQueries.setFileError(true, errorMessage, file.id)
            }
        }

        detachForegroundNotification()
        showErrorNotification(this, submission)
    }

    private fun showProgressNotification(assignmentName: String?, submissionId: Long, inForeground: Boolean = true, alertOnlyOnce: Boolean = false) {
        createNotificationChannel(notificationManager)
        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_canvas_logo)
            .setContentTitle(getString(R.string.assignmentSubmissionUpload, assignmentName))
            .setProgress(0, 0, true)
            .setOnlyAlertOnce(alertOnlyOnce)
        if (inForeground) {
            startForeground(submissionId.toInt(), notificationBuilder.build())
        } else {
            notificationManager.notify(submissionId.toInt(), notificationBuilder.build())
        }
    }

    private fun updateFileProgress(db: StudentDb, dbSubmissionId: Long, uploaded: Float, fileIndex: Int, fileCount: Int, completedAttachmentCount: Int) {
        // Update notification
        notificationBuilder
            .setContentTitle(getString(R.string.assignmentSubmissionUploadingFile, fileIndex + 1, fileCount))
            .setProgress(100, (uploaded * 100f).toInt(), false)
            .setOnlyAlertOnce(true)
        notificationManager.notify(dbSubmissionId.toInt(), notificationBuilder.build())

        // Set initial progress
        db.submissionQueries.updateProgress(
            currentFile = (completedAttachmentCount + fileIndex).toLong(),
            fileCount = (completedAttachmentCount + fileCount).toLong(),
            progress = uploaded.toDouble(),
            id = dbSubmissionId
        )
    }

    private fun updateCommentProgress(notification: NotificationCompat.Builder, comment: PendingSubmissionComment, progressPercent: Float, db: StudentDb, completedSize: Int = 0, pendingSize: Int = 1, index: Int = 0) {
        // Update notification
        notification.setProgress(1000, (progressPercent * 1000).toInt(), false)
        notificationManager.notify(comment.assignmentId.toInt(), notification.build())

        // Update progress in db
        db.pendingSubmissionCommentQueries.updateCommentProgress(
            id = comment.id,
            currentFile = (completedSize + index).toLong(),
            fileCount = (completedSize + pendingSize).toLong(),
            progress = progressPercent.toDouble()
        )
    }

    private fun detachForegroundNotification() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            stopForeground(Service.STOP_FOREGROUND_DETACH)
        else
            stopForeground(false)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager, channelId: String = CHANNEL_ID) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

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

    private fun getSubmissionIntent(
        context: Context,
        canvasContext: CanvasContext,
        assignmentId: Long,
        goToSubmissionDetail: Boolean = false
    ): PendingIntent {
        val submissionPage = if (goToSubmissionDetail) "/submissions" else ""
        val path = "${ApiPrefs.fullDomain}/${canvasContext.apiContext()}/${canvasContext.id}/assignments/$assignmentId$submissionPage"
        val intent = Intent(context, NavigationActivity.startActivityClass).apply {
            putExtra(Const.LOCAL_NOTIFICATION, true)
            putExtra(PushNotification.HTML_URL, path)
        }

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun showErrorNotification(
        context: Context,
        submission: com.instructure.student.Submission
    ) {
        val pendingIntent = getSubmissionIntent(context, submission.canvasContext, submission.assignmentId)
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_canvas_logo)
            .setContentTitle(context.getString(R.string.assignmentSubmissionError, submission.assignmentName ?: ""))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
        NotificationManagerCompat.from(context)
            .notify(submission.id.toInt(), notificationBuilder.build())
    }

    private fun showCompleteNotification(
        context: Context,
        submission: com.instructure.student.Submission,
        isLate: Boolean
    ) {
        val pendingIntent = getSubmissionIntent(context, submission.canvasContext, submission.assignmentId)
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_canvas_logo)
            .setContentTitle(context.getString(R.string.assignmentSubmissionComplete, submission.assignmentName ?: ""))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
        NotificationManagerCompat.from(context).notify(submission.id.toInt(), notificationBuilder.build())
        if (!isLate) showConfetti()
    }

    // endregion

    enum class Action {
        TEXT_ENTRY, URL_ENTRY, MEDIA_ENTRY, FILE_ENTRY, STUDIO_ENTRY, COMMENT_ENTRY
    }

    companion object {
        const val FILE_SUBMISSION_FINISHED = "file_submission_finished"

        private const val CHANNEL_ID = "submissionChannel"
        const val COMMENT_CHANNEL_ID = "commentUploadChannel"

        private fun getUserId() = ApiPrefs.user!!.id

        private fun insertNewSubmission(assignmentId: Long, context: Context, files: List<FileSubmitObject> = emptyList(), insertBlock: (StudentDb) -> Unit): Long {
            val db = Db.getInstance(context)
            deleteSubmissionsForAssignment(assignmentId, db, files)
            insertBlock(db)
            val dbSubmissionId = db.submissionQueries.getLastInsert().executeAsOne()

            files.forEach {
                db.fileSubmissionQueries.insertFile(dbSubmissionId, it.name, it.size, it.contentType, it.fullPath)
            }

            return dbSubmissionId
        }

        private fun deleteSubmissionsForAssignment(id: Long, db: StudentDb, files: List<FileSubmitObject> = emptyList()) {
            db.submissionQueries.getSubmissionsByAssignmentId(id, getUserId()).executeAsList().forEach { submission ->
                db.fileSubmissionQueries.getFilesForSubmissionId(submission.id).executeAsList().forEach { file ->
                    // Delete the file for the old submission unless a new file or another database file depends on it (duplicate file being uploaded)
                    if (!files.any { it.fullPath == file.fullPath } && db.fileSubmissionQueries.getFilesForPath(file.id, file.fullPath).executeAsList().isEmpty()) {
                        FileUploadUtils.deleteTempFile(file.fullPath)
                    }
                }
                db.fileSubmissionQueries.deleteFilesForSubmissionId(submission.id)
            }
            db.submissionQueries.deleteSubmissionsForAssignmentId(id, getUserId())
        }

        // region start helpers

        private fun startService(context: Context, action: Action, extras: Bundle) {
            Intent(context, SubmissionService::class.java).also { intent ->
                intent.action = action.name
                intent.putExtras(extras)
                context.startService(intent)
            }
        }

        fun startTextSubmission(
            context: Context,
            canvasContext: CanvasContext,
            assignmentId: Long,
            assignmentName: String?,
            text: String
        ) {
            val dbSubmissionId = insertNewSubmission(assignmentId, context) {
                it.submissionQueries.insertOnlineTextSubmission(text, assignmentName, assignmentId, canvasContext, getUserId(), OffsetDateTime.now())
            }

            val bundle = Bundle().apply {
                putLong(Const.SUBMISSION_ID, dbSubmissionId)
            }

            startService(context, Action.TEXT_ENTRY, bundle)
        }

        fun startUrlSubmission(
            context: Context,
            canvasContext: CanvasContext,
            assignmentId: Long,
            assignmentName: String?,
            url: String
        ) {
            val dbSubmissionId = insertNewSubmission(assignmentId, context) {
                it.submissionQueries.insertOnlineUrlSubmission(url, assignmentName, assignmentId, canvasContext, getUserId(), OffsetDateTime.now())
            }

            val bundle = Bundle().apply {
                putLong(Const.SUBMISSION_ID, dbSubmissionId)
            }

            startService(context, Action.URL_ENTRY, bundle)
        }

        fun startFileSubmission(
            context: Context,
            canvasContext: CanvasContext,
            assignmentId: Long,
            assignmentName: String?,
            assignmentGroupCategoryId: Long = 0,
            files: ArrayList<FileSubmitObject>
        ) {
            files.ifEmpty { return } // No need to upload files if we aren't given any

            val dbSubmissionId = insertNewSubmission(assignmentId, context, files) {
                it.submissionQueries.insertOnlineUploadSubmission(assignmentName, assignmentId, assignmentGroupCategoryId, canvasContext, getUserId(), OffsetDateTime.now())
            }

            val bundle = Bundle().apply {
                putLong(Const.SUBMISSION_ID, dbSubmissionId)
            }

            startService(context, Action.FILE_ENTRY, bundle)
        }

        fun retryFileSubmission(context: Context, dbSubmissionId: Long) {
            val db = Db.getInstance(context)
            val submission = db.submissionQueries.getSubmissionById(dbSubmissionId).executeAsOneOrNull() ?: return // No submission exists, so nothing to be done

            db.submissionQueries.setSubmissionError(false, submission.id)
            val bundle = Bundle().apply {
                putLong(Const.SUBMISSION_ID, dbSubmissionId)
            }

            startService(context, if (submission.submissionType == Assignment.SubmissionType.MEDIA_RECORDING.apiString) Action.MEDIA_ENTRY else Action.FILE_ENTRY, bundle)
        }

        fun startMediaSubmission(
            context: Context,
            canvasContext: CanvasContext,
            assignmentId: Long,
            assignmentName: String?,
            assignmentGroupCategoryId: Long,
            mediaFilePath: String
        ) {
            val file = File(mediaFilePath).let { FileSubmitObject(it.name, it.length(), FileUtils.getMimeType(it.path), mediaFilePath) }
            val dbSubmissionId = insertNewSubmission(assignmentId, context, listOf(file)) {
                it.submissionQueries.insertMediaUploadSubmission(assignmentName, assignmentId, assignmentGroupCategoryId, canvasContext, getUserId(), OffsetDateTime.now())
            }

            val bundle = Bundle().apply {
                putLong(Const.SUBMISSION_ID, dbSubmissionId)
            }

            startService(context, Action.MEDIA_ENTRY, bundle)
        }

        fun startStudioSubmission(
            context: Context,
            canvasContext: CanvasContext,
            assignmentId: Long,
            assignmentName: String?,
            url: String
        ) {
            val dbSubmissionId = insertNewSubmission(assignmentId, context) {
                it.submissionQueries.insertOnlineUrlSubmission(url, assignmentName, assignmentId, canvasContext, getUserId(), OffsetDateTime.now())
            }

            val bundle = Bundle().apply {
                putLong(Const.SUBMISSION_ID, dbSubmissionId)
            }

            startService(context, Action.STUDIO_ENTRY, bundle)
        }

        /**
         * Begins the process of uploading a submission comment for the given assignment. A valid value *must* be
         * provided for [message] and/or [attachments]. If no message is provided, the API will use the message "This
         * is a media comment" - or its applicable translation. If one or more attachments are provided, a progress
         * notification will be displayed while the upload occurs.
         */
        fun startCommentUpload(
            context: Context,
            canvasContext: CanvasContext,
            assignmentId: Long,
            assignmentName: String,
            message: String?,
            attachments: List<FileSubmitObject>?,
            isGroupMessage: Boolean
        ) {
            require(message.isValid() || attachments?.isNotEmpty() == true)
            val db = Db.getInstance(context)
            db.pendingSubmissionCommentQueries.insertComment(
                accountDomain = ApiPrefs.domain,
                canvasContext = canvasContext,
                assignmentName = assignmentName,
                assignmentId = assignmentId,
                lastActivityDate = Date.now(),
                isGroupMessage = isGroupMessage,
                message = message,
                mediaPath = null
            )
            val commentId = db.pendingSubmissionCommentQueries.getLastInsert().executeAsOne()
            attachments?.forEach { db.submissionCommentFileQueries.insertFile(commentId, it.name, it.size, it.contentType, it.fullPath) }

            val bundle = Bundle().apply {
                putLong(Const.ID, commentId)
            }
            startService(context, Action.COMMENT_ENTRY, bundle)
        }

        /**
         * Begins the process of uploading a media submission comment for the given assignment. A progress notification
         * will be displayed while the upload occurs.
         */
        fun startMediaCommentUpload(
            context: Context,
            canvasContext: CanvasContext,
            assignmentId: Long,
            assignmentName: String,
            mediaFile: File,
            isGroupMessage: Boolean
        ) {
            val db = Db.getInstance(context)
            db.pendingSubmissionCommentQueries.insertComment(
                accountDomain = ApiPrefs.domain,
                canvasContext = canvasContext,
                assignmentName = assignmentName,
                assignmentId = assignmentId,
                lastActivityDate = Date.now(),
                isGroupMessage = isGroupMessage,
                message = null,
                mediaPath = mediaFile.absolutePath
            )
            val commentId = db.pendingSubmissionCommentQueries.getLastInsert().executeAsOne()

            val bundle = Bundle().apply {
                putLong(Const.ID, commentId)
            }
            startService(context, Action.COMMENT_ENTRY, bundle)
        }

        fun retryCommentUpload(context: Context, commentId: Long) {
            val bundle = Bundle().apply {
                putLong(Const.ID, commentId)
            }
            startService(context, Action.COMMENT_ENTRY, bundle)
        }

        fun deletePendingComment(context: Context, commentId: Long) {
            val db = Db.getInstance(context)
            db.pendingSubmissionCommentQueries.deleteCommentById(commentId)
            db.submissionCommentFileQueries.deleteFilesForCommentId(commentId)
        }
        // endregion
    }
}

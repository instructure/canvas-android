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
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Parcelable
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.canvasapi2.utils.weave.apiAsync
import com.instructure.pandautils.models.FileSubmitObject
import com.instructure.pandautils.models.PushNotification
import com.instructure.pandautils.services.FileUploadService
import com.instructure.pandautils.services.NotoriousUploadService
import com.instructure.pandautils.utils.Const
import com.instructure.student.R
import com.instructure.student.activity.NavigationActivity
import com.instructure.student.db.Db
import com.instructure.student.db.getInstance
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.threeten.bp.OffsetDateTime
import java.io.File
import java.util.*

class SubmissionFileUploadReceiver(private val dbSubmissionId: Long) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return

        val submissionId = intent?.extras?.getLong(Const.SUBMISSION_ID)
        val assignmentName = intent?.extras?.getString(Const.ASSIGNMENT_NAME)

        if (submissionId == null || submissionId != dbSubmissionId) return // Only handle our submission

        val db = Db.getInstance(context)
        val submission = db.submissionQueries.getSubmissionById(submissionId).executeAsOne()

        when (intent.action) {
            FileUploadService.UPLOAD_ERROR -> {
                SubmissionService.showErrorNotification(context, submission.canvasContext!!, submission.assignmentId!!, assignmentName, submissionId)

                val message = intent.getStringExtra(Const.MESSAGE)
                val attachments = intent.getParcelableArrayListExtra<Attachment>(Const.ATTACHMENTS)
                val files = db.fileSubmissionQueries.getFilesWithoutAttachmentsForSubmissionId(submissionId).executeAsList()

                // Update files, if we have an attachment it uploaded, otherwise it failed
                db.submissionQueries.setSubmissionError(true, submissionId)
                files.forEachIndexed { index, file ->
                    if (index < attachments.size) {
                        db.fileSubmissionQueries.setFileAttachmentIdAndError(attachments[index].id, false, null, file.id)
                    } else {
                        db.fileSubmissionQueries.setFileError(true, message, file.id)
                    }
                }
            }
            FileUploadService.ALL_UPLOADS_COMPLETED -> {
                SubmissionService.showCompleteNotification(context, submission.canvasContext!!, submission.assignmentId!!, assignmentName, submissionId)

                // Clear out the db for the successful submission
                db.fileSubmissionQueries.deleteFilesForSubmissionId(submissionId)
                db.submissionQueries.deleteSubmissionById(submissionId)
            }
            else -> return // Don't do anything on other actions
        }

        // We'll always want to unregister ourselves
        context.unregisterReceiver(this)
        context.sendBroadcast(Intent(SubmissionService.FILE_SUBMISSION_FINISHED).apply {
            putExtra(Const.SUBMISSION, submissionId)
        })
    }
}

class SubmissionService : IntentService(SubmissionService::class.java.simpleName) {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    init {
        setIntentRedelivery(true)
    }

    private fun getUserId() = ApiPrefs.user!!.id

    override fun onHandleIntent(intent: Intent) {
        val action = intent.action!!

        when (Action.valueOf(action)) {
            Action.TEXT_ENTRY -> uploadText(intent)
            Action.FILE_ENTRY -> uploadFile(intent)
            Action.MEDIA_ENTRY -> uploadMedia(intent)
            Action.URL_ENTRY -> uploadUrl(intent, false)
            Action.STUDIO_ENTRY -> uploadUrl(intent, true)
        }.exhaustive
    }

    private fun uploadText(intent: Intent) {
        val text = intent.getStringExtra(Const.MESSAGE)
        val assignmentId = intent.getLongExtra(Const.ASSIGNMENT_ID, 0)
        val assignmentName = intent.getStringExtra(Const.ASSIGNMENT_NAME)
        val context = intent.getParcelableExtra<CanvasContext>(Const.CANVAS_CONTEXT)
        val dbSubmissionId: Long
        val db = Db.getInstance(this).submissionQueries

        // Save to persistence
        db.deleteSubmissionsForAssignmentId(assignmentId, getUserId())
        db.insertOnlineTextSubmission(text, assignmentName, assignmentId, context, getUserId(), OffsetDateTime.now())
        dbSubmissionId = db.getLastInsert().executeAsOne()

        showProgressNotification(assignmentName, dbSubmissionId)
        val result = apiAsync<Submission> { SubmissionManager.postTextSubmission(context, assignmentId, text, it) }

        GlobalScope.launch {
            val uploadResult = result.await()
            uploadResult.onSuccess {
                db.deleteSubmissionById(dbSubmissionId)
            }.onFailure {
                db.setSubmissionError(true, dbSubmissionId)
                showErrorNotification(this@SubmissionService, context, assignmentId, assignmentName, dbSubmissionId)
            }
        }
    }

    private fun uploadUrl(intent: Intent, isLti: Boolean) {
        val url = intent.getStringExtra(Const.URL)
        val assignmentId = intent.getLongExtra(Const.ASSIGNMENT_ID, 0)
        val assignmentName = intent.getStringExtra(Const.ASSIGNMENT_NAME)
        val context = intent.getParcelableExtra<CanvasContext>(Const.CANVAS_CONTEXT)
        val dbSubmissionId: Long
        val db = Db.getInstance(this).submissionQueries

        // Save to persistence
        db.deleteSubmissionsForAssignmentId(assignmentId, getUserId())
        db.insertOnlineUrlSubmission(url, assignmentName, assignmentId, context, getUserId(), OffsetDateTime.now())
        dbSubmissionId = db.getLastInsert().executeAsOne()

        showProgressNotification(assignmentName, dbSubmissionId)
        val result = apiAsync<Submission> { SubmissionManager.postUrlSubmission(context, assignmentId, url, isLti, it) }

        GlobalScope.launch {
            val uploadResult = result.await()
            uploadResult.onSuccess {
                db.deleteSubmissionById(dbSubmissionId)
            }.onFailure {
                db.setSubmissionError(true, dbSubmissionId)
                showErrorNotification(this@SubmissionService, context, assignmentId, assignmentName, dbSubmissionId)
            }
        }
    }

    private fun uploadMedia(intent: Intent) {
        val mediaFilePath = intent.getStringExtra(Const.MEDIA_FILE_PATH)
        var assignment = intent.getParcelableExtra<Assignment>(Const.ASSIGNMENT)
        val canvasContext = intent.getParcelableExtra<CanvasContext>(Const.CANVAS_CONTEXT)
        val action = intent.getSerializableExtra(Const.ACTION) as NotoriousUploadService.ACTION

        val dbSubmissionId: Long
        val filesDb = Db.getInstance(this).fileSubmissionQueries
        val submissionsDb = Db.getInstance(this).submissionQueries

        // Check if we're retrying a submission or if it's a new one that needs to be persisted
        if (intent.hasExtra(Const.SUBMISSION_ID)) {
            // Get previously attempted submission information so we can retry
            dbSubmissionId = intent.extras!!.getLong(Const.SUBMISSION_ID)
            val submission = submissionsDb.getSubmissionById(dbSubmissionId).executeAsOne()

            assignment = Assignment(
                id = submission.assignmentId!!.toLong(),
                name = submission.assignmentName,
                groupCategoryId = submission.assignmentGroupCategoryId ?: 0
            )
        } else {
            // New submission - store submission details in the db
            submissionsDb.deleteSubmissionsForAssignmentId(assignment.id, getUserId())
            submissionsDb.insertOnlineUploadSubmission(
                assignment.name,
                assignment.id,
                assignment.groupCategoryId,
                canvasContext,
                getUserId(),
                OffsetDateTime.now()
            )
            dbSubmissionId = submissionsDb.getLastInsert().executeAsOne()

            val file = File(mediaFilePath)
            filesDb.insertFile(dbSubmissionId, file.name, file.length(), "", mediaFilePath)
        }

        // Don't show the notification in the foreground so it doesn't disappear when this service dies
        showProgressNotification(assignment.name, dbSubmissionId, false)

        // Handle broadcasts from file upload service
        // Register the receiver on the application context so this service can die and handle the next submission
        val receiver = SubmissionFileUploadReceiver(dbSubmissionId)

        val filter = IntentFilter(FileUploadService.ALL_UPLOADS_COMPLETED)
        filter.addAction(FileUploadService.UPLOAD_ERROR)
        applicationContext.registerReceiver(receiver, filter)

        val mediaFileUploadIntent = Intent(this, NotoriousUploadService::class.java).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Const.MEDIA_FILE_PATH, mediaFilePath)
            putExtra(Const.ACTION, action)
            putExtra(Const.ASSIGNMENT, assignment.copy(courseId = canvasContext.id) as Parcelable)
            putExtra(Const.SUBMISSION_ID, dbSubmissionId)
        }
        startService(mediaFileUploadIntent)
    }

    private fun uploadFile(intent: Intent) {
        var files = intent.getParcelableArrayListExtra<FileSubmitObject>(Const.FILES)
        var assignment = intent.getParcelableExtra<Assignment>(Const.ASSIGNMENT)
        val context = intent.getParcelableExtra<CanvasContext>(Const.CANVAS_CONTEXT)

        val dbSubmissionId: Long
        var attachmentIds: ArrayList<Long>? = null
        val db = Db.getInstance(this)
        val submissionsDb = db.submissionQueries
        val filesDb = db.fileSubmissionQueries

        // Check if we're retrying a submission or if it's a new one that needs to be persisted
        if (intent.hasExtra(Const.SUBMISSION_ID)) {
            dbSubmissionId = intent.extras!!.getLong(Const.SUBMISSION_ID)
            val submission = db.submissionQueries.getSubmissionById(dbSubmissionId).executeAsOne()
            val dbFiles = db.fileSubmissionQueries.getFilesForSubmissionId(dbSubmissionId).executeAsList()

            files = ArrayList(dbFiles.filter { it.attachmentId == null }
                .map { FileSubmitObject(it.name, it.size!!, it.contentType, it.fullPath, it.error) })
            attachmentIds = ArrayList(dbFiles.mapNotNull { it.attachmentId })
            assignment = Assignment(
                id = submission.assignmentId!!.toLong(),
                name = submission.assignmentName,
                groupCategoryId = submission.assignmentGroupCategoryId ?: 0
            )
        } else {
            submissionsDb.deleteSubmissionsForAssignmentId(assignment.id, getUserId())
            submissionsDb.insertOnlineUploadSubmission(
                assignment.name,
                assignment.id,
                assignment.groupCategoryId,
                context,
                getUserId(),
                OffsetDateTime.now()
            )
            dbSubmissionId = submissionsDb.getLastInsert().executeAsOne()

            files.forEach {
                filesDb.insertFile(dbSubmissionId, it.name, it.size, it.contentType, it.fullPath)
            }
        }

        // Don't show the notification in the foreground so it doesn't disappear when this service dies
        showProgressNotification(assignment.name, dbSubmissionId, false)

        // Handle broadcasts from file upload service
        // Register the receiver on the application context so this service can die and handle the next submission
        val receiver = SubmissionFileUploadReceiver(dbSubmissionId)

        val filter = IntentFilter(FileUploadService.ALL_UPLOADS_COMPLETED)
        filter.addAction(FileUploadService.UPLOAD_ERROR)
        applicationContext.registerReceiver(receiver, filter)

        val fileUploadIntent = Intent(this, FileUploadService::class.java).apply {
            action = FileUploadService.ACTION_ASSIGNMENT_SUBMISSION
            putExtras(FileUploadService.getAssignmentSubmissionBundle(files, context.id, assignment!!, dbSubmissionId, attachmentIds))
        }
        startService(fileUploadIntent)
    }

    // region Notifications

    private fun showProgressNotification(assignmentName: String?, submissionId: Long, inForeground: Boolean = true) {
        FileUploadService.createNotificationChannel(notificationManager)
        notificationBuilder = NotificationCompat.Builder(this, FileUploadService.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_canvas_logo)
                .setContentTitle(getString(R.string.assignmentSubmissionUpload, assignmentName))
                .setProgress(0, 0, true)
        if (inForeground) {
            startForeground(submissionId.toInt(), notificationBuilder.build())
        } else {
            notificationManager.notify(submissionId.toInt(), notificationBuilder.build())
        }
    }

    // endregion

    enum class Action {
        TEXT_ENTRY, URL_ENTRY, MEDIA_ENTRY, FILE_ENTRY, STUDIO_ENTRY
    }

    companion object {
        const val FILE_SUBMISSION_FINISHED = "file_submission_finished"

        private fun getSubmissionIntent(context: Context, canvasContext: CanvasContext, assignmentId: Long): PendingIntent {
            val intent = Intent(context, NavigationActivity.startActivityClass).apply {
                putExtra(Const.LOCAL_NOTIFICATION, true)
                putExtra(
                    PushNotification.HTML_URL,
                    "${ApiPrefs.fullDomain}/${canvasContext.apiContext()}/${canvasContext.id}/assignments/$assignmentId"
                )
            }

            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        /**
         * @param notificationId - this should be the submission id in the local database, so we can have different submissions in notifications
         */
        internal fun showErrorNotification(context: Context, canvasContext: CanvasContext, assignmentId: Long, assignmentName: String?, notificationId: Long) {
            val pendingIntent = getSubmissionIntent(context, canvasContext, assignmentId)
            val notificationBuilder = NotificationCompat.Builder(context, FileUploadService.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_canvas_logo)
                .setContentTitle(context.getString(R.string.assignmentSubmissionError, assignmentName ?: ""))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
            NotificationManagerCompat.from(context)
                .notify(notificationId.toInt(), notificationBuilder.build())
        }

        /**
         * @param notificationId - this should be the submission id in the local database, so we can have different submissions in notifications
         */
        internal fun showCompleteNotification(context: Context, canvasContext: CanvasContext, assignmentId: Long, assignmentName: String?, notificationId: Long) {
            val pendingIntent = getSubmissionIntent(context, canvasContext, assignmentId)
            val notificationBuilder = NotificationCompat.Builder(context, FileUploadService.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_canvas_logo)
                .setContentTitle(context.getString(R.string.assignmentSubmissionComplete, assignmentName ?: ""))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
            NotificationManagerCompat.from(context).notify(notificationId.toInt(), notificationBuilder.build())
        }

        // region start helpers

        private fun startService(context: Context, action: Action, extras: Bundle) {
            Intent(context, SubmissionService::class.java).also { intent ->
                intent.action = action.name
                intent.putExtras(extras)
                context.startService(intent)
            }
        }

        fun startTextSubmission(context: Context, canvasContext: CanvasContext, assignmentId: Long, assignmentName: String?, text: String) {
            val bundle = Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putLong(Const.ASSIGNMENT_ID, assignmentId)
                putString(Const.ASSIGNMENT_NAME, assignmentName)
                putString(Const.MESSAGE, text)
            }

            startService(context, Action.TEXT_ENTRY, bundle)
        }

        fun startUrlSubmission(context: Context, canvasContext: CanvasContext, assignmentId: Long, assignmentName: String?, url: String) {
            val bundle = Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putLong(Const.ASSIGNMENT_ID, assignmentId)
                putString(Const.ASSIGNMENT_NAME, assignmentName)
                putString(Const.URL, url)
            }

            startService(context, Action.URL_ENTRY, bundle)
        }

        fun startFileSubmission(context: Context, canvasContext: CanvasContext, assignmentId: Long, assignmentName: String?, assignmentGroupCategoryId: Long = 0, files: ArrayList<FileSubmitObject>) {
            files.ifEmpty { return } // No need to upload files if we aren't given any

            val bundle = Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putParcelable(Const.ASSIGNMENT, Assignment(id = assignmentId, name = assignmentName, groupCategoryId = assignmentGroupCategoryId))
                putParcelableArrayList(Const.FILES, files)
            }

            startService(context, Action.FILE_ENTRY, bundle)
        }

        fun retryFileSubmission(context: Context, canvasContext: CanvasContext, dbSubmissionId: Long) {
            val bundle = Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putLong(Const.SUBMISSION_ID, dbSubmissionId)
            }

            startService(context, Action.FILE_ENTRY, bundle)
        }

        fun startMediaSubmission(context: Context, canvasContext: CanvasContext, assignmentId: Long, assignmentName: String?, assignmentGroupCategoryId: Long, mediaFilePath: String?, notoriousAction: NotoriousUploadService.ACTION) {
            val bundle = Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putParcelable(Const.ASSIGNMENT, Assignment(id = assignmentId, name = assignmentName, groupCategoryId = assignmentGroupCategoryId))
                putString(Const.MEDIA_FILE_PATH, mediaFilePath)
                putSerializable(Const.ACTION, notoriousAction)
            }

            startService(context, Action.MEDIA_ENTRY, bundle)
        }

        fun startStudioSubmission(context: Context, canvasContext: CanvasContext, assignmentId: Long, assignmentName: String?, url: String) {
            val bundle = Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putLong(Const.ASSIGNMENT_ID, assignmentId)
                putString(Const.ASSIGNMENT_NAME, assignmentName)
                putString(Const.URL, url)
            }

            startService(context, Action.STUDIO_ENTRY, bundle)
        }
        // endregion
    }
}

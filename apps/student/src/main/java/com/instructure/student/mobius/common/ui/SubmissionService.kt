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
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.canvasapi2.utils.weave.apiAsync
import com.instructure.pandautils.models.FileSubmitObject
import com.instructure.pandautils.services.FileUploadService
import com.instructure.pandautils.utils.Const
import com.instructure.student.R
import com.instructure.student.db.Db
import com.instructure.student.db.getInstance
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class SubmissionFileUploadReceiver(private val dbSubmissionId: Long) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return

        val submissionId = intent?.extras?.getLong(Const.SUBMISSION)
        val assignmentName = intent?.extras?.getString(Const.ASSIGNMENT_NAME)

        if (submissionId == null || submissionId != dbSubmissionId) return // Only handle our submission

        val db = Db.getInstance(context)

        when (intent.action) {
            FileUploadService.UPLOAD_ERROR -> {
                SubmissionService.showErrorNotification(context, assignmentName, submissionId, intent) // TODO: create intent to go to file upload submission screen

                val message = intent.getStringExtra(Const.MESSAGE)
                val attachments = intent.getParcelableArrayListExtra<Attachment>(Const.ATTACHMENTS)
                val files = db.fileSubmissionQueries.getFilesForSubmissionId(submissionId).executeAsList().sortedBy { it.id }

                // Update files, if we have an attachment it uploaded, otherwise it failed
                files.forEachIndexed { index, file ->
                    if (index < attachments.size) {
                        db.fileSubmissionQueries.setAttachmentId(attachments[index].id, file.id)
                    } else {
                        db.fileSubmissionQueries.setFileError(true, message, file.id)
                    }
                }
            }
            FileUploadService.ALL_UPLOADS_COMPLETED -> {
                SubmissionService.showCompleteNotification(context, assignmentName, submissionId, intent)

                // Clear out the db for the successful submission
                db.fileSubmissionQueries.deleteFilesForSubmissionId(submissionId)
                db.submissionQueries.deleteSubmissionById(submissionId)
            }
            else -> return // Don't do anything on other actions
        }

        // We'll always want to unregister ourselves
        context.unregisterReceiver(this)
    }
}

class SubmissionService : IntentService(SubmissionService::class.java.simpleName) {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    init {
        setIntentRedelivery(true)
    }

//    override fun onCreate() {
//        super.onCreate()
//
//        // perform one-time setup procedures when the service is initially created (before it calls
//        // either onStartCommand() or onBind()). If the service is already running, this method is not called.
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//
//        // clean up any resources such as threads, registered listeners, or receivers
//    }

    override fun onHandleIntent(intent: Intent) {
        val action = intent.action!!

        when (Action.valueOf(action)) {
            Action.TEXT_ENTRY -> uploadText(intent)
            Action.FILE_ENTRY -> uploadFile(intent)
            Action.MEDIA_ENTRY -> uploadMedia(intent)
            Action.URL_ENTRY -> uploadUrl(intent, false)
            Action.ARC_ENTRY -> uploadUrl(intent, true)
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
        db.insertOnlineTextSubmission(text, assignmentName, assignmentId, context)
        dbSubmissionId = db.getLastInsert().executeAsOne()

        showProgressNotification(assignmentName, dbSubmissionId)
        val result = apiAsync<Submission> { SubmissionManager.postTextSubmission(context, assignmentId, text, it) }

        GlobalScope.launch {
            val uploadResult = result.await()
            uploadResult.onSuccess {
                db.deleteSubmissionById(dbSubmissionId)
            }.onFailure {
                db.setSubmissionError(true, dbSubmissionId)
                showErrorNotification(this@SubmissionService, assignmentName, dbSubmissionId, intent)
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
        db.insertOnlineUrlSubmission(url, assignmentName, assignmentId, context)
        dbSubmissionId = db.getLastInsert().executeAsOne()

        showProgressNotification(assignmentName, dbSubmissionId)
        val result = apiAsync<Submission> { SubmissionManager.postUrlSubmission(context, assignmentId, url, isLti, it) }

        GlobalScope.launch {
            val uploadResult = result.await()
            uploadResult.onSuccess {
                db.deleteSubmissionById(dbSubmissionId)
            }.onFailure {
                db.setSubmissionError(true, dbSubmissionId)
                showErrorNotification(this@SubmissionService, assignmentName, dbSubmissionId, intent)
            }
        }
    }

    private fun uploadMedia(intent: Intent) {
        // TODO: Save to persistence
        // TODO: Upload files to server
        // TODO: On broadcast, upload submission with file references
        // TODO: Show progress notification (one already shown for each file)
        // TODO: Update persistence, either delete on success or update with error and show notification
    }

    private fun uploadFile(intent: Intent) {
        val files = intent.getParcelableArrayListExtra<FileSubmitObject>(Const.FILES)
        val assignment = intent.getParcelableExtra<Assignment>(Const.ASSIGNMENT)
        val context = intent.getParcelableExtra<CanvasContext>(Const.CANVAS_CONTEXT)

        // Save to persistence
        val dbSubmissionId: Long
        val db = Db.getInstance(this)
        val submissionsDb = db.submissionQueries
        val filesDb = db.fileSubmissionQueries

        submissionsDb.insertOnlineUploadSubmission(assignment.name, assignment.id, assignment.groupCategoryId, context)
        dbSubmissionId = submissionsDb.getLastInsert().executeAsOne()

        files.forEach {
            filesDb.insertFile(dbSubmissionId, it.name, it.size, it.contentType, it.fullPath)
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
            putExtras(FileUploadService.getAssignmentSubmissionBundle(files, context.id, assignment!!, dbSubmissionId))
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
        TEXT_ENTRY, URL_ENTRY, MEDIA_ENTRY, FILE_ENTRY, ARC_ENTRY
    }

    companion object {
        /**
         * @param notificationId - this should be the submission id in the local database, so we can have different submissions in notifications
         * @param intent - an Intent to launch when the notification is clicked so the user can address any issues
         */
        internal fun showErrorNotification(context: Context, assignmentName: String?, notificationId: Long, intent: Intent) {
            val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            val notificationBuilder = NotificationCompat.Builder(context, FileUploadService.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_canvas_logo)
                .setContentTitle(context.getString(R.string.assignmentSubmissionError, assignmentName ?: ""))
                .setContentIntent(pendingIntent)
            NotificationManagerCompat.from(context)
                .notify(notificationId.toInt(), notificationBuilder.build())
        }

        /**
         * @param notificationId - this should be the submission id in the local database, so we can have different submissions in notifications
         * @param intent - an Intent to launch when the notification is clicked so the user can address any issues
         */
        internal fun showCompleteNotification(context: Context, assignmentName: String?, notificationId: Long, intent: Intent) {
            val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
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

        fun startMediaSubmission(context: Context, canvasContext: CanvasContext, assignmentId: Long, assignmentName: String?) {
            val bundle = Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putLong(Const.ASSIGNMENT_ID, assignmentId)
                putString(Const.ASSIGNMENT_NAME, assignmentName)
            }

            startService(context, Action.MEDIA_ENTRY, bundle)
        }

        fun startArcSubmission(context: Context, canvasContext: CanvasContext, assignmentId: Long, assignmentName: String?, url: String) {
            val bundle = Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putLong(Const.ASSIGNMENT_ID, assignmentId)
                putString(Const.ASSIGNMENT_NAME, assignmentName)
                putString(Const.URL, url)
            }

            startService(context, Action.ARC_ENTRY, bundle)
        }
        // endregion
    }
}

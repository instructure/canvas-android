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
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.canvasapi2.utils.weave.apiAsync
import com.instructure.pandautils.utils.Const
import com.instructure.student.R
import java.util.*

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
        val assignmentName = intent.getStringExtra(Const.ASSIGNMENT)
        val context = intent.getParcelableExtra<CanvasContext>(Const.CANVAS_CONTEXT)

        // TODO: Save to persistence
        showProgressNotification(assignmentName)
        val result = apiAsync<Submission> { SubmissionManager.postTextSubmission(context, assignmentId, text, it) }
        // TODO: Update persistence, either delete on success or update with error and show notification
//        showErrorNotification(assignment.name, assignment.id.toInt(), intent)
    }

    private fun uploadUrl(intent: Intent, isLti: Boolean) {
        val url = intent.getStringExtra(Const.URL)
        val assignmentId = intent.getLongExtra(Const.ASSIGNMENT_ID, 0)
        val assignmentName = intent.getStringExtra(Const.ASSIGNMENT)
        val context = intent.getParcelableExtra<CanvasContext>(Const.CANVAS_CONTEXT)

        // TODO: Save to persistence
        showProgressNotification(assignmentName)
        val result = apiAsync<Submission> { SubmissionManager.postUrlSubmission(context, assignmentId, url, isLti, it) }
        // TODO: Update persistence, either delete on success or update with error and show notification
//        showErrorNotification(assignment.name, assignment.id.toInt(), intent)
    }

    private fun uploadMedia(intent: Intent) {
        // TODO: Save to persistence
        // TODO: Upload files to server
        // TODO: On broadcast, upload submission with file references
        // TODO: Show progress notification (one already shown for each file)
        // TODO: Update persistence, either delete on success or update with error and show notification
    }

    private fun uploadFile(intent: Intent) {
        // TODO: Save to persistence
        // TODO: Upload files to server
        // TODO: On broadcast, upload submission with file references
        // TODO: Show progress notification (one already shown for each file)
        // TODO: Update persistence, either delete on success or update with error and show notification
    }

    // region Notifications

    /**
     * Show an error notification to the user
     *
     * @param assignmentId - used as the notification ID so that we can have many, distinct notifications
     * @param intent - an Intent to launch when the notification is clicked so the user can address
     */
    private fun showErrorNotification(assignmentName: String?, assignmentId: Int, intent: Intent) {
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        createNotificationChannel()
        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_canvas_logo)
                .setContentTitle(String.format(Locale.US, getString(R.string.assignmentSubmissionError), assignmentName))
                .setContentIntent(pendingIntent)
        NotificationManagerCompat.from(this).notify(assignmentId, notificationBuilder.build())
    }

    private fun showProgressNotification(assignmentName: String?) {
        createNotificationChannel()
        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_canvas_logo)
                .setContentTitle(String.format(Locale.US, getString(R.string.assignmentSubmissionUpload), assignmentName))
                .setProgress(0, 0, true)
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        // Prevents recreation of notification channel if it exists.
        if (notificationManager.notificationChannels.any { it.id == CHANNEL_ID }) return

        val name = getString(R.string.notificationChannelNameFileUploadsName)
        val description = getString(R.string.notificationChannelNameFileUploadsDescription)

        // Create the channel and add the group
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description
        channel.enableLights(false)
        channel.enableVibration(false)

        // Create the channel
        notificationManager.createNotificationChannel(channel)
    }

    // endregion

    enum class Action {
        TEXT_ENTRY, URL_ENTRY, MEDIA_ENTRY, FILE_ENTRY, ARC_ENTRY
    }

    companion object {
        private const val CHANNEL_ID = "SubmissionUploadChannel"
        private const val NOTIFICATION_ID = 12223

        fun startTextSubmission(context: Context, canvasContext: CanvasContext, assignmentId: Long, assignmentName: String?, text: String) {
            val bundle = Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putLong(Const.ASSIGNMENT_ID, assignmentId)
                putString(Const.ASSIGNMENT, assignmentName)
                putString(Const.MESSAGE, text)
            }

            startService(context, Action.TEXT_ENTRY, bundle)
        }

        fun startUrlSubmission(context: Context, canvasContext: CanvasContext, assignmentId: Long, assignmentName: String?, url: String) {
            val bundle = Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putLong(Const.ASSIGNMENT_ID, assignmentId)
                putString(Const.ASSIGNMENT, assignmentName)
                putString(Const.URL, url)
            }

            startService(context, Action.URL_ENTRY, bundle)
        }

        fun startFileSubmission(context: Context, canvasContext: CanvasContext, assignmentId: Long, assignmentName: String?) {
            val bundle = Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putLong(Const.ASSIGNMENT_ID, assignmentId)
                putString(Const.ASSIGNMENT, assignmentName)
            }

            startService(context, Action.FILE_ENTRY, bundle)
        }

        fun startMediaSubmission(context: Context, canvasContext: CanvasContext, assignmentId: Long, assignmentName: String?) {
            val bundle = Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putLong(Const.ASSIGNMENT_ID, assignmentId)
                putString(Const.ASSIGNMENT, assignmentName)
            }

            startService(context, Action.MEDIA_ENTRY, bundle)
        }

        fun startArcSubmission(context: Context, canvasContext: CanvasContext, assignmentId: Long, assignmentName: String?, url: String) {
            val bundle = Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putLong(Const.ASSIGNMENT_ID, assignmentId)
                putString(Const.ASSIGNMENT, assignmentName)
                putString(Const.URL, url)
            }

            startService(context, Action.ARC_ENTRY, bundle)
        }

        private fun startService(context: Context, action: Action, extras: Bundle) {
            Intent(context, SubmissionService::class.java).also { intent ->
                intent.action = action.name
                intent.putExtras(extras)
                context.startService(intent)
            }
        }
    }
}

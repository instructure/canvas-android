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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.notorious.NotoriousResult
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.canvasapi2.utils.FileUtils
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.ProgressRequestUpdateListener
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.NotoriousUploader
import com.instructure.pandautils.utils.fromJson
import com.instructure.pandautils.utils.toJson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

@HiltWorker
class NotoriousUploadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationManager: NotificationManager,
    private val submissionApi: SubmissionAPI.SubmissionInterface,
    private val localBroadcastManager: LocalBroadcastManager,
    private val notoriousUploader: NotoriousUploader
) : CoroutineWorker(context, workerParams) {
    private lateinit var builder: NotificationCompat.Builder

    private val mediaCommentId = inputData.getLong(Const.ID, -1L)
    private val assignment: Assignment? = inputData.getString(Const.ASSIGNMENT)?.fromJson()
    private val studentId = inputData.getLong(Const.STUDENT_ID, -1L)
    private val isGroupComment = inputData.getBoolean(Const.IS_GROUP, false)
    private val pageId = inputData.getString(Const.PAGE_ID)
    private val attemptId = inputData.getLong(Const.SUBMISSION_ATTEMPT, -1L).takeIf { it != -1L }
    private val mediaPath = inputData.getString(Const.MEDIA_FILE_PATH)

    private val notificationId = assignment?.id?.toInt() ?: 0

    override suspend fun doWork(): Result {
        return if (!mediaPath.isNullOrEmpty()) {
            setForeground(createForegroundInfo())
            startFileUpload(mediaPath)
        } else {
            handleFailure(null)
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        createNotificationChannel()

        builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_canvas_logo)
            .setContentTitle(context.getString(R.string.notificationTitle))
            .setContentText(context.getString(R.string.preparingUpload))
            .setOngoing(true)

        val notification = builder.build()

        val foregroundNotificationId = Random.nextInt()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(foregroundNotificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(foregroundNotificationId, notification)
        }
    }

    private fun createNotificationChannel(channelId: String = CHANNEL_ID) {
        // Prevents recreation of notification channel if it exists.
        val channelList = notificationManager.notificationChannels
        for (channel in channelList) {
            if (channelId == channel.id) {
                return
            }
        }

        val name = context.getString(R.string.notificationChannelNameFileUploadsName)
        val description = context.getString(R.string.notificationChannelNameFileUploadsDescription)

        // Create the channel and add the group
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance)
        channel.description = description
        channel.enableLights(false)
        channel.enableVibration(false)

        // Create the channel
        notificationManager.createNotificationChannel(channel)
    }

    private suspend fun startFileUpload(mediaPath: String): Result {
        // Set initial progress in notification
        builder.setContentText(this@NotoriousUploadWorker.context.getString(R.string.uploadingFile))
        builder.setProgress(0, 0, true)
        notificationManager.notify(notificationId, builder.build())
        uploadStartedToast()

        val result = notoriousUploader.performUpload(mediaPath, object : ProgressRequestUpdateListener {
            override fun onProgressUpdated(progressPercent: Float, length: Long): Boolean {
                // Update progress in notification
                builder.setProgress(1000, (progressPercent * 1000).toInt(), false)
                notificationManager.notify(notificationId, builder.build())
                return true
            }
        })

        return when (result) {
            is DataResult.Success -> {
                handleSuccess(result.dataOrThrow)
            }

            is DataResult.Fail -> {
                handleFailure(result.failure)
            }
        }
    }

    private fun handleFailure(failure: Failure?): Result {
        when (failure) {
            is Failure.Network -> uploadError(failure.message)
            is Failure.Exception -> uploadError(failure.exception)
            else -> uploadError("Unknown error")
        }
        return Result.failure()
    }

    private suspend fun handleSuccess(result: NotoriousResult): Result {
        val mediaId = result.id ?: return handleFailure(null)
        val assignment = assignment ?: return handleFailure(null)
        val mediaType = FileUtils.mediaTypeFromNotoriousCode(result.mediaType)
        val course = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, assignment.courseId, Const.COURSE)

        val params = RestParams(domain = ApiPrefs.overrideDomains[course.id], shouldLoginOnTokenError = false)
        val postMediaSubmissionCommentResult = submissionApi.postMediaSubmissionComment(
            courseId = course.id,
            assignmentId = assignment.id,
            userId = studentId,
            attemptId = attemptId,
            mediaId = mediaId,
            commentType = mediaType,
            isGroupComment = isGroupComment,
            restParams = params
        )

        return when (postMediaSubmissionCommentResult) {
            is DataResult.Success -> {
                broadcastSubmission(postMediaSubmissionCommentResult.dataOrThrow)
            }

            is DataResult.Fail -> {
                handleFailure(postMediaSubmissionCommentResult.failure)
            }
        }
    }

    private fun broadcastSubmission(submission: Submission): Result {
        builder.setContentText(context.getString(R.string.fileUploadSuccess))
            .setProgress(100, 100, false)
            .setOngoing(false)
        notificationManager.notify(notificationId, builder.build())

        val intent = Intent(Const.SUBMISSION_COMMENT_SUBMITTED)
        localBroadcastManager.sendBroadcast(intent)

        // TODO: This is caught in ParentFragment, which we are moving away from as we integrate Mobius into more parts of the app.
        //       We'll want to change out this intent with something like FileUploadService.ALL_UPLOADS_COMPLETED, which is caught in the
        //       SubmissionFileUploadReceiver
        val successIntent = Intent(Const.UPLOAD_SUCCESS)
        localBroadcastManager.sendBroadcast(successIntent)

        val uploadCompleteIntent = Intent(ALL_UPLOADS_COMPLETED)
        uploadCompleteIntent.putExtra(Const.SUBMISSION_ID, notificationId.toLong())
        assignment?.name?.let { uploadCompleteIntent.putExtra(Const.ASSIGNMENT_NAME, it) }
        context.sendBroadcast(uploadCompleteIntent)

        val mediaUploadIntent = Intent(Const.ACTION_MEDIA_UPLOAD_SUCCESS)
        mediaUploadIntent.putExtra(Const.MEDIA_FILE_PATH, mediaPath)
        mediaUploadIntent.putExtra(Const.PAGE_ID, pageId)
        mediaUploadIntent.putExtra(Const.ID, mediaCommentId)
        mediaUploadIntent.putParcelableArrayListExtra(
            Const.SUBMISSION_COMMENT_LIST,
            ArrayList(submission.submissionComments)
        )
        localBroadcastManager.sendBroadcast(mediaUploadIntent)
        return Result.success()
    }

    private fun uploadError(e: Throwable) {
        Logger.e("NOTORIOUS EXCEPTION: $e")
        e.printStackTrace()
        sendErrorBroadcast()
        showErrorNotification()
    }

    private fun uploadError(message: String? = null) {
        Logger.e(message ?: "UNKNOWN ERROR CAUSE")
        sendErrorBroadcast()
        showErrorNotification()
    }

    private fun showErrorNotification() {
        builder.setContentText(context.getString(R.string.errorUploadingFile))
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
        localBroadcastManager.sendBroadcast(errorIntent)
    }

    companion object {
        // Upload broadcasts, extended string to ensure unique strings across the device
        private const val BROADCAST_BASE = "com.instructure.pandautils.services"
        const val ALL_UPLOADS_COMPLETED = "$BROADCAST_BASE.ALL_UPLOADS_COMPLETED"

        const val CHANNEL_ID = "uploadChannel"

        fun enqueueUpload(
            context: Context,
            mediaFilePath: String?,
            assignment: Assignment?,
            studentId: Long?,
            isGroupComment: Boolean,
            pageId: String?,
            attemptId: Long?,
            mediaCommentId: Long?
        ): Flow<WorkInfo?> {
            val data = workDataOf(
                Const.MEDIA_FILE_PATH to mediaFilePath,
                Const.ASSIGNMENT to assignment?.toJson(),
                Const.STUDENT_ID to studentId,
                Const.IS_GROUP to isGroupComment,
                Const.PAGE_ID to pageId,
                Const.SUBMISSION_ATTEMPT to attemptId,
                Const.ID to mediaCommentId
            )

            val workRequest = OneTimeWorkRequest.Builder(NotoriousUploadWorker::class.java)
                .setInputData(data)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .addTag("NotoriousUploadWorker")
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
            return WorkManager.getInstance(context).getWorkInfoByIdFlow(workRequest.id)
        }
    }
}

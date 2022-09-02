/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.file.upload.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.instructure.canvasapi2.managers.*
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.ProgressRequestUpdateListener
import com.instructure.pandautils.R
import com.instructure.pandautils.features.file.upload.FileUploadUtilsHelper
import com.instructure.pandautils.features.file.upload.preferences.FileUploadPreferences
import com.instructure.pandautils.toJson
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FileUploadUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.coroutines.suspendCoroutine

class FileUploadWorker(private val context: Context, private val workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val courseId = inputData.getLong(Const.COURSE_ID, INVALID_ID)
    private val assignmentId = inputData.getLong(Const.ASSIGNMENT_ID, INVALID_ID)
    private val quizQuestionId = inputData.getLong(Const.QUIZ_ANSWER_ID, INVALID_ID)
    private val quizId = inputData.getLong(Const.QUIZ, INVALID_ID)
    private val position = inputData.getInt(Const.POSITION, INVALID_ID.toInt())
    private val parentFolderId = inputData.getLong(Const.PARENT_FOLDER_ID, INVALID_ID)
    private val notificationId = notificationId(inputData)
    private val submissionId = inputData.getLong(Const.SUBMISSION_ID, INVALID_ID)
    private val action = inputData.getString(FILE_SUBMIT_ACTION)

    private var fullSize = 0L
    private var currentProgress = 0L
    private var uploaded = 0L

    private var uploadCount = 0

    private val workDataBuilder = Data.Builder()

    override suspend fun doWork(): Result {
        try {
            var assignmentName = ""
            var groupId: Long? = null
            if (assignmentId != INVALID_ID && courseId != INVALID_ID) {
                val assignment = getAssignment(assignmentId, courseId)
                groupId = getGroupId(assignment, courseId)
                assignmentName = assignment.name.orEmpty()
            }

            val title = context.getString(
                if (action == ACTION_ASSIGNMENT_SUBMISSION) {
                    R.string.dashboardNotificationUploadingSubmissionTitle
                } else {
                    R.string.dashboardNotificationUploadingFilesTitle
                }
            )

            setProgress(
                Data.Builder()
                    .putString(PROGRESS_DATA_TITLE, title)
                    .putString(PROGRESS_DATA_SUBTITLE, assignmentName)
                    .build()
            )

            FileUploadPreferences.addWorkerId(id)

            var groupId: Long? = null
            if (assignmentId != INVALID_ID && courseId != INVALID_ID) {
                val assignment = getAssignment(assignmentId, courseId)
                workDataBuilder.putString(ASSIGNMENT_NAME, assignment.name)
                groupId = getGroupId(assignment, courseId)
            }

            val filePaths = inputData.getStringArray(FILE_PATHS)

            val fileSubmitObjects = filePaths?.let {
                getFileSubmitObjects(it)
            } ?: throw IllegalArgumentException()

            val fsoJson = fileSubmitObjects.map {
                it.toJson()
            }.toTypedArray()
            workDataBuilder.putStringArray(FILES_IN_PROGRESS, fsoJson)

            fullSize = fileSubmitObjects.sumOf { it.size }
            workDataBuilder.putLong(FULL_SIZE, fullSize)

            uploadCount = fileSubmitObjects.size

            setProgress(workDataBuilder.build())

            val attachments = uploadFiles(fileSubmitObjects, groupId)

            val attachmentsIds = attachments.map { it.id }.plus(
                inputData.getLongArray(Const.ATTACHMENTS)?.toList()
                    ?: emptyList()
            )

            val result = when (action) {
                ACTION_ASSIGNMENT_SUBMISSION -> {
                    submitAttachmentsToSubmission(attachmentsIds)?.let {
                        updateSubmissionComplete(notificationId)
                        Result.success()
                    } ?: Result.retry()
                }
                ACTION_MESSAGE_ATTACHMENTS -> {
                    updateNotificationComplete(notificationId)
                    val attachmentJsons = attachments.map { it.toJson() }.toTypedArray()
                    Result.success(workDataOf(RESULT_ATTACHMENTS to attachmentJsons))
                }
                else -> {
                    updateNotificationComplete(notificationId)
                    Result.success()
                }
            }

            FileUploadPreferences.removeWorkerId(id)
            return result
        } catch (e: Exception) {
            FileUploadPreferences.removeWorkerId(id)
            e.printStackTrace()
            return Result.failure()
        }
    }

    private fun getFileSubmitObjects(filePaths: Array<String>): List<FileSubmitObject> {
        val fileSubmitObjects = filePaths.map {
            val uri = Uri.parse(it)
            val fileUploadUtilsHelper = FileUploadUtilsHelper(FileUploadUtils, context, context.contentResolver)
            val mimeType = fileUploadUtilsHelper.getFileMimeType(uri)
            val fileName = fileUploadUtilsHelper.getFileNameWithDefault(uri)

            fileUploadUtilsHelper.getFileSubmitObjectFromInputStream(uri, fileName, mimeType)
        }
        if (fileSubmitObjects.contains(null)) throw IllegalArgumentException("Could not parse file.")

        return fileSubmitObjects.filterNotNull()
    }

    private suspend fun getAssignment(assignmentId: Long, courseId: Long): Assignment {
        return AssignmentManager.getAssignmentAsync(assignmentId, courseId, true).await().dataOrThrow
    }

    private suspend fun getGroupId(assignment: Assignment, courseId: Long): Long? {
        return if (assignment.groupCategoryId != 0L) {
            GroupManager.getAllGroupsForCourseAsync(courseId, true).await().dataOrThrow
                .find { it.groupCategoryId == assignment.groupCategoryId }?.id ?: throw IllegalArgumentException()
        } else {
            null
        }
    }

    private suspend fun uploadFiles(fileSubmitObjects: List<FileSubmitObject>, groupId: Long?): List<Attachment> {
        val attachments = mutableListOf<Attachment>()

        fileSubmitObjects.forEachIndexed { index, fileSubmitObject ->
            setForeground(createForegroundInfo(notificationId, fileSubmitObject.name, index + 1))
            val config: FileUploadConfig = when (action) {
                ACTION_ASSIGNMENT_SUBMISSION -> {
                    if (groupId == null) {
                        FileUploadConfig.forSubmission(fileSubmitObject, courseId, assignmentId)
                    } else {
                        FileUploadConfig.forGroup(fileSubmitObject, groupId)
                    }
                }
                ACTION_COURSE_FILE -> FileUploadConfig.forCourse(
                    fileSubmitObject,
                    courseId,
                    if (parentFolderId != INVALID_ID) parentFolderId else null
                )
                ACTION_GROUP_FILE -> FileUploadConfig.forGroup(
                    fileSubmitObject,
                    courseId,
                    if (parentFolderId != INVALID_ID) parentFolderId else null
                )
                ACTION_USER_FILE -> FileUploadConfig.forUser(
                    fileSubmitObject,
                    if (parentFolderId != INVALID_ID) parentFolderId else null
                )
                ACTION_MESSAGE_ATTACHMENTS -> FileUploadConfig.forUser(
                    fileSubmitObject,
                    parentFolderPath = MESSAGE_ATTACHMENT_PATH
                )
                ACTION_QUIZ_FILE -> FileUploadConfig.forQuiz(fileSubmitObject, courseId, quizId)
                ACTION_DISCUSSION_ATTACHMENT -> FileUploadConfig.forUser(
                    fileSubmitObject,
                    parentFolderPath = DISCUSSION_ATTACHMENT_PATH
                )
                ACTION_SUBMISSION_COMMENT -> FileUploadConfig.forSubmissionComment(
                    fileSubmitObject,
                    courseId,
                    assignmentId
                )
                else -> throw IllegalArgumentException("Unknown file upload action: $action")
            }

            attachments += FileUploadManager.uploadFile(config, object : ProgressRequestUpdateListener {
                override fun onProgressUpdated(progressPercent: Float, length: Long): Boolean {
                    currentProgress = uploaded + (fileSubmitObject.size * progressPercent).toLong()
                    workDataBuilder.putLong(CURRENT_PROGRESS, currentProgress)
                    setProgressAsync(workDataBuilder.build())
                    return true
                }
            }).dataOrThrow

            val updatedList = workDataBuilder.build().getStringArray(FILES_SUCCEEDED).orEmpty().toMutableList().apply {
                add(fileSubmitObject.toJson())
            }.toTypedArray()
            uploaded += fileSubmitObject.size
            currentProgress = uploaded
            workDataBuilder.putStringArray(FILES_SUCCEEDED, updatedList)
            workDataBuilder.putLong(CURRENT_PROGRESS, currentProgress)
            setProgress(workDataBuilder.build())
        }
        return attachments
    }

    private fun submitAttachmentsToSubmission(attachmentIds: List<Long>): Submission? {
        return SubmissionManager.postSubmissionAttachmentsSynchronous(courseId, assignmentId, attachmentIds)
    }

    private fun notificationId(data: Data): Int {
        return if (data.getLong(Const.SUBMISSION_ID, INVALID_ID) != INVALID_ID) {
            data.getLong(Const.SUBMISSION_ID, INVALID_ID).toInt()
        } else {
            NOTIFICATION_ID
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager, channelId: String = CHANNEL_ID) {
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

    private fun createForegroundInfo(notificationId: Int, fileName: String, currentItem: Int): ForegroundInfo {
        createNotificationChannel(notificationManager)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_canvas_logo)
            .setContentTitle(
                String.format(
                    Locale.US,
                    context.getString(R.string.uploadingFileNum),
                    currentItem,
                    uploadCount
                )
            )
            .setContentText(fileName)
            .setProgress(0, 0, true)
            .setOngoing(true)
            .build()

        return ForegroundInfo(notificationId, notification)
    }

    private fun updateNotificationComplete(notificationId: Int) {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_canvas_logo)
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setContentTitle(context.getString(R.string.filesUploadedSuccessfully))
            .build()
        notificationManager.notify(notificationId + 1, notification)
    }

    private fun updateSubmissionComplete(notificationId: Int) {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_canvas_logo)
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setContentTitle(context.getString(R.string.filesSubmittedSuccessfully))
            .build()
        notificationManager.notify(notificationId + 1, notification)
    }

    companion object {
        private const val NOTIFICATION_ID = -2
        const val INVALID_ID = -1L
        const val FILE_SUBMIT_ACTION = "fileSubmitAction"
        const val FILE_PATHS = "filePaths"
        const val CHANNEL_ID = "uploadChannel"

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

        const val RESULT_ATTACHMENTS = "attachments"

        const val FILES_IN_PROGRESS = "filesInProgress"
        const val FILES_SUCCEEDED = "filesSucceeded"
        const val FILES_FAILED = "filesFailed"
        const val ASSIGNMENT_NAME = "assignmentName"
        const val FULL_SIZE = "fullSize"
        const val CURRENT_PROGRESS = "currentProgress"
        const val PROGRESS_DATA_TITLE = "PROGRESS_DATA_TITLE"
        const val PROGRESS_DATA_SUBTITLE = "PROGRESS_DATA_SUBTITLE"
    }
}
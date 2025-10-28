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
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.instructure.canvasapi2.managers.*
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.ProgressRequestUpdateListener
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.pandautils.R
import com.instructure.pandautils.features.file.upload.FileUploadUtilsHelper
import com.instructure.pandautils.room.appdatabase.daos.*
import com.instructure.pandautils.room.appdatabase.entities.*
import com.instructure.pandautils.utils.FileUploadUtils
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.toJson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.lang.IllegalStateException
import java.util.*

@HiltWorker
class FileUploadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val fileUploadInputDao: FileUploadInputDao,
    private val attachmentDao: AttachmentDao,
    private val submissionCommentDao: SubmissionCommentDao,
    private val mediaCommentDao: MediaCommentDao,
    private val authorDao: AuthorDao,
    private val dashboardFileUploadDao: DashboardFileUploadDao,
    private val apiPrefs: ApiPrefs,
    private val fileUploadUtilsHelper: FileUploadUtilsHelper
) : CoroutineWorker(context, workerParameters) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var courseId: Long = INVALID_ID
    private var assignmentId: Long = INVALID_ID
    private var quizId: Long = INVALID_ID
    private var parentFolderId: Long = INVALID_ID
    private lateinit var action: String
    private var userId: Long = INVALID_ID
    private var attemptId: Long? = null
    private var notificationId: Int = DEFAULT_NOTIFICATION_ID

    private var fullSize = 0L
    private var currentProgress = 0L
    private var uploaded = 0L

    private var uploadCount = 0

    private val workDataBuilder = Data.Builder()

    override suspend fun doWork(): Result {
        var title = ""
        var subtitle = ""
        try {
            val inputData = fileUploadInputDao.findByWorkerId(id.toString()) ?: throw IllegalArgumentException()
            getArguments(inputData)

            val fileSubmitObjects = getFileSubmitObjects(inputData.filePaths)
            fullSize = fileSubmitObjects.sumOf { it.size }
            uploadCount = fileSubmitObjects.size

            title = if (action == ACTION_ASSIGNMENT_SUBMISSION) {
                context.getString(R.string.dashboardNotificationUploadingSubmissionTitle)
            } else {
                context.resources.getQuantityString(R.plurals.dashboardNotificationUploadingFilesTitle, uploadCount)
            }

            var assignmentName = ""
            var groupId: Long? = null
            if (assignmentId != INVALID_ID && courseId != INVALID_ID) {
                val assignment = getAssignment(assignmentId, courseId)
                groupId = getGroupId(assignment, courseId)
                assignmentName = assignment.name.orEmpty()
            }

            setProgress(
                workDataBuilder
                    .putString(PROGRESS_DATA_TITLE, title)
                    .putString(PROGRESS_DATA_ASSIGNMENT_NAME, assignmentName)
                    .putLong(PROGRESS_DATA_FULL_SIZE, fullSize)
                    .putStringArray(PROGRESS_DATA_FILES_TO_UPLOAD, fileSubmitObjects.map { it.toJson() }.toTypedArray())
                    .build()
            )

            subtitle = assignmentName.ifEmpty { fileSubmitObjects.joinToString { it.name } }

            if (shouldShowDashboardNotification(action)) insertDashboardUpload(title, subtitle)

            val attachments = uploadFiles(fileSubmitObjects, groupId)

            val attachmentsIds = attachments.map { it.id }.plus(
                inputData.attachments.toList()
            )

            val result = when (action) {
                ACTION_ASSIGNMENT_SUBMISSION -> {
                    submitAttachmentsToSubmission(attachmentsIds)?.let {
                        updateSubmissionComplete(notificationId)
                        attemptId = it.attempt
                        Result.success()
                    } ?: throw IllegalStateException("Failed to attach file to submission")
                }
                ACTION_MESSAGE_ATTACHMENTS -> {
                    updateNotificationComplete(notificationId)
                    val attachmentEntities = attachments.map { AttachmentEntity(it, id.toString()) }
                    attachmentDao.insertAll(attachmentEntities)
                    Result.success()
                }
                ACTION_TEACHER_SUBMISSION_COMMENT -> {
                    postSubmissionComment(attachmentsIds).let {
                        updateSubmissionComplete(notificationId)
                        val submissionCommentId = insertSubmissionComment(it.submissionComments.last())

                        Result.success(
                            workDataOf(
                                RESULT_SUBMISSION_COMMENT to submissionCommentId
                            )
                        )
                    }
                }
                else -> {
                    updateNotificationComplete(notificationId)
                    Result.success()
                }
            }

            title = context.getString(
                if (action == ACTION_ASSIGNMENT_SUBMISSION) {
                    R.string.dashboardNotificationSubmissionUploadSuccessTitle
                } else {
                    R.string.dashboardNotificationUploadingFilesSuccessTitle
                }
            )

            fileUploadInputDao.delete(inputData)
            fileUploadUtilsHelper.deleteCachedFiles(inputData.filePaths)
            return result
        } catch (e: Exception) {
            title = context.getString(
                if (action == ACTION_ASSIGNMENT_SUBMISSION) {
                    R.string.dashboardNotificationSubmissionUploadFailedTitle
                } else {
                    R.string.dashboardNotificationUploadingFilesFailedTitle
                }
            )
            e.printStackTrace()
            return Result.failure(workDataBuilder.build())
        } finally {
            if (shouldShowDashboardNotification(action)) insertDashboardUpload(title, subtitle)
        }
    }

    private suspend fun insertDashboardUpload(title: String, subtitle: String) {
        val userId = apiPrefs.user?.id ?: return
        dashboardFileUploadDao.insert(
            DashboardFileUploadEntity(
                workerId = id.toString(),
                userId = userId,
                title = title,
                subtitle = subtitle,
                courseId = courseId.takeIf { it != INVALID_ID },
                assignmentId = assignmentId.takeIf { it != INVALID_ID },
                folderId = if (action == ACTION_USER_FILE) parentFolderId.takeIf { it != INVALID_ID }.orDefault() else null,
                attemptId = attemptId.takeIf { it != INVALID_ID }
            )
        )
    }

    private fun shouldShowDashboardNotification(action: String) = action == ACTION_ASSIGNMENT_SUBMISSION || action == ACTION_USER_FILE

    private suspend fun insertSubmissionComment(submissionComment: SubmissionComment): Long {
        val submissionCommentId = submissionCommentDao.insert(
            SubmissionCommentEntity(submissionComment)
        )

        submissionComment.mediaComment?.let {
            mediaCommentDao.insert(
                MediaCommentEntity(
                    it
                )
            )
        }

        submissionComment.author?.let {
            authorDao.insert(
                AuthorEntity(it)
            )
        }

        val attachmentEntities = submissionComment.attachments.map {
            AttachmentEntity(
                attachment = it,
                submissionCommentId = submissionCommentId
            )
        }
        attachmentDao.insertAll(attachmentEntities)

        return submissionCommentId
    }

    private fun getArguments(inputData: FileUploadInputEntity) {
        courseId = inputData.courseId ?: INVALID_ID
        assignmentId = inputData.assignmentId ?: INVALID_ID
        quizId = inputData.quizId ?: INVALID_ID
        parentFolderId = inputData.parentFolderId ?: INVALID_ID
        action = inputData.action
        userId = inputData.userId ?: INVALID_ID
        attemptId = inputData.attemptId
        notificationId = inputData.notificationId ?: DEFAULT_NOTIFICATION_ID
    }

    private fun getFileSubmitObjects(filePaths: List<String>): List<FileSubmitObject> {
        val fileSubmitObjects = filePaths.map {
            val uri = Uri.parse(it)
            val fileUploadUtilsHelper = FileUploadUtilsHelper(FileUploadUtils, context, context.contentResolver)
            val mimeType = fileUploadUtilsHelper.getFileMimeType(uri)
            val fileName = fileUploadUtilsHelper.getFileNameWithDefault(uri)

            fileUploadUtilsHelper.getFileSubmitObjectByFileUri(uri, fileName, mimeType)
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
                ACTION_TEACHER_SUBMISSION_COMMENT -> FileUploadConfig.forSubmissionCommentFromTeacher(
                    fileSubmitObject,
                    courseId,
                    assignmentId,
                    userId
                )
                else -> throw IllegalArgumentException("Unknown file upload action: $action")
            }

            attachments += FileUploadManager.uploadFile(config, object : ProgressRequestUpdateListener {
                override fun onProgressUpdated(progressPercent: Float, length: Long): Boolean {
                    currentProgress = uploaded + (fileSubmitObject.size * progressPercent).toLong()
                    setProgressAsync(
                        workDataBuilder
                            .putLong(PROGRESS_DATA_UPLOADED_SIZE, currentProgress)
                            .build()
                    )
                    return !this@FileUploadWorker.isStopped
                }
            }).dataOrThrow

            val updatedList: Array<String?> = workDataBuilder.build()
                .getStringArray(PROGRESS_DATA_UPLOADED_FILES)
                .orEmpty()
                .toMutableList()
                .apply { add(fileSubmitObject.toJson()) }
                .toTypedArray()

            uploaded += fileSubmitObject.size
            currentProgress = uploaded

            setProgress(
                workDataBuilder
                    .putStringArray(PROGRESS_DATA_UPLOADED_FILES, updatedList)
                    .putLong(PROGRESS_DATA_UPLOADED_SIZE, currentProgress)
                    .build()
            )
        }
        return attachments
    }

    private fun submitAttachmentsToSubmission(attachmentIds: List<Long>): Submission? {
        return SubmissionManager.postSubmissionAttachmentsSynchronous(courseId, assignmentId, attachmentIds)
    }

    private suspend fun postSubmissionComment(attachmentIds: List<Long>) = awaitApi<Submission> {
        SubmissionManager.postSubmissionComment(
            courseId,
            assignmentId,
            userId,
            "",
            false,
            attachmentIds,
            it,
            attemptId
        )
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

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
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
        private const val DEFAULT_NOTIFICATION_ID = -2
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
        const val ACTION_TEACHER_SUBMISSION_COMMENT = "ACTION_SUBMISSION_COMMENT_TEACHER"

        const val MESSAGE_ATTACHMENT_PATH = "conversation attachments"
        const val DISCUSSION_ATTACHMENT_PATH = "discussion attachments"

        const val RESULT_SUBMISSION_COMMENT = "submission-comment"

        const val PROGRESS_DATA_FILES_TO_UPLOAD = "filesToUpload"
        const val PROGRESS_DATA_UPLOADED_FILES = "uploadedFiles"
        const val PROGRESS_DATA_ASSIGNMENT_NAME = "assignmentName"
        const val PROGRESS_DATA_FULL_SIZE = "fullSize"
        const val PROGRESS_DATA_UPLOADED_SIZE = "uploadedSize"
        const val PROGRESS_DATA_TITLE = "PROGRESS_DATA_TITLE"
    }
}
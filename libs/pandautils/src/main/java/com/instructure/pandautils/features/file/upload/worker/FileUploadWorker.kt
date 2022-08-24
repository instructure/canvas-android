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

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.instructure.canvasapi2.managers.*
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.pandautils.features.file.upload.FileUploadUtilsHelper
import com.instructure.pandautils.toJson
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FileUploadUtils

class FileUploadWorker(private val context: Context, private val workerParameters: WorkerParameters) : CoroutineWorker(context, workerParameters) {

    private val courseId = inputData.getLong(Const.COURSE_ID, INVALID_ID)
    private val assignmentId = inputData.getLong(Const.ASSIGNMENT_ID, INVALID_ID)
    private val quizQuestionId = inputData.getLong(Const.QUIZ_ANSWER_ID, INVALID_ID)
    private val quizId = inputData.getLong(Const.QUIZ, INVALID_ID)
    private val position = inputData.getInt(Const.POSITION, INVALID_ID.toInt())
    private val parentFolderId = inputData.getLong(Const.PARENT_FOLDER_ID, INVALID_ID)
    private val notificationId = notificationId(inputData)
    private val submissionId = inputData.getLong(Const.SUBMISSION_ID, INVALID_ID)
    private val action = inputData.getString(FILE_SUBMIT_ACTION)

    override suspend fun doWork(): Result {
        try {
            val filePaths = inputData.getStringArray(FILE_PATHS)

            val fileSubmitObjects = filePaths?.let {
                getFileSubmitObjects(it)
            } ?: return Result.failure()

            var groupId: Long? = null
            if (assignmentId != INVALID_ID && courseId != INVALID_ID) {
                val assignment = getAssignment(assignmentId, courseId)
                groupId = getGroupId(assignment, courseId)
            }

            val attachments = uploadFiles(fileSubmitObjects, groupId)

            val attachmentsIds = attachments.map { it.id }.plus(inputData.getLongArray(Const.ATTACHMENTS)?.toList() ?: emptyList())

            return when (action) {
                ACTION_ASSIGNMENT_SUBMISSION -> {
                    submitAttachmentsToSubmission(attachmentsIds)?.let {
                        Result.success()
                    } ?: Result.retry()
                }
                ACTION_MESSAGE_ATTACHMENTS -> {
                    val attachmentJsons = attachments.map { it.toJson() }.toTypedArray()
                    Result.success(workDataOf(RESULT_ATTACHMENTS to attachmentJsons))
                }
                else -> {
                    Result.success()
                }
            }
        } catch (e: Exception) {
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

    private fun uploadFiles(fileSubmitObjects: List<FileSubmitObject>, groupId: Long?): List<Attachment> {
        val attachments = mutableListOf<Attachment>()

        fileSubmitObjects.forEach {
            val config: FileUploadConfig = when (action) {
                ACTION_ASSIGNMENT_SUBMISSION -> {
                    if (groupId == null) {
                        FileUploadConfig.forSubmission(it, courseId, assignmentId)
                    } else {
                        FileUploadConfig.forGroup(it, groupId)
                    }
                }
                ACTION_COURSE_FILE -> FileUploadConfig.forCourse(it, courseId, if (parentFolderId != INVALID_ID) parentFolderId else null)
                ACTION_GROUP_FILE -> FileUploadConfig.forGroup(it, courseId, if (parentFolderId != INVALID_ID) parentFolderId else null)
                ACTION_USER_FILE -> FileUploadConfig.forUser(it, if (parentFolderId != INVALID_ID) parentFolderId else null)
                ACTION_MESSAGE_ATTACHMENTS -> FileUploadConfig.forUser(it, parentFolderPath = MESSAGE_ATTACHMENT_PATH)
                ACTION_QUIZ_FILE -> FileUploadConfig.forQuiz(it, courseId, quizId)
                ACTION_DISCUSSION_ATTACHMENT -> FileUploadConfig.forUser(it, parentFolderPath = DISCUSSION_ATTACHMENT_PATH)
                ACTION_SUBMISSION_COMMENT -> FileUploadConfig.forSubmissionComment(it, courseId, assignmentId)
                else -> throw IllegalArgumentException("Unknown file upload action: $action")
            }

            attachments += FileUploadManager.uploadFile(config).dataOrThrow
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

    companion object {
        private const val NOTIFICATION_ID = -2
        private const val INVALID_ID = -1L
        const val FILE_SUBMIT_ACTION = "fileSubmitAction"
        const val FILE_PATHS = "filePaths"

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

        fun getUserFilesBundle(
                fileSubmitObjects: List<Uri>,
                parentFolderId: Long?
        ) = Data.Builder()
                .putStringArray(FILE_PATHS, fileSubmitObjects.map { it.toString() }.toTypedArray())
                .putLong(Const.PARENT_FOLDER_ID, parentFolderId ?: INVALID_ID)

        fun getQuizFileBundle(
                fileSubmitObjects: List<Uri>,
                parentFolderId: Long?,
                quizQuestionId: Long,
                position: Int,
                courseId: Long,
                quizId: Long
        ) = Data.Builder()
                .putStringArray(FILE_PATHS, fileSubmitObjects.map { it.toString() }.toTypedArray())
                .putLong(Const.QUIZ_ANSWER_ID, quizQuestionId)
                .putLong(Const.QUIZ, quizId)
                .putLong(Const.COURSE_ID, courseId)
                .putInt(Const.POSITION, position)
                .putLong(Const.PARENT_FOLDER_ID, parentFolderId ?: INVALID_ID)

        fun getCourseFilesBundle(
                fileSubmitObjects: List<Uri>,
                courseId: Long,
                parentFolderId: Long?
        ) = Data.Builder()
                .putStringArray(FILE_PATHS, fileSubmitObjects.map { it.toString() }.toTypedArray())
                .putLong(Const.COURSE_ID, courseId)
                .putLong(Const.PARENT_FOLDER_ID, parentFolderId ?: INVALID_ID)

        fun getAssignmentSubmissionBundle(
                fileSubmitObjects: List<Uri>,
                courseId: Long,
                assignment: Assignment,
                dbSubmissionId: Long? = null,
                additionalAttachmentIds: ArrayList<Long>? = null
        ) = Data.Builder()
                .putStringArray(FILE_PATHS, fileSubmitObjects.map { it.toString() }.toTypedArray())
                .putLong(Const.COURSE_ID, courseId)
                .putLong(Const.ASSIGNMENT_ID, assignment.id)
                .putLong(Const.SUBMISSION_ID, dbSubmissionId ?: INVALID_ID)
                .putLongArray(Const.ATTACHMENTS, additionalAttachmentIds?.toLongArray() ?: longArrayOf())

        fun getMessageBundle(
                fileSubmitObjects: List<Uri>,
                messageText: String,
                conversationId: Long
        ) = Data.Builder()
                .putStringArray(FILE_PATHS, fileSubmitObjects.map { it.toString() }.toTypedArray())
                .putLong(Const.CONVERSATION, conversationId)
                .putString(Const.MESSAGE, messageText)

        fun getNewMessageBundle(
                fileSubmitObjects: List<Uri>,
                userIds: ArrayList<String>,
                subject: String,
                messageText: String,
                isGroup: Boolean,
                contextId: String
        ) = Data.Builder()
                .putStringArray(FILE_PATHS, fileSubmitObjects.map { it.toString() }.toTypedArray())
                .putStringArray(Const.USER_IDS, userIds.toTypedArray())
                .putString(Const.SUBJECT, subject)
                .putString(Const.MESSAGE, messageText)
                .putBoolean(Const.IS_GROUP, isGroup)
                .putString(Const.CONTEXT_ID, contextId)

        fun getSubmissionCommentBundle(
                fileSubmitObjects: List<Uri>,
                courseId: Long,
                assignment: Assignment
        ) = Data.Builder()
                .putStringArray(FILE_PATHS, fileSubmitObjects.map { it.toString() }.toTypedArray())
                .putLong(Const.COURSE_ID, courseId)
                .putLong(Const.ASSIGNMENT_ID, assignment.id)
    }
}
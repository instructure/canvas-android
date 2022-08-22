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

package com.instructure.pandautils.features.file.upload

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FileUploadUtils
import java.io.File

class FileUploadWorker(private val context: Context, private val workerParameters: WorkerParameters) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        val courseId = workerParameters.inputData.getLong(Const.COURSE_ID, INVALID_ID)
        val assignmentId = workerParameters.inputData.getLong(Const.ASSIGNMENT_ID, INVALID_ID)
        val quizQuestionId = workerParameters.inputData.getLong(Const.QUIZ_ANSWER_ID, INVALID_ID)
        val quizId = workerParameters.inputData.getLong(Const.QUIZ, INVALID_ID)
        val position = workerParameters.inputData.getInt(Const.POSITION, INVALID_ID.toInt())
        val parentFolderId = workerParameters.inputData.getLong(Const.PARENT_FOLDER_ID, INVALID_ID)
        val notificationId = notificationId(workerParameters.inputData)
        val submissionId = workerParameters.inputData.getLong(Const.SUBMISSION_ID, INVALID_ID)

        val filePaths = workerParameters.inputData.getStringArray(FILE_PATHS)

        val fileSubmitObjects = filePaths?.map {
            val uri = Uri.parse(it)
            val fileUploadUtilsHelper = FileUploadUtilsHelper(FileUploadUtils, context, context.contentResolver)
            val mimeType = fileUploadUtilsHelper.getFileMimeType(uri)
            val fileName = fileUploadUtilsHelper.getFileNameWithDefault(uri)

            fileUploadUtilsHelper.getFileSubmitObjectFromInputStream(uri, fileName, mimeType)
        }

        Log.d(this::class.java.simpleName, "doWork: ${fileSubmitObjects?.size ?: "null ffs"}")


        return Result.success()
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
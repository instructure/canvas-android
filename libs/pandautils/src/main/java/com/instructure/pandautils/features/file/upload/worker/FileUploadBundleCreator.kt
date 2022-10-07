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

import android.net.Uri
import androidx.work.Data
import com.instructure.canvasapi2.models.Assignment
import com.instructure.pandautils.utils.Const

class FileUploadBundleCreator {

    fun getUserFilesBundle(
            fileSubmitObjects: List<Uri>,
            parentFolderId: Long?
    ) = Data.Builder()
            .putStringArray(FileUploadWorker.FILE_PATHS, fileSubmitObjects.map { it.toString() }.toTypedArray())
            .putLong(Const.PARENT_FOLDER_ID, parentFolderId ?: FileUploadWorker.INVALID_ID)

    fun getQuizFileBundle(
            fileSubmitObjects: List<Uri>,
            parentFolderId: Long?,
            quizQuestionId: Long,
            position: Int,
            courseId: Long,
            quizId: Long
    ) = Data.Builder()
            .putStringArray(FileUploadWorker.FILE_PATHS, fileSubmitObjects.map { it.toString() }.toTypedArray())
            .putLong(Const.QUIZ_ANSWER_ID, quizQuestionId)
            .putLong(Const.QUIZ, quizId)
            .putLong(Const.COURSE_ID, courseId)
            .putInt(Const.POSITION, position)
            .putLong(Const.PARENT_FOLDER_ID, parentFolderId ?: FileUploadWorker.INVALID_ID)

    fun getCourseFilesBundle(
            fileSubmitObjects: List<Uri>,
            courseId: Long,
            parentFolderId: Long?
    ) = Data.Builder()
            .putStringArray(FileUploadWorker.FILE_PATHS, fileSubmitObjects.map { it.toString() }.toTypedArray())
            .putLong(Const.COURSE_ID, courseId)
            .putLong(Const.PARENT_FOLDER_ID, parentFolderId ?: FileUploadWorker.INVALID_ID)

    fun getAssignmentSubmissionBundle(
            fileSubmitObjects: List<Uri>,
            courseId: Long,
            assignment: Assignment,
            dbSubmissionId: Long? = null,
            additionalAttachmentIds: ArrayList<Long>? = null
    ) = Data.Builder()
            .putStringArray(FileUploadWorker.FILE_PATHS, fileSubmitObjects.map { it.toString() }.toTypedArray())
            .putLong(Const.COURSE_ID, courseId)
            .putLong(Const.ASSIGNMENT_ID, assignment.id)
            .putLong(Const.SUBMISSION_ID, dbSubmissionId ?: FileUploadWorker.INVALID_ID)
            .putLongArray(Const.ATTACHMENTS, additionalAttachmentIds?.toLongArray() ?: longArrayOf())

    fun getMessageBundle(
            fileSubmitObjects: List<Uri>,
            messageText: String,
            conversationId: Long
    ) = Data.Builder()
            .putStringArray(FileUploadWorker.FILE_PATHS, fileSubmitObjects.map { it.toString() }.toTypedArray())
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
            .putStringArray(FileUploadWorker.FILE_PATHS, fileSubmitObjects.map { it.toString() }.toTypedArray())
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
            .putStringArray(FileUploadWorker.FILE_PATHS, fileSubmitObjects.map { it.toString() }.toTypedArray())
            .putLong(Const.COURSE_ID, courseId)
            .putLong(Const.ASSIGNMENT_ID, assignment.id)

    fun getTeacherSubmissionCommentBundle(
        fileSubmitObjects: List<Uri>,
        courseId: Long,
        assignmentId: Long,
        userId: Long
    ) = Data.Builder()
        .putStringArray(FileUploadWorker.FILE_PATHS, fileSubmitObjects.map { it.toString() }.toTypedArray())
        .putLong(Const.COURSE_ID, courseId)
        .putLong(Const.ASSIGNMENT_ID, assignmentId)
        .putLong(Const.USER_ID, userId)
        .putString(FileUploadWorker.FILE_SUBMIT_ACTION, FileUploadWorker.ACTION_TEACHER_SUBMISSION_COMMENT)
}
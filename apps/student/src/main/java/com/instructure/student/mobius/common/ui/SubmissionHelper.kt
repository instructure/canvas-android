/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 */
package com.instructure.student.mobius.common.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.FileUtils
import com.instructure.canvasapi2.utils.isValid
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FileUploadUtils
import com.instructure.student.room.StudentDb
import com.instructure.student.room.entities.CreateFileSubmissionEntity
import com.instructure.student.room.entities.CreatePendingSubmissionCommentEntity
import com.instructure.student.room.entities.CreateSubmissionCommentFileEntity
import com.instructure.student.room.entities.CreateSubmissionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.Date

class SubmissionHelper(
    private val context: Context,
    private val studentDb: StudentDb,
    private val apiPrefs: ApiPrefs
) {
    fun startTextSubmission(
        canvasContext: CanvasContext,
        assignmentId: Long,
        assignmentName: String?,
        text: String
    ) {
        val dbSubmissionId = runBlocking {
            insertNewSubmission(assignmentId) {
                val entity = CreateSubmissionEntity(
                    submissionEntry = text,
                    assignmentName = assignmentName,
                    assignmentId = assignmentId,
                    canvasContext = canvasContext,
                    submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString,
                    userId = getUserId(),
                    lastActivityDate = Date(),
                    isDraft = false
                )
                it.submissionDao().insert(entity)
            }
        }

        val bundle = Bundle().apply {
            putLong(Const.SUBMISSION_ID, dbSubmissionId)
        }

        startService(context, SubmissionService.Action.TEXT_ENTRY, bundle)
    }

    suspend fun saveDraft(
        canvasContext: CanvasContext,
        assignmentId: Long,
        assignmentName: String?,
        text: String
    ) {
        insertDraft(assignmentId) {
            val entity = CreateSubmissionEntity(
                assignmentName = assignmentName,
                assignmentId = assignmentId,
                canvasContext = canvasContext,
                submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString,
                userId = getUserId(),
                lastActivityDate = Date(),
                submissionEntry = text,
                isDraft = true
            )
            it.submissionDao().insert(entity)
        }
    }

    fun startUrlSubmission(
        canvasContext: CanvasContext,
        assignmentId: Long,
        assignmentName: String?,
        url: String
    ) {
        val dbSubmissionId = runBlocking {
            insertNewSubmission(assignmentId) {
                val entity = CreateSubmissionEntity(
                    submissionEntry = url,
                    assignmentName = assignmentName,
                    assignmentId = assignmentId,
                    canvasContext = canvasContext,
                    submissionType = Assignment.SubmissionType.ONLINE_URL.apiString,
                    userId = getUserId(),
                    lastActivityDate = Date()
                )
                it.submissionDao().insert(entity)
            }
        }

        val bundle = Bundle().apply {
            putLong(Const.SUBMISSION_ID, dbSubmissionId)
        }

        startService(context, SubmissionService.Action.URL_ENTRY, bundle)
    }

    fun startFileSubmission(
        canvasContext: CanvasContext,
        assignmentId: Long,
        assignmentName: String?,
        assignmentGroupCategoryId: Long = 0,
        files: ArrayList<FileSubmitObject>
    ) {
        files.ifEmpty { return } // No need to upload files if we aren't given any

        val dbSubmissionId = runBlocking {
            insertNewSubmission(assignmentId, files) {
                val entity = CreateSubmissionEntity(
                    assignmentName = assignmentName,
                    assignmentId = assignmentId,
                    assignmentGroupCategoryId = assignmentGroupCategoryId,
                    canvasContext = canvasContext,
                    submissionType = Assignment.SubmissionType.ONLINE_UPLOAD.apiString,
                    userId = getUserId(),
                    lastActivityDate = Date()
                )
                it.submissionDao().insert(entity)
            }
        }

        val bundle = Bundle().apply {
            putLong(Const.SUBMISSION_ID, dbSubmissionId)
        }

        startService(context, SubmissionService.Action.FILE_ENTRY, bundle)
    }

    fun retryFileSubmission(dbSubmissionId: Long) {
        val submission = runBlocking {
            studentDb.submissionDao().findSubmissionById(dbSubmissionId)
        } ?: return

        runBlocking { studentDb.submissionDao().setSubmissionError(false, submission.id) }

        val bundle = Bundle().apply {
            putLong(Const.SUBMISSION_ID, dbSubmissionId)
        }

        startService(
            context,
            if (submission.submissionType == Assignment.SubmissionType.MEDIA_RECORDING.apiString) SubmissionService.Action.MEDIA_ENTRY else SubmissionService.Action.FILE_ENTRY,
            bundle
        )
    }

    fun startMediaSubmission(
        canvasContext: CanvasContext,
        assignmentId: Long,
        assignmentName: String?,
        assignmentGroupCategoryId: Long,
        mediaFilePath: String
    ) {
        val file = File(mediaFilePath).let {
            FileSubmitObject(
                it.name,
                it.length(),
                FileUtils.getMimeType(it.path),
                mediaFilePath
            )
        }
        val dbSubmissionId = runBlocking {
            insertNewSubmission(assignmentId, listOf(file)) {
                val entity = CreateSubmissionEntity(
                    assignmentName = assignmentName,
                    assignmentId = assignmentId,
                    assignmentGroupCategoryId = assignmentGroupCategoryId,
                    canvasContext = canvasContext,
                    submissionType = Assignment.SubmissionType.MEDIA_RECORDING.apiString,
                    userId = getUserId(),
                    lastActivityDate = Date()
                )
                it.submissionDao().insert(entity)
            }
        }

        val bundle = Bundle().apply {
            putLong(Const.SUBMISSION_ID, dbSubmissionId)
        }

        startService(context, SubmissionService.Action.MEDIA_ENTRY, bundle)
    }

    fun startStudioSubmission(
        canvasContext: CanvasContext,
        assignmentId: Long,
        assignmentName: String?,
        url: String
    ) {
        val dbSubmissionId = runBlocking {
            insertNewSubmission(assignmentId) {
                val entity = CreateSubmissionEntity(
                    submissionEntry = url,
                    assignmentName = assignmentName,
                    assignmentId = assignmentId,
                    canvasContext = canvasContext,
                    submissionType = Assignment.SubmissionType.ONLINE_URL.apiString,
                    userId = getUserId(),
                    lastActivityDate = Date()
                )
                it.submissionDao().insert(entity)
            }
        }

        val bundle = Bundle().apply {
            putLong(Const.SUBMISSION_ID, dbSubmissionId)
        }

        startService(context, SubmissionService.Action.STUDIO_ENTRY, bundle)
    }

    fun startCommentUpload(
        canvasContext: CanvasContext,
        assignmentId: Long,
        assignmentName: String,
        message: String?,
        attachments: List<FileSubmitObject>?,
        isGroupMessage: Boolean,
        attemptId: Long?
    ) {
        require(message.isValid() || attachments?.isNotEmpty() == true)
        val entity = CreatePendingSubmissionCommentEntity(
            accountDomain = ApiPrefs.domain,
            canvasContext = canvasContext,
            assignmentName = assignmentName,
            assignmentId = assignmentId,
            lastActivityDate = Date(),
            isGroupMessage = isGroupMessage,
            message = message,
            mediaPath = null,
            attemptId = attemptId
        )
        val rowId = runBlocking { studentDb.pendingSubmissionCommentDao().insert(entity) }
        val commentId = runBlocking { studentDb.pendingSubmissionCommentDao().findIdByRowId(rowId) }
        attachments?.forEach {
            val fileEntity = CreateSubmissionCommentFileEntity(
                pendingCommentId = commentId,
                name = it.name,
                size = it.size,
                contentType = it.contentType,
                fullPath = it.fullPath
            )
            runBlocking { studentDb.submissionCommentFileDao().insert(fileEntity) }
        }

        val bundle = Bundle().apply {
            putLong(Const.ID, commentId)
        }
        startService(context, SubmissionService.Action.COMMENT_ENTRY, bundle)
    }

    fun startMediaCommentUpload(
        canvasContext: CanvasContext,
        assignmentId: Long,
        assignmentName: String,
        mediaFile: File,
        isGroupMessage: Boolean,
        attemptId: Long?
    ) {
        val entity = CreatePendingSubmissionCommentEntity(
            accountDomain = ApiPrefs.domain,
            canvasContext = canvasContext,
            assignmentName = assignmentName,
            assignmentId = assignmentId,
            lastActivityDate = Date(),
            isGroupMessage = isGroupMessage,
            message = null,
            mediaPath = mediaFile.absolutePath,
            attemptId = attemptId
        )
        val rowId = runBlocking { studentDb.pendingSubmissionCommentDao().insert(entity) }
        val commentId = runBlocking { studentDb.pendingSubmissionCommentDao().findIdByRowId(rowId) }

        val bundle = Bundle().apply {
            putLong(Const.ID, commentId)
        }
        startService(context, SubmissionService.Action.COMMENT_ENTRY, bundle)
    }

    fun startStudentAnnotationSubmission(
        canvasContext: CanvasContext,
        assignmentId: Long,
        assignmentName: String?,
        annotatableAttachmentId: Long
    ) {
        val dbSubmissionId = runBlocking {
            insertNewSubmission(assignmentId) {
                val entity = CreateSubmissionEntity(
                    annotatableAttachmentId = annotatableAttachmentId,
                    assignmentName = assignmentName,
                    assignmentId = assignmentId,
                    canvasContext = canvasContext,
                    submissionType = Assignment.SubmissionType.STUDENT_ANNOTATION.apiString,
                    userId = getUserId(),
                    lastActivityDate = Date()
                )
                it.submissionDao().insert(entity)
            }
        }

        val bundle = Bundle().apply {
            putLong(Const.SUBMISSION_ID, dbSubmissionId)
        }

        startService(context, SubmissionService.Action.STUDENT_ANNOTATION, bundle)
    }

    private suspend fun insertNewSubmission(
        assignmentId: Long,
        files: List<FileSubmitObject> = emptyList(),
        insertBlock: suspend (StudentDb) -> Long
    ): Long {
        deleteSubmissionsForAssignment(assignmentId, files)
        val rowId = insertBlock(studentDb)
        val dbSubmissionId = studentDb.submissionDao().findSubmissionByRowId(rowId)?.id ?: return -1

        files.forEach {
            val fileEntity = CreateFileSubmissionEntity(
                dbSubmissionId = dbSubmissionId,
                name = it.name,
                size = it.size,
                contentType = it.contentType,
                fullPath = it.fullPath
            )
            studentDb.fileSubmissionDao().insert(fileEntity)
        }

        return dbSubmissionId
    }

    private suspend fun deleteSubmissionsForAssignment(
        id: Long,
        files: List<FileSubmitObject> = emptyList()
    ) {
        studentDb.submissionDao().findSubmissionsByAssignmentId(id, getUserId())
            .forEach { submission ->
                studentDb.fileSubmissionDao().findFilesForSubmissionId(submission.id)
                    .forEach { file ->
                        // Delete the file for the old submission unless a new file or another database file depends on it (duplicate file being uploaded)
                        if (!files.any { it.fullPath == file.fullPath } && studentDb.fileSubmissionDao()
                                .findFilesForPath(file.id, file.fullPath).isEmpty()) {
                            FileUploadUtils.deleteTempFile(file.fullPath)
                        }
                    }
                studentDb.fileSubmissionDao().deleteFilesForSubmissionId(submission.id)
            }
        studentDb.submissionDao().deleteSubmissionsForAssignmentId(id, getUserId())
    }

    private fun startService(context: Context, action: SubmissionService.Action, extras: Bundle) {
        Intent(context, SubmissionService::class.java).also { intent ->
            intent.action = action.name
            intent.putExtras(extras)
            context.startService(intent)
        }
    }

    private fun getUserId(): Long {
        return apiPrefs.user!!.id
    }

    private suspend fun insertDraft(
        assignmentId: Long,
        insertBlock: suspend (StudentDb) -> Unit
    ): Long {
        deleteDraftsForAssignment(assignmentId)
        insertBlock(studentDb)
        return studentDb.submissionDao().getLastInsert()
    }

    private suspend fun deleteDraftsForAssignment(id: Long) {
        studentDb.submissionDao().deleteDraftById(id, getUserId())
    }

    fun deletePendingComment(commentId: Long) {
        runBlocking {
            studentDb.pendingSubmissionCommentDao().deleteCommentById(commentId)
            studentDb.submissionCommentFileDao().deleteFilesForCommentId(commentId)
        }
    }

    fun retryCommentUpload(commentId: Long) {
        val bundle = Bundle().apply {
            putLong(Const.ID, commentId)
        }
        startService(context, SubmissionService.Action.COMMENT_ENTRY, bundle)
    }


}
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
 */package com.instructure.student.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.submission.SubmissionWorkerAction
import com.instructure.pandautils.room.studentdb.StudentDb
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.daos.CreateFileSubmissionDao
import com.instructure.pandautils.room.studentdb.entities.daos.CreatePendingSubmissionCommentDao
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionCommentFileDao
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import com.instructure.pandautils.utils.Const
import com.instructure.student.mobius.common.ui.SubmissionHelper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class SubmissionHelperTest {

    private val studentDb: StudentDb = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val workManager: WorkManager = mockk(relaxed = true)

    private lateinit var submissionHelper: SubmissionHelper

    @Before
    fun setup() {
        submissionHelper = SubmissionHelper(studentDb, apiPrefs, workManager)

        coEvery {
            studentDb.submissionDao().findSubmissionsByAssignmentId(any(), any())
        } returns emptyList()
    }

    @Test
    fun `Start text submission`() {
        val canvasContext = CanvasContext.defaultCanvasContext()
        val assignmentId = 1L
        val assignmentName = "Assignment"
        val text = "Text"

        val submissionDao = mockk<CreateSubmissionDao>(relaxed = true)
        every { studentDb.submissionDao() } returns submissionDao
        coEvery { submissionDao.insert(any()) } returns 1L

        submissionHelper.startTextSubmission(canvasContext, assignmentId, assignmentName, text)

        coVerify {
            submissionDao.insert(match {
                it.assignmentId == assignmentId && it.assignmentName == assignmentName && it.submissionEntry == text && it.submissionType == "online_text_entry"
            })
        }

        verify {
            workManager.enqueue(withArg<WorkRequest> {
                assert(it.workSpec.input.getString(Const.ACTION) == SubmissionWorkerAction.TEXT_ENTRY.name)
            })
        }
    }

    @Test
    fun `Start URL submission`() {
        val canvasContext = CanvasContext.defaultCanvasContext()
        val assignmentId = 1L
        val assignmentName = "Assignment"
        val url = "https://www.instructure.com"

        val submissionDao = mockk<CreateSubmissionDao>(relaxed = true)
        every { studentDb.submissionDao() } returns submissionDao
        coEvery { submissionDao.insert(any()) } returns 1L

        submissionHelper.startUrlSubmission(canvasContext, assignmentId, assignmentName, url)

        coVerify {
            submissionDao.insert(match {
                it.assignmentId == assignmentId && it.assignmentName == assignmentName && it.submissionEntry == url && it.submissionType == "online_url"
            })
        }

        verify {
            workManager.enqueue(withArg<WorkRequest> {
                assert(it.workSpec.input.getString(Const.ACTION) == SubmissionWorkerAction.URL_ENTRY.name)
            })
        }
    }

    @Test
    fun `Start file submission`() {
        val canvasContext = CanvasContext.defaultCanvasContext()
        val assignmentId = 1L
        val assignmentName = "Assignment"
        val file = mockk<FileSubmitObject>(relaxed = true)

        val submissionDao = mockk<CreateSubmissionDao>(relaxed = true)
        val fileSubmissionDao = mockk<CreateFileSubmissionDao>(relaxed = true)
        every { studentDb.submissionDao() } returns submissionDao
        coEvery { submissionDao.insert(any()) } returns 1L
        every { studentDb.fileSubmissionDao() } returns fileSubmissionDao
        coEvery { fileSubmissionDao.insert(any()) } returns Unit

        submissionHelper.startFileSubmission(
            canvasContext,
            assignmentId,
            assignmentName,
            files = arrayListOf(file)
        )

        coVerify {
            submissionDao.insert(match {
                it.assignmentId == assignmentId && it.assignmentName == assignmentName && it.submissionType == "online_upload"
            })

            fileSubmissionDao.insert(any())
        }

        verify {
            workManager.enqueue(withArg<WorkRequest> {
                assert(it.workSpec.input.getString(Const.ACTION) == SubmissionWorkerAction.FILE_ENTRY.name)
            })
        }
    }

    @Test
    fun `Start media submission`() {
        val canvasContext = CanvasContext.defaultCanvasContext()
        val assignmentId = 1L
        val assignmentName = "Assignment"
        val mediaFilePath = "file://media.mp4"

        val mediaFile = FileSubmitObject(
            "media.mp4",
            1000L,
            "video/mp4",
            mediaFilePath
        )

        val submissionDao = mockk<CreateSubmissionDao>(relaxed = true)
        every { studentDb.submissionDao() } returns submissionDao
        coEvery { submissionDao.insert(any()) } returns 1L

        val fileSubmissionDao = mockk<CreateFileSubmissionDao>(relaxed = true)
        every { studentDb.fileSubmissionDao() } returns fileSubmissionDao
        coEvery { fileSubmissionDao.insert(any()) } returns Unit

        submissionHelper.startMediaSubmission(
            canvasContext,
            assignmentId,
            assignmentName,
            0,
            mediaFile.fullPath
        )

        coVerify {
            submissionDao.insert(match {
                it.assignmentId == assignmentId && it.assignmentName == assignmentName && it.submissionType == "media_recording"
            })

            fileSubmissionDao.insert(match {
                it.fullPath == mediaFilePath
            })
        }

        verify {
            workManager.enqueue(withArg<WorkRequest> {
                assert(it.workSpec.input.getString(Const.ACTION) == SubmissionWorkerAction.MEDIA_ENTRY.name)
            })
        }
    }

    @Test
    fun `Start Studio submission`() {
        val canvasContext = CanvasContext.defaultCanvasContext()
        val assignmentId = 1L
        val assignmentName = "Assignment"
        val url = "https://www.arc.studio.instructure.com"

        val submissionDao = mockk<CreateSubmissionDao>(relaxed = true)
        every { studentDb.submissionDao() } returns submissionDao
        coEvery { submissionDao.insert(any()) } returns 1L

        submissionHelper.startStudioSubmission(canvasContext, assignmentId, assignmentName, url)

        coVerify {
            submissionDao.insert(match {
                it.assignmentId == assignmentId && it.assignmentName == assignmentName && it.submissionEntry == url && it.submissionType == "online_url"
            })
        }

        verify {
            workManager.enqueue(withArg<WorkRequest> {
                assert(it.workSpec.input.getString(Const.ACTION) == SubmissionWorkerAction.STUDIO_ENTRY.name)
            })
        }
    }

    @Test
    fun `Start Student Annotation submission`() {
        val canvasContext = CanvasContext.defaultCanvasContext()
        val assignmentId = 1L
        val assignmentName = "Assignment"

        val submissionDao = mockk<CreateSubmissionDao>(relaxed = true)
        every { studentDb.submissionDao() } returns submissionDao
        coEvery { submissionDao.insert(any()) } returns 1L

        submissionHelper.startStudentAnnotationSubmission(
            canvasContext,
            assignmentId,
            assignmentName,
            8L
        )

        coVerify {
            submissionDao.insert(match {
                it.assignmentId == assignmentId && it.assignmentName == assignmentName && it.submissionType == "student_annotation" && it.annotatableAttachmentId == 8L
            })
        }

        verify {
            workManager.enqueue(withArg<WorkRequest> {
                assert(it.workSpec.input.getString(Const.ACTION) == SubmissionWorkerAction.STUDENT_ANNOTATION.name)
            })
        }
    }

    @Test
    fun `Save draft`() = runTest {
        val canvasContext = CanvasContext.defaultCanvasContext()
        val assignmentId = 1L
        val assignmentName = "Assignment"
        val text = "Text"

        val submissionDao = mockk<CreateSubmissionDao>(relaxed = true)
        every { studentDb.submissionDao() } returns submissionDao
        coEvery { submissionDao.insert(any()) } returns 1L

        submissionHelper.saveDraft(canvasContext, assignmentId, assignmentName, text)

        coVerify {
            submissionDao.insert(match {
                it.assignmentId == assignmentId && it.assignmentName == assignmentName && it.submissionEntry == text && it.submissionType == "online_text_entry" && it.isDraft
            })
        }

        verify(exactly = 0) {
            workManager.enqueue(any<WorkRequest>())
        }
    }

    @Test
    fun `Retry file submission`() {
        val submission = CreateSubmissionEntity(
            assignmentId = 1L,
            canvasContext = CanvasContext.defaultCanvasContext(),
            submissionType = "online_upload",
            userId = 1L
        )
        val submissionDao = mockk<CreateSubmissionDao>(relaxed = true)
        every { studentDb.submissionDao() } returns submissionDao
        coEvery { submissionDao.findSubmissionById(any()) } returns submission

        submissionHelper.retryFileSubmission(
            1L
        )

        coVerify {
            submissionDao.setSubmissionError(false, any())
        }

        verify {
            workManager.enqueue(withArg<WorkRequest> {
                assert(it.workSpec.input.getString(Const.ACTION) == SubmissionWorkerAction.FILE_ENTRY.name)
            })
        }
    }

    @Test
    fun `Start media comment upload`() {
        val canvasContext = CanvasContext.defaultCanvasContext()
        val assignmentId = 1L
        val assignmentName = "Assignment"
        val mediaFilePath = "file://media.mp4"

        val pendingSubmissionCommentDao = mockk<CreatePendingSubmissionCommentDao>(relaxed = true)
        every { studentDb.pendingSubmissionCommentDao() } returns pendingSubmissionCommentDao
        coEvery { pendingSubmissionCommentDao.insert(any()) } returns 1L
        coEvery { pendingSubmissionCommentDao.findIdByRowId(1L) } returns 1L

        val file = mockk<File>(relaxed = true)
        every { file.absolutePath } returns mediaFilePath

        submissionHelper.startMediaCommentUpload(
            canvasContext,
            assignmentId,
            assignmentName,
            file,
            false,
            0
        )

        coVerify {
            pendingSubmissionCommentDao.insert(match {
                it.assignmentId == assignmentId && it.assignmentName == assignmentName && it.mediaPath == mediaFilePath
            })
        }

        verify {
            workManager.enqueue(withArg<WorkRequest> {
                assert(
                    it.workSpec.input.getString(Const.ACTION) == SubmissionWorkerAction.COMMENT_ENTRY.name
                            && it.workSpec.input.getLong(Const.ID, -1) == 1L
                )
            })
        }
    }

    @Test
    fun `Start comment upload without files`() {
        val canvasContext = CanvasContext.defaultCanvasContext()
        val assignmentId = 1L
        val assignmentName = "Assignment"

        val pendingSubmissionCommentDao = mockk<CreatePendingSubmissionCommentDao>(relaxed = true)
        every { studentDb.pendingSubmissionCommentDao() } returns pendingSubmissionCommentDao
        coEvery { pendingSubmissionCommentDao.insert(any()) } returns 1L
        coEvery { pendingSubmissionCommentDao.findIdByRowId(1L) } returns 1L

        submissionHelper.startCommentUpload(
            canvasContext,
            assignmentId,
            assignmentName,
            "Message",
            arrayListOf(),
            false,
            0
        )

        coVerify {
            pendingSubmissionCommentDao.insert(match {
                it.assignmentId == assignmentId && it.assignmentName == assignmentName && it.message == "Message"
            })
        }

        verify {
            workManager.enqueue(withArg<WorkRequest> {
                assert(
                    it.workSpec.input.getString(Const.ACTION) == SubmissionWorkerAction.COMMENT_ENTRY.name
                            && it.workSpec.input.getLong(Const.ID, -1) == 1L
                )
            })
        }
    }

    @Test
    fun `Start comment upload with files`() {
        val canvasContext = CanvasContext.defaultCanvasContext()
        val assignmentId = 1L
        val assignmentName = "Assignment"

        val pendingSubmissionCommentDao = mockk<CreatePendingSubmissionCommentDao>(relaxed = true)
        every { studentDb.pendingSubmissionCommentDao() } returns pendingSubmissionCommentDao
        coEvery { pendingSubmissionCommentDao.insert(any()) } returns 1L
        coEvery { pendingSubmissionCommentDao.findIdByRowId(1L) } returns 1L
        val submissionCommentFileDao = mockk<CreateSubmissionCommentFileDao>(relaxed = true)
        every { studentDb.submissionCommentFileDao() } returns submissionCommentFileDao
        coEvery { submissionCommentFileDao.insert(any()) } returns Unit

        val file = FileSubmitObject(
            "file.txt",
            1000L,
            "text/plain",
            "file://file.txt"
        )

        submissionHelper.startCommentUpload(
            canvasContext,
            assignmentId,
            assignmentName,
            "Message",
            arrayListOf(file),
            false,
            0
        )

        coVerify {
            pendingSubmissionCommentDao.insert(match {
                it.assignmentId == assignmentId && it.assignmentName == assignmentName && it.message == "Message"
            })

            submissionCommentFileDao.insert(match {
                it.fullPath == file.fullPath &&
                        it.pendingCommentId == 1L &&
                        it.name == file.name &&
                        it.size == file.size &&
                        it.contentType == file.contentType
            })
        }

        verify {
            workManager.enqueue(withArg<WorkRequest> {
                assert(
                    it.workSpec.input.getString(Const.ACTION) == SubmissionWorkerAction.COMMENT_ENTRY.name
                            && it.workSpec.input.getLong(Const.ID, -1) == 1L
                )
            })
        }
    }
}
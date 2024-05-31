/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.test

import android.content.Context
import android.content.Intent
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.FileUtils
import com.instructure.pandautils.utils.Const
import com.instructure.student.mobius.common.ui.SubmissionService
import io.mockk.*
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

//@RunWith(AndroidJUnit4::class)
//class SubmissionServiceTest : Assert() {
//
//    private var assignmentId: Long = 0
//    private var assignmentName: String = "Assignment Name"
//    private var mediaFilePath = "some/path/to/a/file.mp4"
//
//    private lateinit var context: Context
//    private lateinit var canvasContext: Course
//
//    @Before
//    fun setup() {
//        assignmentId = 123
//
//        context = spyk(ApplicationProvider.getApplicationContext() as Context)
//        canvasContext = Course()
//
//        mockkObject(ApiPrefs)
//        every { ApiPrefs.user } returns User(id = 22)
//        if (!Db.ready) {
//            Db.dbSetup(AndroidSqliteDriver(Schema, context, callback = object : AndroidSqliteDriver.Callback(Schema) {
//                override fun onOpen(db: SupportSQLiteDatabase) {
//                    super.onOpen(db)
//                    db.execSQL("PRAGMA foreign_keys=ON;")
//                }
//            })) // In-memory database
//        }
//    }
//
//    @After
//    fun cleanup() {
//        Db.dbClear()
//        clearAllMocks()
//    }
//
//
//    @Test
//    fun `startTextSubmission starts the service with an intent`() {
//        val text = "stuff"
//        val intent = slot<Intent>()
//
//        every { context.startService(capture(intent)) } returns null
//
//        SubmissionService.startTextSubmission(context, canvasContext, assignmentId, assignmentName, text)
//
//        assertEquals(SubmissionService.Action.TEXT_ENTRY.name, intent.captured.action)
//        assertTrue(intent.captured.hasExtra(Const.SUBMISSION_ID))
//    }
//
//    @Test
//    fun `startUrlSubmission starts the service with an intent`() {
//        val url = "stuff"
//        val intent = slot<Intent>()
//
//        every { context.startService(capture(intent)) } returns null
//
//        SubmissionService.startUrlSubmission(context, canvasContext, assignmentId, assignmentName, url)
//
//        assertEquals(SubmissionService.Action.URL_ENTRY.name, intent.captured.action)
//        assertTrue(intent.captured.hasExtra(Const.SUBMISSION_ID))
//    }
//
//    @Test
//    fun `startFileSubmission starts the service with an intent`() {
//        val intent = slot<Intent>()
//        val assignmentGroupCategoryId = 0L
//        val files = arrayListOf(FileSubmitObject("", 0L, "", ""))
//
//        every { context.startService(capture(intent)) } returns null
//
//        SubmissionService.startFileSubmission(context, canvasContext, assignmentId, assignmentName, assignmentGroupCategoryId, files)
//
//        assertEquals(SubmissionService.Action.FILE_ENTRY.name, intent.captured.action)
//        assertTrue(intent.captured.hasExtra(Const.SUBMISSION_ID))
//    }
//
//    @Test
//    fun `startFileSubmission does not start the service if no files are given`() {
//        val intent = slot<Intent>()
//        val assignmentGroupCategoryId = 0L
//
//        every { context.startService(capture(intent)) } returns null
//
//        SubmissionService.startFileSubmission(context, canvasContext, assignmentId, assignmentName, assignmentGroupCategoryId, arrayListOf())
//
//        verify(exactly = 0) { context.startService(any()) }
//    }
//
//    @Test
//    fun `startMediaSubmission starts the service with an intent`() {
//        val intent = slot<Intent>()
//        val assignmentGroupCategoryId = 0L
//
//        every { context.startService(capture(intent)) } returns null
//
//        mockkObject(FileUtils)
//        every { FileUtils.getMimeType(any()) } returns "video"
//        SubmissionService.startMediaSubmission(context, canvasContext, assignmentId, assignmentName, assignmentGroupCategoryId, mediaFilePath)
//
//        assertEquals(SubmissionService.Action.MEDIA_ENTRY.name, intent.captured.action)
//        assertTrue(intent.captured.hasExtra(Const.SUBMISSION_ID))
//    }
//
//    @Test
//    fun `startArcSubmission starts the service with an intent`() {
//        val url = "text"
//        val intent = slot<Intent>()
//
//        every { context.startService(capture(intent)) } returns null
//
//        SubmissionService.startStudioSubmission(context, canvasContext, assignmentId, assignmentName, url)
//
//        assertEquals(SubmissionService.Action.STUDIO_ENTRY.name, intent.captured.action)
//        assertTrue(intent.captured.hasExtra(Const.SUBMISSION_ID))
//    }
//
//    @Test
//    fun `Inserts and deletes pending submission comment`() {
//        val fileCount = 3
//        val commentDb = Db.instance.pendingSubmissionCommentQueries
//        val fileDb = Db.instance.submissionCommentFileQueries
//
//        // Insert comment
//        commentDb.insertComment(
//            accountDomain = "www.instructure.com",
//            canvasContext = canvasContext,
//            assignmentName = assignmentName,
//            assignmentId = assignmentId,
//            lastActivityDate = Date.now(),
//            isGroupMessage = false,
//            message = "Test!",
//            mediaPath = null,
//            attemptId = 1
//        )
//
//        // Get inserted comment ID
//        val commentId = commentDb.getLastInsert().executeAsOne()
//
//        // Insert comment files
//        repeat(fileCount) { fileDb.insertFile(commentId, "", 0, "", "") }
//
//        // Assert insertions
//        assertNotNull(commentDb.getCommentById(commentId).executeAsOneOrNull())
//        assertEquals(fileCount, fileDb.getFilesForPendingComment(commentId).executeAsList().size)
//
//        // Delete comment
//        SubmissionService.deletePendingComment(context, commentId)
//
//        // Assert deleted
//        assertNull(commentDb.getCommentById(commentId).executeAsOneOrNull())
//        assertEquals(0, fileDb.getFilesForPendingComment(commentId).executeAsList().size)
//    }
//
//    @Test
//    fun `Student annotation submission starts the service with an intent`() {
//        val annotatableAttachmentId = 123L
//        val intent = slot<Intent>()
//
//        every { context.startService(capture(intent)) } returns null
//
//        SubmissionService.startStudentAnnotationSubmission(context, canvasContext, assignmentId, assignmentName, annotatableAttachmentId)
//
//        assertEquals(SubmissionService.Action.STUDENT_ANNOTATION.name, intent.captured.action)
//        assertTrue(intent.captured.hasExtra(Const.SUBMISSION_ID))
//    }
//}

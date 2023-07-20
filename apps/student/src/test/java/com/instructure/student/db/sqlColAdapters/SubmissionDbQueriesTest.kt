///*
// * Copyright (C) 2019 - present Instructure, Inc.
// *
// *     Licensed under the Apache License, Version 2.0 (the "License");
// *     you may not use this file except in compliance with the License.
// *     You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// *     Unless required by applicable law or agreed to in writing, software
// *     distributed under the License is distributed on an "AS IS" BASIS,
// *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *     See the License for the specific language governing permissions and
// *     limitations under the License.
// */
//package com.instructure.student.db.sqlColAdapters
//
//import android.content.Context
//import androidx.sqlite.db.SupportSQLiteDatabase
//import androidx.test.core.app.ApplicationProvider
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.instructure.canvasapi2.models.CanvasContext
//import com.instructure.student.FileSubmissionQueries
//import com.instructure.student.Submission
//import com.instructure.student.SubmissionQueries
//import com.instructure.student.db.Db
//import com.instructure.student.db.Schema
//import com.squareup.sqldelight.android.AndroidSqliteDriver
//import org.junit.After
//import org.junit.Assert
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.threeten.bp.OffsetDateTime
//
//
//@RunWith(AndroidJUnit4::class)
//class SubmissionDbQueriesTest : Assert() {
//
//    lateinit var db: SubmissionQueries
//    lateinit var courseCanvasContext: CanvasContext
//    lateinit var context: Context
//    val assignmentName = "Assignment"
//    val assignmentId = 1234L
//    val assignmentGroupCategoryId = 4321L
//    val userId = 1357L
//
//    @Before
//    fun setup() {
//        context = ApplicationProvider.getApplicationContext()
//        courseCanvasContext = CanvasContext.emptyCourseContext()
//
//        if (!Db.ready) {
//            Db.dbSetup(AndroidSqliteDriver(Schema, context, callback = object : AndroidSqliteDriver.Callback(Schema) {
//                override fun onOpen(db: SupportSQLiteDatabase) {
//                    super.onOpen(db)
//                    db.execSQL("PRAGMA foreign_keys=ON;")
//                }
//            })) // In-memory database
//        }
//
//        db = Db.instance.submissionQueries
//    }
//
//    @After
//    fun cleanup() {
//        Db.dbClear()
//    }
//
//    @Test
//    fun `Inserting online text submission works correctly`() {
//        val submissionType = "online_text_entry"
//        val (submissionEntry, _, submission) = insertTextSubmission()
//
//        assertEquals(submission.assignmentId, assignmentId)
//        assertEquals(submission.assignmentName, assignmentName)
//        assertEquals(submission.canvasContext, courseCanvasContext)
//        assertEquals(submission.submissionEntry, submissionEntry)
//        assertEquals(submission.submissionType, submissionType)
//    }
//
//    @Test
//    fun `Inserting online url submission works`() {
//        val submissionEntry = "https://www.instructure.com"
//        val submissionType = "online_url"
//
//        db.insertOnlineUrlSubmission(submissionEntry, assignmentName, assignmentId, courseCanvasContext, userId, OffsetDateTime.now())
//        val submissionId = db.getLastInsert().executeAsOne()
//        val submission = db.getSubmissionById(submissionId).executeAsOne()
//
//        assertEquals(submission.assignmentId, assignmentId)
//        assertEquals(submission.assignmentName, assignmentName)
//        assertEquals(submission.canvasContext, courseCanvasContext)
//        assertEquals(submission.submissionEntry, submissionEntry)
//        assertEquals(submission.submissionType, submissionType)
//    }
//
//    @Test
//    fun `Get all submissions works`() {
//        val numberOfSubmissions = 10
//        for (i in 0 until numberOfSubmissions)
//            insertTextSubmission()
//
//        val submissions = db.getAllSubmissions().executeAsList()
//
//        assertEquals(numberOfSubmissions, submissions.size)
//    }
//
//    @Test
//    fun `Getting a submission by id results in correct submission`() {
//        val (_, submissionId, submission) = insertTextSubmission()
//
//        val retrievedSubmission= db.getSubmissionById(submissionId).executeAsOne()
//
//        assertEquals(submission.assignmentId, retrievedSubmission.assignmentId)
//        assertEquals(submission.assignmentName, retrievedSubmission.assignmentName)
//        assertEquals(submission.canvasContext, retrievedSubmission.canvasContext)
//        assertEquals(submission.submissionEntry, retrievedSubmission.submissionEntry)
//        assertEquals(submission.submissionType, retrievedSubmission.submissionType)
//    }
//
//    @Test
//    fun `Deleting a submission id removes the correct submission from the database`() {
//        val (_, submissionId, _) = insertTextSubmission()
//
//        db.deleteSubmissionById(submissionId)
//        val submissions = db.getSubmissionById(submissionId).executeAsOneOrNull()
//
//        assertEquals(submissions, null)
//    }
//
//    @Test
//    fun `Updating a submission with an error works correctly`() {
//        val (_, submissionId, _) = insertTextSubmission()
//        db.setSubmissionError(true, submissionId)
//        val submission = db.getSubmissionById(submissionId).executeAsOne()
//
//        assertEquals(submission.errorFlag, true)
//    }
//
//    @Test
//    fun `Getting the last inserted row id works`() {
//        val(_, submissionId, _) = insertTextSubmission()
//        val lastInsertedRowId = db.getLastInsert().executeAsOne()
//
//        assertEquals(submissionId, lastInsertedRowId)
//    }
//
//    @Test
//    fun `Getting submission for an assignment works`() {
//        val (_, submissionId, _) = insertTextSubmission()
//        val submissions = db.getSubmissionsByAssignmentId(assignmentId, userId).executeAsList()
//
//        assertTrue(submissions.isNotEmpty())
//        assertEquals(submissionId, submissions.last().id)
//    }
//
//    @Test
//    fun `Getting submission for an assignment with a non used user id gives an empty list`() {
//        insertTextSubmission()
//        val submissions = db.getSubmissionsByAssignmentId(assignmentId, userId + 1).executeAsList()
//
//        assertTrue(submissions.isEmpty())
//    }
//
//    @Test
//    fun `Deleting submissions for an assignment works`() {
//        val count = 3
//        repeat(count) {
//            insertTextSubmission()
//        }
//
//        var submissions = db.getSubmissionsByAssignmentId(assignmentId, userId).executeAsList()
//        assertEquals(submissions.size, count)
//
//        db.deleteSubmissionsForAssignmentId(assignmentId, userId)
//        submissions = db.getSubmissionsByAssignmentId(assignmentId, userId).executeAsList()
//        assertTrue(submissions.isEmpty())
//    }
//
//    @Test
//    fun `Deleting a file submission for an assignment cascades delete to file table`() {
//        val fileQuery = Db.instance.fileSubmissionQueries
//        val submissionId = insertFileSubmission(fileQuery)
//
//        var files = fileQuery.getFilesForSubmissionId(submissionId).executeAsList()
//        assertTrue(files.isNotEmpty())
//
//        db.deleteSubmissionsForAssignmentId(assignmentId, userId)
//        files = fileQuery.getFilesForSubmissionId(submissionId).executeAsList()
//        assertTrue(files.isEmpty())
//    }
//
//    @Test
//    fun `Deleting submissions for an assignment won't delete other users submissions`() {
//        val count = 3
//        repeat(count) {
//            insertTextSubmission()
//        }
//
//        db.insertOnlineTextSubmission("Canvas", assignmentName, assignmentId, courseCanvasContext, userId + 1, OffsetDateTime.now(), false)
//
//        var submissions = db.getSubmissionsByAssignmentId(assignmentId, userId).executeAsList()
//        var altSubmissions = db.getSubmissionsByAssignmentId(assignmentId, userId + 1).executeAsList()
//        assertEquals(submissions.size, count)
//        assertEquals(altSubmissions.size, 1)
//
//        db.deleteSubmissionsForAssignmentId(assignmentId, userId)
//        submissions = db.getSubmissionsByAssignmentId(assignmentId, userId).executeAsList()
//        altSubmissions = db.getSubmissionsByAssignmentId(assignmentId, userId + 1).executeAsList()
//        assertTrue(submissions.isEmpty())
//        assertEquals(altSubmissions.size, 1)
//    }
//
//    private fun insertTextSubmission(): Triple<String, Long, Submission> {
//        val submissionEntry = "Canvas"
//
//        db.insertOnlineTextSubmission(submissionEntry, assignmentName, assignmentId, courseCanvasContext, userId, OffsetDateTime.now(), false)
//        val submissionId = db.getLastInsert().executeAsOne()
//        val submission = db.getSubmissionById(submissionId).executeAsOne()
//        return Triple(submissionEntry, submissionId, submission)
//    }
//
//    private fun insertFileSubmission(fileQuery: FileSubmissionQueries): Long {
//        db.insertOnlineUploadSubmission(
//            assignmentName,
//            assignmentId,
//            assignmentGroupCategoryId,
//            courseCanvasContext,
//            userId,
//            OffsetDateTime.now()
//        )
//        val submissionId = db.getLastInsert().executeAsOne()
//
//        fileQuery.insertFile(submissionId, "file", 1, "content", "path")
//
//        return submissionId
//    }
//}

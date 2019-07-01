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
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.models.FileSubmitObject
import com.instructure.pandautils.services.NotoriousUploadService
import com.instructure.pandautils.utils.Const
import com.instructure.student.mobius.common.ui.SubmissionService
import io.mockk.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SubmissionServiceTest : Assert() {

    private var assignmentId: Long = 0
    private var assignmentName: String = "Assignment Name"
    private var mediaFilePath = "/some/path/to/a/file"
    private var notoriousAction = NotoriousUploadService.ACTION.ASSIGNMENT_SUBMISSION

    private lateinit var context: Context
    private lateinit var canvasContext: Course

    @Before
    fun setup() {
        assignmentId = 123

        context = mockk()
        canvasContext = Course()

        every { context.packageName } returns "test"
    }

    @Test
    fun `startTextSubmission starts the service with an intent`() {
        val text = "stuff"
        val intent = slot<Intent>()

        every { context.startService(capture(intent)) } returns null

        SubmissionService.startTextSubmission(context, canvasContext, assignmentId, assignmentName, text)

        assertEquals(SubmissionService.Action.TEXT_ENTRY.name, intent.captured.action)
        assertEquals(canvasContext, intent.captured.getParcelableExtra(Const.CANVAS_CONTEXT))
        assertEquals(assignmentId, intent.captured.getLongExtra(Const.ASSIGNMENT_ID, -1))
        assertEquals(assignmentName, intent.captured.getStringExtra(Const.ASSIGNMENT_NAME))
        assertEquals(text, intent.captured.getStringExtra(Const.MESSAGE))
    }

    @Test
    fun `startUrlSubmission starts the service with an intent`() {
        val url = "stuff"
        val intent = slot<Intent>()

        every { context.startService(capture(intent)) } returns null

        SubmissionService.startUrlSubmission(context, canvasContext, assignmentId, assignmentName, url)

        assertEquals(SubmissionService.Action.URL_ENTRY.name, intent.captured.action)
        assertEquals(canvasContext, intent.captured.getParcelableExtra(Const.CANVAS_CONTEXT))
        assertEquals(assignmentId, intent.captured.getLongExtra(Const.ASSIGNMENT_ID, -1))
        assertEquals(assignmentName, intent.captured.getStringExtra(Const.ASSIGNMENT_NAME))
        assertEquals(url, intent.captured.getStringExtra(Const.URL))
    }

    @Test
    fun `startFileSubmission starts the service with an intent`() {
        val intent = slot<Intent>()
        val assignmentGroupCategoryId = 0L
        val assignment = Assignment(id = assignmentId, name = assignmentName, groupCategoryId = assignmentGroupCategoryId)
        val files = arrayListOf(FileSubmitObject())

        every { context.startService(capture(intent)) } returns null

        SubmissionService.startFileSubmission(context, canvasContext, assignmentId, assignmentName, assignmentGroupCategoryId, files)

        assertEquals(SubmissionService.Action.FILE_ENTRY.name, intent.captured.action)
        assertEquals(canvasContext, intent.captured.getParcelableExtra(Const.CANVAS_CONTEXT))
        assertEquals(assignment, intent.captured.getParcelableExtra(Const.ASSIGNMENT))
        assertEquals(files, intent.captured.getParcelableArrayListExtra<FileSubmitObject>(Const.FILES))
    }

    @Test
    fun `startFileSubmission does not start the service if no files are given`() {
        val intent = slot<Intent>()
        val assignmentGroupCategoryId = 0L

        every { context.startService(capture(intent)) } returns null

        SubmissionService.startFileSubmission(context, canvasContext, assignmentId, assignmentName, assignmentGroupCategoryId, arrayListOf())

        verify(exactly = 0) { context.startService(any()) }
    }

    @Test
    fun `startMediaSubmission starts the service with an intent`() {
        val intent = slot<Intent>()
        val assignmentGroupCategoryId = 0L
        val assignment = Assignment(id = assignmentId, name = assignmentName, groupCategoryId = assignmentGroupCategoryId)

        every { context.startService(capture(intent)) } returns null

        SubmissionService.startMediaSubmission(context, canvasContext, assignment.id, assignment.name, assignment.groupCategoryId, mediaFilePath, notoriousAction)

        assertEquals(SubmissionService.Action.MEDIA_ENTRY.name, intent.captured.action)
        assertEquals(canvasContext, intent.captured.getParcelableExtra(Const.CANVAS_CONTEXT))
        assertEquals(assignment, intent.captured.getParcelableExtra(Const.ASSIGNMENT))
        assertEquals(mediaFilePath, intent.captured.getStringExtra(Const.MEDIA_FILE_PATH))
        assertEquals(notoriousAction, intent.captured.getSerializableExtra(Const.ACTION))
    }

    @Test
    fun `startArcSubmission starts the service with an intent`() {
        val url = "text"
        val intent = slot<Intent>()

        every { context.startService(capture(intent)) } returns null

        SubmissionService.startArcSubmission(context, canvasContext, assignmentId, assignmentName, url)

        assertEquals(SubmissionService.Action.ARC_ENTRY.name, intent.captured.action)
        assertEquals(canvasContext, intent.captured.getParcelableExtra(Const.CANVAS_CONTEXT))
        assertEquals(assignmentId, intent.captured.getLongExtra(Const.ASSIGNMENT_ID, -1))
        assertEquals(assignmentName, intent.captured.getStringExtra(Const.ASSIGNMENT_NAME))
        assertEquals(url, intent.captured.getStringExtra(Const.URL))
    }
}

/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.pandautils.features.speedgrader.content

import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.SubmissionContentQuery
import com.instructure.canvasapi2.models.canvadocs.CanvaDocSessionResponseBody
import com.instructure.canvasapi2.type.SubmissionState
import com.instructure.canvasapi2.type.SubmissionType
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SpeedGraderContentViewModelTest {

    private lateinit var repository: SpeedGraderContentRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: SpeedGraderContentViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val assignmentId = 123L
    private val studentId = 456L
    private val courseId = 789L
    private val groupId = 987L

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        savedStateHandle = SavedStateHandle(
            mapOf(
                SpeedGraderContentViewModel.ASSIGNMENT_ID_KEY to assignmentId,
                SpeedGraderContentViewModel.STUDENT_ID_KEY to studentId
            )
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = SpeedGraderContentViewModel(savedStateHandle, repository)
    }

    @Test
    fun `fetchData updates uiState with TextContent for ONLINE_TEXT_ENTRY submission type`() = runTest {
        val submissionData = mockk<SubmissionContentQuery.Data>()
        val submission = mockk<SubmissionContentQuery.Submission>()
        val assignment = mockk<SubmissionContentQuery.Assignment>()
        val submissionBody = "This is a text submission."

        coEvery { submissionData.submission } returns submission
        coEvery { submission.assignment } returns assignment
        coEvery { assignment.submissionTypes } returns listOf(SubmissionType.online_text_entry)
        coEvery { submission.body } returns submissionBody
        coEvery { submission.groupId } returns null
        coEvery { submission.userId } returns studentId.toString()
        coEvery { submission.submissionType } returns SubmissionType.online_text_entry

        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is TextContent)
        assertEquals("This is a text submission.", (viewModel.uiState.value.content as TextContent).text)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with ExternalToolContent for BASIC_LTI_LAUNCH with empty url when both preview and html urls are null`() = runTest {
        val submissionData = mockk<SubmissionContentQuery.Data>()
        val submission = mockk<SubmissionContentQuery.Submission>()
        val assignment = mockk<SubmissionContentQuery.Assignment>()

        coEvery { submissionData.submission } returns submission
        coEvery { submission.assignment } returns assignment
        coEvery { submission.submissionType } returns SubmissionType.basic_lti_launch
        coEvery { submission.previewUrl } returns null
        coEvery { assignment.htmlUrl } returns null
        coEvery { assignment.submissionTypes } returns listOf(SubmissionType.basic_lti_launch)
        coEvery { submission.groupId } returns null
        coEvery { submission.userId } returns studentId.toString()

        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is ExternalToolContent)
        assertEquals("", (viewModel.uiState.value.content as ExternalToolContent).url)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with ExternalToolContent for BASIC_LTI_LAUNCH with htmlUrl when previewUrl is null`() = runTest {
        val submissionData = mockk<SubmissionContentQuery.Data>()
        val submission = mockk<SubmissionContentQuery.Submission>()
        val assignment = mockk<SubmissionContentQuery.Assignment>()
        val htmlUrl = "https://example.com/html"

        coEvery { submissionData.submission } returns submission
        coEvery { submission.assignment } returns assignment
        coEvery { submission.submissionType } returns SubmissionType.basic_lti_launch
        coEvery { submission.previewUrl } returns null
        coEvery { assignment.submissionTypes } returns listOf(SubmissionType.basic_lti_launch)
        coEvery { assignment.htmlUrl } returns htmlUrl
        coEvery { submission.groupId } returns null
        coEvery { submission.userId } returns studentId.toString()

        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is ExternalToolContent)
        assertEquals(htmlUrl, (viewModel.uiState.value.content as ExternalToolContent).url)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with ExternalToolContent for BASIC_LTI_LAUNCH with previewUrl`() = runTest {
        val submissionData = mockk<SubmissionContentQuery.Data>()
        val submission = mockk<SubmissionContentQuery.Submission>()
        val assignment = mockk<SubmissionContentQuery.Assignment>()
        val previewUrl = "https://example.com/preview"

        coEvery { submissionData.submission } returns submission
        coEvery { submission.assignment } returns assignment
        coEvery { submission.submissionType } returns SubmissionType.basic_lti_launch
        coEvery { submission.previewUrl } returns previewUrl
        coEvery { assignment.submissionTypes } returns listOf(SubmissionType.basic_lti_launch)
        coEvery { assignment.htmlUrl } returns null
        coEvery { submission.groupId } returns null
        coEvery { submission.userId } returns studentId.toString()

        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is ExternalToolContent)
        assertEquals(previewUrl, (viewModel.uiState.value.content as ExternalToolContent).url)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with NoSubmissionContent when submissionType is null`() = runTest {
        val submissionData = mockk<SubmissionContentQuery.Data>()
        val submission = mockk<SubmissionContentQuery.Submission>()

        coEvery { submissionData.submission } returns submission
        coEvery { submission.assignment } returns null
        coEvery { submission.submissionType } returns null
        coEvery { submission.groupId } returns null
        coEvery { submission.userId } returns studentId.toString()

        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is NoSubmissionContent)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with OnPaperContent for ON_PAPER submission type`() = runTest {
        val submissionData = mockk<SubmissionContentQuery.Data>()
        val submission = mockk<SubmissionContentQuery.Submission>()
        val assignment = mockk<SubmissionContentQuery.Assignment>()

        coEvery { submissionData.submission } returns submission
        coEvery { submission.assignment } returns assignment
        coEvery { assignment.submissionTypes } returns listOf(SubmissionType.on_paper)
        coEvery { submission.groupId } returns null
        coEvery { submission.userId } returns studentId.toString()

        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is OnPaperContent)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with NoneContent for NONE submission type`() = runTest {
        val submissionData = mockk<SubmissionContentQuery.Data>()
        val submission = mockk<SubmissionContentQuery.Submission>()
        val assignment = mockk<SubmissionContentQuery.Assignment>()

        coEvery { submissionData.submission } returns submission
        coEvery { submission.assignment } returns assignment
        coEvery { assignment.submissionTypes } returns listOf(SubmissionType.none)
        coEvery { submission.groupId } returns null
        coEvery { submission.userId } returns studentId.toString()

        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is NoneContent)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with UrlContent for ONLINE_URL submission type`() = runTest {
        val submissionData = mockk<SubmissionContentQuery.Data>()
        val submission = mockk<SubmissionContentQuery.Submission>()
        val assignment = mockk<SubmissionContentQuery.Assignment>()
        val url = "https://example.com/submission"

        coEvery { submissionData.submission } returns submission
        coEvery { submission.assignment } returns assignment
        coEvery { submission.submissionType } returns SubmissionType.online_url
        coEvery { submission.url } returns url
        coEvery { assignment.submissionTypes } returns listOf(SubmissionType.online_url)
        coEvery { submission.groupId } returns null
        coEvery { submission.userId } returns studentId.toString()
        coEvery { submission.attachments } returns null

        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is UrlContent)
        assertEquals(url, (viewModel.uiState.value.content as UrlContent).url)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with QuizContent for ONLINE_QUIZ submission type`() = runTest {
        val submissionData = mockk<SubmissionContentQuery.Data>()
        val submission = mockk<SubmissionContentQuery.Submission>()
        val assignment = mockk<SubmissionContentQuery.Assignment>()
        val previewUrl = "https://example.com/quiz"

        coEvery { submissionData.submission } returns submission
        coEvery { submission.assignment } returns assignment
        coEvery { submission.submissionType } returns SubmissionType.online_quiz
        coEvery { submission.previewUrl } returns previewUrl
        coEvery { assignment.courseId } returns courseId.toString()
        coEvery { assignment.submissionTypes } returns listOf(SubmissionType.online_quiz)
        coEvery { submission.groupId } returns null
        coEvery { submission.userId } returns studentId.toString()
        coEvery { assignment.anonymousGrading } returns false
        coEvery { submission.state } returns SubmissionState.submitted

        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is QuizContent)
        val content = viewModel.uiState.value.content as QuizContent
        assertEquals(previewUrl, content.url)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with DiscussionContent for DISCUSSION_TOPIC submission type`() = runTest {
        val submissionData = mockk<SubmissionContentQuery.Data>()
        val submission = mockk<SubmissionContentQuery.Submission>()
        val previewUrl = "https://example.com/discussion"

        coEvery { submissionData.submission } returns submission
        coEvery { submission.assignment } returns null
        coEvery { submission.submissionType } returns SubmissionType.discussion_topic
        coEvery { submission.previewUrl } returns previewUrl
        coEvery { submission.groupId } returns null
        coEvery { submission.userId } returns studentId.toString()

        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is DiscussionContent)
        assertEquals(previewUrl, (viewModel.uiState.value.content as DiscussionContent).previewUrl)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with StudentAnnotationContent for STUDENT_ANNOTATION submission type`() = runTest {
        val submissionData = mockk<SubmissionContentQuery.Data>()
        val submission = mockk<SubmissionContentQuery.Submission>()
        val submissionId = 1234L
        val attempt = 1

        coEvery { submissionData.submission } returns submission
        coEvery { submission.assignment } returns null
        coEvery { submission.submissionType } returns SubmissionType.student_annotation
        coEvery { submission._id } returns submissionId.toString()
        coEvery { submission.attempt } returns attempt
        coEvery { submission.groupId } returns null
        coEvery { submission.userId } returns studentId.toString()

        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        coEvery { repository.createCanvaDocSession(any(), any()) } returns CanvaDocSessionResponseBody(
            canvadocsSessionUrl = "https://example.com/canvadocs"
        )


        createViewModel()

        assert(viewModel.uiState.value.content is PdfContent)
        val content = viewModel.uiState.value.content as PdfContent
        assertEquals("https://example.com/canvadocs", content.url)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with UnsupportedContent for unsupported submission type`() = runTest {
        val submissionData = mockk<SubmissionContentQuery.Data>()
        val submission = mockk<SubmissionContentQuery.Submission>()

        coEvery { submissionData.submission } returns submission
        coEvery { submission.assignment } returns null
        coEvery { submission.submissionType } returns SubmissionType.external_tool
        coEvery { submission.groupId } returns null
        coEvery { submission.userId } returns studentId.toString()

        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is UnsupportedContent)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with AttachmentContent for ONLINE_UPLOAD submission type`() = runTest {
        val submissionData = mockk<SubmissionContentQuery.Data>()
        val submission = mockk<SubmissionContentQuery.Submission>()
        val assignment = mockk<SubmissionContentQuery.Assignment>()
        val attachment = mockk<SubmissionContentQuery.Attachment1>()
        val attachmentUrl = "https://example.com/file.pdf"
        val attachmentThumbnailUrl = "https://example.com/thumbnail.jpg"
        val attachmentDisplayName = "file.pdf"
        val attachmentContentType = "application/pdf"

        coEvery { submissionData.submission } returns submission
        coEvery { submission.assignment } returns assignment
        coEvery { submission.submissionType } returns SubmissionType.online_upload
        coEvery { submission.attachments } returns listOf(attachment)
        coEvery { assignment.submissionTypes } returns listOf(SubmissionType.online_upload)
        coEvery { attachment.url } returns attachmentUrl
        coEvery { attachment.thumbnailUrl } returns attachmentThumbnailUrl
        coEvery { attachment.title } returns attachmentDisplayName
        coEvery { attachment.contentType } returns attachmentContentType
        coEvery { submission.groupId } returns null
        coEvery { submission.userId } returns studentId.toString()
        coEvery { assignment.courseId } returns courseId.toString()
        coEvery { attachment.submissionPreviewUrl } returns attachmentUrl

        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is PdfContent)
        val content = viewModel.uiState.value.content as PdfContent
        assertEquals(attachmentUrl, content.url)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }
}
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
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.features.grades.SubmissionStateLabel
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

    private val submissionData = mockk<SubmissionContentQuery.Data>(relaxed = true)
    private val submission = mockk<SubmissionContentQuery.Submission>(relaxed = true)
    private val assignment = mockk<SubmissionContentQuery.Assignment>(relaxed = true)

    @Before
    fun setup() {
        ContextKeeper.appContext = mockk(relaxed = true)
        Dispatchers.setMain(testDispatcher)

        repository = mockk(relaxed = true)

        savedStateHandle = SavedStateHandle(
            mapOf(
                SpeedGraderContentViewModel.ASSIGNMENT_ID_KEY to assignmentId,
                SpeedGraderContentViewModel.STUDENT_ID_KEY to studentId
            )
        )

        coEvery { submissionData.submission } returns submission
        coEvery { submission.assignment } returns assignment
        coEvery { assignment.courseId } returns courseId.toString()
        coEvery { submission.userId } returns studentId.toString()
        coEvery { submission.groupId } returns null
        coEvery { submission.user } returns SubmissionContentQuery.User(
            name = "Test User",
            avatarUrl = "https://example.com/avatar.png",
            shortName = "TU",
            sortableName = "Test User"
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
        val submissionBody = "This is a text submission."

        coEvery { submission.body } returns submissionBody
        coEvery { submission.submissionType } returns SubmissionType.online_text_entry
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is TextContent)
        assertEquals("This is a text submission.", (viewModel.uiState.value.content as TextContent).text)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with ExternalToolContent for BASIC_LTI_LAUNCH with empty url when both preview and html urls are null`() = runTest {
        coEvery { submission.submissionType } returns SubmissionType.basic_lti_launch
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is ExternalToolContent)
        assertEquals("", (viewModel.uiState.value.content as ExternalToolContent).url)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with ExternalToolContent for BASIC_LTI_LAUNCH with htmlUrl when previewUrl is null`() = runTest {
        val htmlUrl = "https://example.com/html"

        coEvery { submission.submissionType } returns SubmissionType.basic_lti_launch
        coEvery { assignment.htmlUrl } returns htmlUrl
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is ExternalToolContent)
        assertEquals(htmlUrl, (viewModel.uiState.value.content as ExternalToolContent).url)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with ExternalToolContent for BASIC_LTI_LAUNCH with previewUrl`() = runTest {
        val previewUrl = "https://example.com/preview"

        coEvery { submission.submissionType } returns SubmissionType.basic_lti_launch
        coEvery { submission.previewUrl } returns previewUrl
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is ExternalToolContent)
        assertEquals(previewUrl, (viewModel.uiState.value.content as ExternalToolContent).url)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with NoSubmissionContent when submissionType is null`() = runTest {
        coEvery { submission.submissionType } returns null
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is NoSubmissionContent)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with OnPaperContent for ON_PAPER submission type`() = runTest {
        coEvery { assignment.submissionTypes } returns listOf(SubmissionType.on_paper)
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is OnPaperContent)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with NoneContent for NONE submission type`() = runTest {
        coEvery { assignment.submissionTypes } returns listOf(SubmissionType.none)
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is NoneContent)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with UrlContent for ONLINE_URL submission type`() = runTest {
        val url = "https://example.com/submission"

        coEvery { submission.submissionType } returns SubmissionType.online_url
        coEvery { submission.url } returns url
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is UrlContent)
        assertEquals(url, (viewModel.uiState.value.content as UrlContent).url)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with QuizContent for ONLINE_QUIZ submission type`() = runTest {
        val previewUrl = "https://example.com/quiz"

        coEvery { submission.submissionType } returns SubmissionType.online_quiz
        coEvery { submission.previewUrl } returns previewUrl
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is QuizContent)
        val content = viewModel.uiState.value.content as QuizContent
        assertEquals(previewUrl, content.url)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with DiscussionContent for DISCUSSION_TOPIC submission type`() = runTest {
        val previewUrl = "https://example.com/discussion"

        coEvery { submission.submissionType } returns SubmissionType.discussion_topic
        coEvery { submission.previewUrl } returns previewUrl
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is DiscussionContent)
        assertEquals(previewUrl, (viewModel.uiState.value.content as DiscussionContent).previewUrl)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with StudentAnnotationContent for STUDENT_ANNOTATION submission type`() = runTest {
        val submissionId = 1234L
        val attempt = 1

        coEvery { submission.submissionType } returns SubmissionType.student_annotation
        coEvery { submission._id } returns submissionId.toString()
        coEvery { submission.attempt } returns attempt
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
        coEvery { submission.submissionType } returns SubmissionType.external_tool
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is UnsupportedContent)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with AttachmentContent for ONLINE_UPLOAD submission type`() = runTest {
        val url = "https://example.com/file.pdf"

        coEvery { submission.submissionType } returns SubmissionType.online_upload
        coEvery { submission.attachments } returns listOf(mockAttachment(url = url))
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is PdfContent)
        val content = viewModel.uiState.value.content as PdfContent
        assertEquals(url, content.url)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `user data maps correctly`() = runTest {
        coEvery { submission.submissionType } returns SubmissionType.on_paper
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assertEquals("Test User", viewModel.uiState.value.userName)
        assertEquals("https://example.com/avatar.png", viewModel.uiState.value.userUrl)
    }

    @Test
    fun `fetchData updates uiState with SUBMITTED state`() = runTest {
        coEvery { submission.state } returns SubmissionState.submitted
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assertEquals(SubmissionStateLabel.SUBMITTED, viewModel.uiState.value.submissionState)
    }

    @Test
    fun `fetchData updates uiState with NOT_SUBMITTED state`() = runTest {
        coEvery { submission.state } returns SubmissionState.unsubmitted
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assertEquals(SubmissionStateLabel.NOT_SUBMITTED, viewModel.uiState.value.submissionState)
    }

    @Test
    fun `fetchData updates uiState with GRADED state`() = runTest {
        coEvery { submission.state } returns SubmissionState.graded
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assertEquals(SubmissionStateLabel.GRADED, viewModel.uiState.value.submissionState)
    }

    @Test
    fun `fetchData updates uiState with NONE state when submission state is null`() = runTest {
        coEvery { submission.state } returns SubmissionState.UNKNOWN__
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assertEquals(SubmissionStateLabel.NONE, viewModel.uiState.value.submissionState)
    }

    @Test
    fun `fetchData updates uiState with attachments`() = runTest {
        coEvery { submission.attachments } returns listOf(mockAttachment(id = "10"), mockAttachment(id = "20"))
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assertEquals(10L, viewModel.uiState.value.attachmentSelectorUiState.selectedItemId)
        assertEquals(
            listOf(SelectorItem(10, "file_10.pdf"), SelectorItem(20, "file_20.pdf")),
            viewModel.uiState.value.attachmentSelectorUiState.items
        )
    }

    @Test
    fun `Selecting an attachment updates uiState with content and selected attachment id`() = runTest {
        coEvery { submission.submissionType } returns SubmissionType.online_upload
        coEvery { submission.attachments } returns listOf(mockAttachment(id = "10"), mockAttachment(id = "20"))
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        val content = viewModel.uiState.value.content as PdfContent
        assertEquals("https://example.com/file_10.pdf", content.url)
        assertEquals(10L, viewModel.uiState.value.attachmentSelectorUiState.selectedItemId)

        viewModel.uiState.value.attachmentSelectorUiState.onItemSelected(20)

        val newContent = viewModel.uiState.value.content as PdfContent
        assertEquals("https://example.com/file_20.pdf", newContent.url)
        assertEquals(20L, viewModel.uiState.value.attachmentSelectorUiState.selectedItemId)
    }
}

private fun mockAttachment(
    id: String = "1",
    url: String = "https://example.com/file_$id.pdf",
    thumbnailUrl: String = "https://example.com/thumbnail_$id.jpg",
    displayName: String = "file_$id.pdf",
    contentType: String = "application/pdf"
): SubmissionContentQuery.Attachment1 {
    val attachment = mockk<SubmissionContentQuery.Attachment1>(relaxed = true)
    coEvery { attachment._id } returns id
    coEvery { attachment.url } returns url
    coEvery { attachment.thumbnailUrl } returns thumbnailUrl
    coEvery { attachment.displayName } returns displayName
    coEvery { attachment.contentType } returns contentType
    return attachment
}

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

import android.content.res.Resources
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.SubmissionContentQuery
import com.instructure.canvasapi2.fragment.SubmissionFields
import com.instructure.canvasapi2.models.canvadocs.CanvaDocSessionResponseBody
import com.instructure.canvasapi2.type.SubmissionState
import com.instructure.canvasapi2.type.SubmissionType
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandares.R
import com.instructure.pandautils.features.grades.SubmissionStateLabel
import com.instructure.pandautils.features.speedgrader.SpeedGraderSelectedAttemptHolder
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
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
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import java.util.Date

@ExperimentalCoroutinesApi
class SpeedGraderContentViewModelTest {

    private lateinit var repository: SpeedGraderContentRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: SpeedGraderContentViewModel
    private val selectedAttemptHolder = SpeedGraderSelectedAttemptHolder()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val assignmentId = 123L
    private val studentId = 456L
    private val courseId = 789L
    private val groupId = 987L

    private lateinit var submissionFields: SubmissionFields
    private val submissionData = mockk<SubmissionContentQuery.Data>(relaxed = true)
    private val submission = mockk<SubmissionContentQuery.Submission>(relaxed = true)
    private val assignment = mockk<SubmissionFields.Assignment>(relaxed = true)
    private val resources = mockk<Resources>(relaxed = true)

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

        submissionFields = mockSubmissionFields()
        coEvery { assignment.courseId } returns courseId.toString()
        coEvery { submissionData.submission } returns submission
        coEvery { submission.userId } returns studentId.toString()
        coEvery { submission.submissionFields } returns submissionFields
        coEvery { submission.submissionHistoriesConnection } returns mockSubmissionHistory(submissionFields)

        every { resources.getString(R.string.attempt, any()) } answers { "Attempt ${secondArg<Array<Any>>()[0]}" }

        mockkStatic(Uri::class)
        every { Uri.parse(any()) } answers {
            val input = firstArg<String>()
            val mockUri = mockk<Uri>()
            every { mockUri.toString() } returns input
            mockUri
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = SpeedGraderContentViewModel(savedStateHandle, repository, resources, selectedAttemptHolder)
    }

    @Test
    fun `fetchData updates uiState with TextContent for ONLINE_TEXT_ENTRY submission type`() = runTest {
        val submissionBody = "This is a text submission."

        coEvery { submissionFields.body } returns submissionBody
        coEvery { submissionFields.submissionType } returns SubmissionType.online_text_entry
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is TextContent)
        assertEquals("This is a text submission.", (viewModel.uiState.value.content as TextContent).text)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with ExternalToolContent for BASIC_LTI_LAUNCH with empty url when both preview and html urls are null`() =
        runTest {
            coEvery { submissionFields.submissionType } returns SubmissionType.basic_lti_launch
            coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

            createViewModel()

            assert(viewModel.uiState.value.content is ExternalToolContent)
            assertEquals("", (viewModel.uiState.value.content as ExternalToolContent).url)
            assertEquals(studentId, viewModel.uiState.value.assigneeId)
        }

    @Test
    fun `fetchData updates uiState with ExternalToolContent for BASIC_LTI_LAUNCH with htmlUrl when previewUrl is null`() = runTest {
        val htmlUrl = "https://example.com/html"

        coEvery { submissionFields.submissionType } returns SubmissionType.basic_lti_launch
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

        coEvery { submissionFields.submissionType } returns SubmissionType.basic_lti_launch
        coEvery { submissionFields.previewUrl } returns previewUrl
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is ExternalToolContent)
        assertEquals(previewUrl, (viewModel.uiState.value.content as ExternalToolContent).url)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with NoSubmissionContent when submissionType is null`() = runTest {
        coEvery { submissionFields.submissionType } returns null
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

        coEvery { submissionFields.submissionType } returns SubmissionType.online_url
        coEvery { submissionFields.url } returns url
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is UrlContent)
        assertEquals(url, (viewModel.uiState.value.content as UrlContent).url)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with QuizContent for ONLINE_QUIZ submission type`() = runTest {
        val previewUrl = "https://example.com/quiz"

        coEvery { submissionFields.submissionType } returns SubmissionType.online_quiz
        coEvery { submissionFields.previewUrl } returns previewUrl
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

        coEvery { submissionFields.submissionType } returns SubmissionType.discussion_topic
        coEvery { submissionFields.previewUrl } returns previewUrl
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

        coEvery { submissionFields.submissionType } returns SubmissionType.student_annotation
        coEvery { submission._id } returns submissionId.toString()
        coEvery { submissionFields.attempt } returns attempt
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
        coEvery { submissionFields.submissionType } returns SubmissionType.external_tool
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is UnsupportedContent)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with AttachmentContent for ONLINE_UPLOAD submission type`() = runTest {
        val url = "https://example.com/file.pdf"

        coEvery { submissionFields.submissionType } returns SubmissionType.online_upload
        coEvery { submissionFields.attachments } returns listOf(mockAttachment(url = url))
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is PdfContent)
        val content = viewModel.uiState.value.content as PdfContent
        assertEquals(url, content.url)
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `fetchData updates uiState with MediaContent for MEDIA_RECORDING submission type`() = runTest {
        val url = "https://example.com/file.mp4"

        coEvery { submissionFields.submissionType } returns SubmissionType.media_recording
        val mediaSource = mockk<SubmissionFields.MediaSource>(relaxed = true).apply {
            every { this@apply.url } returns url
        }
        coEvery { submissionFields.mediaObject } returns mockk<SubmissionFields.MediaObject>(relaxed = true).apply {
            every { mediaSources } returns listOf(mediaSource)
        }
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assert(viewModel.uiState.value.content is MediaContent)
        val content = viewModel.uiState.value.content as MediaContent
        assertEquals(url, content.uri.toString())
        assertEquals(studentId, viewModel.uiState.value.assigneeId)
    }

    @Test
    fun `user data maps correctly`() = runTest {
        coEvery { submissionFields.submissionType } returns SubmissionType.on_paper
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assertEquals("Test User", viewModel.uiState.value.userName)
        assertEquals("https://example.com/avatar.png", viewModel.uiState.value.userUrl)
    }

    @Test
    fun `fetchData updates uiState with SUBMITTED state`() = runTest {
        coEvery { submissionFields.state } returns SubmissionState.submitted
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assertEquals(SubmissionStateLabel.Submitted, viewModel.uiState.value.submissionState)
    }

    @Test
    fun `fetchData updates uiState with NOT_SUBMITTED state`() = runTest {
        coEvery { submissionFields.state } returns SubmissionState.unsubmitted
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assertEquals(SubmissionStateLabel.NotSubmitted, viewModel.uiState.value.submissionState)
    }

    @Test
    fun `fetchData updates uiState with GRADED state`() = runTest {
        coEvery { submissionFields.state } returns SubmissionState.graded
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assertEquals(SubmissionStateLabel.Graded, viewModel.uiState.value.submissionState)
    }

    @Test
    fun `fetchData updates uiState with NONE state when submission state is null`() = runTest {
        coEvery { submissionFields.state } returns SubmissionState.UNKNOWN__
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assertEquals(SubmissionStateLabel.None, viewModel.uiState.value.submissionState)
    }

    @Test
    fun `fetchData updates uiState with attachments`() = runTest {
        coEvery { submissionFields.attachments } returns listOf(mockAttachment(id = "10"), mockAttachment(id = "20"))
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
        coEvery { submissionFields.submissionType } returns SubmissionType.online_upload
        coEvery { submissionFields.attachments } returns listOf(mockAttachment(id = "10"), mockAttachment(id = "20"))
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

    @Test
    fun `fetchData updates uiState with attempts`() = runTest {
        val attempt1 = mockSubmissionFields(attempt = 1)
        val attempt2 = mockSubmissionFields(attempt = 2)

        val connection = mockSubmissionHistory(attempt1, attempt2)
        coEvery { submission.submissionHistoriesConnection } returns connection
        coEvery { submission.submissionFields } returns attempt2
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assertEquals(2L, viewModel.uiState.value.attemptSelectorUiState.selectedItemId)
        assertEquals(
            listOf(
                SelectorItem(2, "Attempt 2", "Feb 7, 2024, 2:16 AM"),
                SelectorItem(1, "Attempt 1", "Feb 6, 2024, 2:16 AM")
            ),
            viewModel.uiState.value.attemptSelectorUiState.items
        )
    }

    @Test
    fun `Selecting an attempt updates uiState with content, selected attachment and attempt id`() = runTest {
        val attempt1 = mockSubmissionFields(
            attempt = 1,
            submissionType = SubmissionType.online_upload,
            attachments = listOf(mockAttachment(id = "10"), mockAttachment(id = "20"))
        )
        val attempt2 = mockSubmissionFields(
            attempt = 2,
            submissionType = SubmissionType.online_upload,
            attachments = listOf(mockAttachment(id = "30"), mockAttachment(id = "40"))
        )

        val connection = mockSubmissionHistory(attempt1, attempt2)
        coEvery { submission.submissionHistoriesConnection } returns connection
        coEvery { submission.submissionFields } returns attempt2
        coEvery { repository.getSubmission(assignmentId, studentId) } returns submissionData

        createViewModel()

        assertEquals(2L, viewModel.uiState.value.attemptSelectorUiState.selectedItemId)
        val content = viewModel.uiState.value.content as PdfContent
        assertEquals("https://example.com/file_30.pdf", content.url)
        assertEquals(30L, viewModel.uiState.value.attachmentSelectorUiState.selectedItemId)

        viewModel.uiState.value.attemptSelectorUiState.onItemSelected(1)

        assertEquals(1L, viewModel.uiState.value.attemptSelectorUiState.selectedItemId)
        val newContent = viewModel.uiState.value.content as PdfContent
        assertEquals("https://example.com/file_10.pdf", newContent.url)
        assertEquals(10L, viewModel.uiState.value.attachmentSelectorUiState.selectedItemId)
    }

    private fun mockAttachment(
        id: String = "1",
        url: String = "https://example.com/file_$id.pdf",
        thumbnailUrl: String = "https://example.com/thumbnail_$id.jpg",
        displayName: String = "file_$id.pdf",
        contentType: String = "application/pdf"
    ): SubmissionFields.Attachment {
        val attachment = mockk<SubmissionFields.Attachment>(relaxed = true)
        coEvery { attachment._id } returns id
        coEvery { attachment.url } returns url
        coEvery { attachment.thumbnailUrl } returns thumbnailUrl
        coEvery { attachment.displayName } returns displayName
        coEvery { attachment.contentType } returns contentType
        return attachment
    }

    private fun mockSubmissionFields(
        attempt: Int = 1,
        submissionType: SubmissionType = SubmissionType.online_text_entry,
        body: String? = null,
        groupId: String? = null,
        user: SubmissionFields.User? = SubmissionFields.User(
            name = "Test User",
            avatarUrl = "https://example.com/avatar.png",
            shortName = "TU",
            sortableName = "Test User"
        ),
        assignment: SubmissionFields.Assignment? = this.assignment,
        attachments: List<SubmissionFields.Attachment> = emptyList(),
        submittedAt: Date? = Date(
            LocalDateTime
                .of(2024, 2, 5 + attempt, 2, 16)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
    ): SubmissionFields {
        val submissionFields = mockk<SubmissionFields>(relaxed = true)
        coEvery { submissionFields.attempt } returns attempt
        coEvery { submissionFields.submissionType } returns submissionType
        coEvery { submissionFields.body } returns body
        coEvery { submissionFields.groupId } returns groupId
        coEvery { submissionFields.user } returns user
        coEvery { submissionFields.assignment } returns assignment
        coEvery { submissionFields.attachments } returns attachments
        coEvery { submissionFields.submittedAt } returns submittedAt
        return submissionFields
    }

    private fun mockSubmissionHistory(vararg fields: SubmissionFields): SubmissionContentQuery.SubmissionHistoriesConnection {
        val edges = fields.map { field ->
            val node = mockk<SubmissionContentQuery.Node>(relaxed = true).apply {
                coEvery { submissionFields } returns field
            }
            mockk<SubmissionContentQuery.Edge>(relaxed = true).apply {
                coEvery { this@apply.node } returns node
            }
        }
        return mockk<SubmissionContentQuery.SubmissionHistoriesConnection>(relaxed = true).apply {
            coEvery { this@apply.edges } returns edges
        }
    }
}

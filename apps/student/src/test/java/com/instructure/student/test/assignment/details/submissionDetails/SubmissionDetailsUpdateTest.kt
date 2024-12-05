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
 */
package com.instructure.student.test.assignment.details.submissionDetails

import android.net.Uri
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.MediaComment
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsContentType
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsEffect
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsModel
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsUpdate
import com.instructure.student.test.util.matchesEffects
import com.instructure.student.test.util.matchesFirstEffects
import com.instructure.student.util.Const
import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNothing
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.unmockkObject
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File

class SubmissionDetailsUpdateTest : Assert() {

    private val initSpec = InitSpec(SubmissionDetailsUpdate()::init)
    private val updateSpec = UpdateSpec(SubmissionDetailsUpdate()::update)
    private val url = "http://www.google.com"

    private lateinit var course: Course
    private lateinit var assignment: Assignment
    private lateinit var submission: Submission
    private lateinit var initModel: SubmissionDetailsModel
    private lateinit var ltiTool: LTITool
    private var isStudioEnabled = false

    @Before
    fun setup() {
        course = Course()
        assignment = Assignment(id = 1234L, courseId = course.id, name = "Assignment")
        submission = Submission(id = 30L, attempt = 1L, assignmentId = assignment.id)
        initModel = SubmissionDetailsModel(assignmentId = assignment.id, canvasContext = course, isStudioEnabled = isStudioEnabled, assignmentEnhancementsEnabled = true)
        ltiTool = LTITool(url = "https://www.instructure.com")

        mockkStatic(URLUtil::class)
        every { URLUtil.isNetworkUrl(any()) } returns true
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Initializes into a loading state`() {
        val expectedModel = initModel.copy(isLoading = true)
        initSpec
            .whenInit(initModel)
            .then(
                assertThatFirst(
                    FirstMatchers.hasModel(expectedModel),
                    matchesFirstEffects<SubmissionDetailsModel, SubmissionDetailsEffect>(
                        SubmissionDetailsEffect.LoadData(course.id, assignment.id)
                    )
                )
            )
    }

    @Test
    fun `RefreshRequested event produces a loading state and LoadData effect`() {
        val expectedModel = initModel.copy(isLoading = true)
        val expectedEffect = SubmissionDetailsEffect.LoadData(initModel.canvasContext.id, initModel.assignmentId)
        updateSpec.given(initModel)
            .whenEvent(SubmissionDetailsEvent.RefreshRequested)
            .then(
                assertThatNext(
                    hasModel(expectedModel),
                    matchesEffects<SubmissionDetailsModel, SubmissionDetailsEffect>(expectedEffect)
                )
            )
    }

    // region SubmissionClicked event tests

    @Test
    fun `SubmissionClicked event results in model change and a ShowSubmissionContentType effect`() {
        val submission = submission.copy(
            body = "submission body",
            submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString
        )
        initModel = initModel.copy(
            assignmentResult = DataResult.Success(assignment),
            rootSubmissionResult = DataResult.Success(
                Submission(
                    submissionHistory = listOf(
                        Submission(id = 1),
                        Submission(id = 2),
                        submission
                    )
                )
            )
        )
        val contentType = SubmissionDetailsContentType.TextContent(submission.body!!)
        val expectedModel = initModel.copy(
            selectedSubmissionAttempt = submission.attempt
        )

        updateSpec
            .given(initModel)
            .whenEvent(SubmissionDetailsEvent.SubmissionClicked(submission.attempt))
            .then(
                assertThatNext(
                    hasModel(expectedModel),
                    matchesEffects<SubmissionDetailsModel, SubmissionDetailsEffect>(
                        SubmissionDetailsEffect.ShowSubmissionContentType(contentType)
                    )
                )
            )
    }

    @Test
    fun `SubmissionClicked event results in no change if submission is already selected`() {
        val submission = submission.copy(
            body = "submission body",
            submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString
        )
        initModel = initModel.copy(
            selectedSubmissionAttempt = submission.attempt,
            assignmentResult = DataResult.Success(assignment),
            rootSubmissionResult = DataResult.Success(
                Submission(
                    submissionHistory = listOf(
                        Submission(id = 1),
                        Submission(id = 2),
                        submission
                    )
                )
            )
        )

        updateSpec
            .given(initModel)
            .whenEvent(SubmissionDetailsEvent.SubmissionClicked(submission.attempt))
            .then(assertThatNext(NextMatchers.hasNothing()))
    }

    @Test
    fun `SubmissionClicked event with no corresponding submission results in model change and a ShowSubmissionContentType effect of NoSubmissionContent`() {
        val submissionId = 1234L
        val contentType =
            SubmissionDetailsContentType.NoSubmissionContent(course, assignment, isStudioEnabled) // No submission in the model with the selected ID maps to NoSubmissionContent type

        initModel = initModel.copy(assignmentResult = DataResult.Success(assignment))

        val expectedModel = initModel.copy(
                selectedSubmissionAttempt = submissionId,
                assignmentResult = DataResult.Success(assignment)
        )

        updateSpec
            .given(initModel)
            .whenEvent(SubmissionDetailsEvent.SubmissionClicked(submissionId))
            .then(
                assertThatNext(
                    hasModel(expectedModel),
                    matchesEffects<SubmissionDetailsModel, SubmissionDetailsEffect>(
                        SubmissionDetailsEffect.ShowSubmissionContentType(contentType)
                    )
                )
            )
    }

    @Test
    fun `SubmissionAndAttachmentClicked event returns in no change if both submission attempt and attachment Id are unchanged`() {
        val model = initModel.copy(
            selectedSubmissionAttempt = 123L,
            selectedAttachmentId = 456L
        )

        val event = SubmissionDetailsEvent.SubmissionAndAttachmentClicked(123L, Attachment(456L))

        updateSpec
            .given(model)
            .whenEvent(event)
            .then(
                assertThatNext(
                    hasNothing()
                )
            )
    }

    @Test
    fun `SubmissionAndAttachmentClicked event returns in model change and ShowSubmissionContentType effect`() {
        val model = initModel.copy(
            selectedSubmissionAttempt = 123L,
            selectedAttachmentId = 456L
        )

        val expectedModel = initModel.copy(
            selectedSubmissionAttempt = 321L,
            selectedAttachmentId = 654L
        )

        val attachment = Attachment(654L)
        val event = SubmissionDetailsEvent.SubmissionAndAttachmentClicked(321L, Attachment(654L))
        val expectedEffect = SubmissionDetailsEffect.ShowSubmissionContentType(
            SubmissionDetailsContentType.OtherAttachmentContent(attachment)
        )

        updateSpec
            .given(model)
            .whenEvent(event)
            .then(
                assertThatNext<SubmissionDetailsModel, SubmissionDetailsEffect>(
                    hasModel(expectedModel),
                    matchesEffects(expectedEffect)
                )
            )
    }

    // endregion

    // region DataLoaded event tests

    @Test
    fun `DataLoaded event with a failed submission DataResult results in model change and a ShowSubmissionContentType effect of NoSubmissionContent`() {
        initModel = initModel.copy(isLoading = true)
        val assignment = DataResult.Success(assignment)
        val submission = DataResult.Fail(Failure.Network("ErRoR"))
        val ltiTool = DataResult.Fail(Failure.Network("ErRoR"))
        val contentType = SubmissionDetailsContentType.NoSubmissionContent(course, assignment.data, isStudioEnabled)
        val expectedModel = initModel.copy(
            isLoading = false,
            assignmentResult = assignment,
            rootSubmissionResult = submission,
            selectedSubmissionAttempt = null,
            isStudioEnabled = isStudioEnabled
        )
        updateSpec
            .given(initModel)
            .whenEvent(SubmissionDetailsEvent.DataLoaded(assignment, submission, ltiTool, isStudioEnabled, null, null, assignmentEnhancementsEnabled = true))
            .then(
                assertThatNext(
                    hasModel(expectedModel),
                    matchesEffects<SubmissionDetailsModel, SubmissionDetailsEffect>(
                        SubmissionDetailsEffect.ShowSubmissionContentType(contentType)
                    )
                )
            )
    }

    // region getSubmissionContentType tests

    @Test
    fun `NONE results in SubmissionDetailsContentType of NoneContent`() {
        verifyGetSubmissionContentType(
            assignment.copy(submissionTypesRaw = listOf(Assignment.SubmissionType.NONE.apiString)),
            submission,
            SubmissionDetailsContentType.NoneContent
        )
    }

    @Test
    fun `ON_PAPER results in SubmissionDetailsContentType of OnPaperContent`() {
        verifyGetSubmissionContentType(
            assignment.copy(submissionTypesRaw = listOf(Assignment.SubmissionType.ON_PAPER.apiString)),
            submission,
            SubmissionDetailsContentType.OnPaperContent
        )
    }

    @Test
    fun `EXTERNAL_TOOL results in SubmissionDetailsContentType of ExternalToolContent`() {
        verifyGetSubmissionContentType(
            assignment.copy(submissionTypesRaw = listOf(Assignment.SubmissionType.EXTERNAL_TOOL.apiString)),
            submission,
            SubmissionDetailsContentType.ExternalToolContent(course, ltiTool, assignment.name!!),
            ltiTool
        )
    }

    @Test
    fun `EXTERNAL_TOOL with locked assignment results in SubmissionDetailsContentType of LockedContent`() {
        verifyGetSubmissionContentType(
            assignment.copy(
                submissionTypesRaw = listOf(Assignment.SubmissionType.EXTERNAL_TOOL.apiString),
                lockedForUser = true),
            submission,
            SubmissionDetailsContentType.LockedContent,
            ltiTool
        )
    }

    @Test
    fun `ASSIGNMENT_STATE_MISSING results in SubmissionDetailsContentType of NoSubmissionContent`() {
        verifyGetSubmissionContentType(
            assignment,
            submission.copy(attempt = 0),
            SubmissionDetailsContentType.NoSubmissionContent(course, assignment, isStudioEnabled)
        )
    }

    @Test
    fun `ASSIGNMENT_STATE_MISSING with ltiTool results in SubmissionDetailsContentType of NoSubmissionContent`() {
        verifyGetSubmissionContentType(
            assignment,
            submission.copy(attempt = 0),
            SubmissionDetailsContentType.NoSubmissionContent(course, assignment, isStudioEnabled, ltiTool = ltiTool),
            lti = ltiTool
        )
    }

    @Test
    fun `ASSIGNMENT_STATE_GRADED_MISSING results in SubmissionDetailsContentType of NoSubmissionContent`() {
        verifyGetSubmissionContentType(
            assignment,
            submission.copy(missing = true),
            SubmissionDetailsContentType.NoSubmissionContent(course, assignment, isStudioEnabled)
        )
    }

    @Test
    fun `ASSIGNMENT_STATE_GRADED_MISSING and workflow submitted results in SubmissionDetailsContentType of TextContent`() {
        val body = "submission body"
        verifyGetSubmissionContentType(
                assignment,
                submission.copy(missing = true, workflowState = "submitted", body = body, submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString),
                SubmissionDetailsContentType.TextContent(body)
        )
    }

    @Test
    fun `ONLINE_TEXT_ENTRY results in SubmissionDetailsContentType of TextContent`() {
        val body = "submission body"
        verifyGetSubmissionContentType(
            assignment,
            submission.copy(body = body, submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString),
            SubmissionDetailsContentType.TextContent(body)
        )
    }

    @Test
    fun `ONLINE_TEXT_ENTRY with a null body results in SubmissionDetailsContentType of TextContent`() {
        verifyGetSubmissionContentType(
            assignment,
            submission.copy(body = null, submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString),
            SubmissionDetailsContentType.TextContent("")
        )
    }

    @Test
    fun `BASIC_LTI_LAUNCH results in SubmissionDetailsContentType of ExternalToolContent`() {
        verifyGetSubmissionContentType(
            assignment,
            submission.copy(previewUrl = url, submissionType = Assignment.SubmissionType.BASIC_LTI_LAUNCH.apiString),
            SubmissionDetailsContentType.ExternalToolContent(initModel.canvasContext, ltiTool, assignment.name!!),
            lti = ltiTool
        )
    }

    @Test
    fun `MEDIA_RECORDING results in SubmissionDetailsContentType of MediaContent`() {
        val contentType = "jpeg"
        val displayName = "Display Name"

        val uri = mockk<Uri>()
        mockkStatic(Uri::class)
        every { Uri.parse(url) } returns uri

        verifyGetSubmissionContentType(
            assignment,
            submission.copy(
                mediaComment = MediaComment(
                    url = url,
                    contentType = contentType,
                    displayName = displayName
                ),
                submissionType = Assignment.SubmissionType.MEDIA_RECORDING.apiString
            ),
            SubmissionDetailsContentType.MediaContent(Uri.parse(url), contentType, null, displayName)
        )
    }

    @Test
    fun `MEDIA_RECORDING with null content type results in SubmissionDetailsContentType of MediaContent`() {
        val displayName = "Display Name"

        val uri = mockk<Uri>()
        mockkStatic(Uri::class)
        every { Uri.parse(url) } returns uri

        verifyGetSubmissionContentType(
            assignment,
            submission.copy(
                mediaComment = MediaComment(url = url, contentType = null, displayName = displayName),
                submissionType = Assignment.SubmissionType.MEDIA_RECORDING.apiString
            ),
            SubmissionDetailsContentType.MediaContent(uri, "", null, displayName)
        )
    }

    @Test
    fun `MEDIA_RECORDING with null media comment results in SubmissionDetailsContentType of UnsupportedContent`() {
        verifyGetSubmissionContentType(
            assignment,
            submission.copy(mediaComment = null, submissionType = Assignment.SubmissionType.MEDIA_RECORDING.apiString),
            SubmissionDetailsContentType.UnsupportedContent(assignment.id)
        )
    }

    @Test
    fun `ONLINE_URL results in SubmissionDetailsContentType of UrlContent`() {
        verifyGetSubmissionContentType(
            assignment,
            submission.copy(
                url = url,
                attachments = arrayListOf(Attachment(url = url)),
                submissionType = Assignment.SubmissionType.ONLINE_URL.apiString
            ),
            SubmissionDetailsContentType.UrlContent(url, url)
        )
    }

    @Test
    fun `ONLINE_URL with no attachments results in SubmissionDetailsContentType of UrlContent`() {
        verifyGetSubmissionContentType(
            assignment,
            submission.copy(url = url, submissionType = Assignment.SubmissionType.ONLINE_URL.apiString),
            SubmissionDetailsContentType.UrlContent(url, null)
        )
    }

    @Test
    fun `ONLINE_QUIZ results in SubmissionDetailsContentType of QuizContent`() {
        val url = "https://example.com"
        val quizId = 987L
        val attempt = 2L
        mockkObject(ApiPrefs)
        every { ApiPrefs.fullDomain } returns url

        verifyGetSubmissionContentType(
            assignment.copy(quizId = quizId),
            submission.copy(
                submissionType = Assignment.SubmissionType.ONLINE_QUIZ.apiString,
                attempt = attempt
            ),
            SubmissionDetailsContentType.QuizContent(
                url + "/courses/${initModel.canvasContext.id}/quizzes/$quizId/history?version=$attempt&headless=1"
            )
        )
        unmockkObject(ApiPrefs)
    }

    @Test
    fun `DISCUSSION_TOPIC results in SubmissionDetailsContentType of DiscussionContent`() {
        verifyGetSubmissionContentType(
            assignment,
            submission.copy(previewUrl = url, submissionType = Assignment.SubmissionType.DISCUSSION_TOPIC.apiString),
            SubmissionDetailsContentType.DiscussionContent(url)
        )
    }

    @Test
    fun `BROKEN_TYPE results in SubmissionDetailsContentType of UnsupportedContent`() {
        verifyGetSubmissionContentType(
            assignment,
            submission.copy(submissionType = "BROKEN_TYPE"),
            SubmissionDetailsContentType.UnsupportedContent(assignment.id)
        )
    }

    @Test
    fun `ONLINE_UPLOAD with no content type results in SubmissionDetailsContentType of OtherAttachmentContent`() {
        val attachment = Attachment(contentType = null)
        verifyGetSubmissionContentType(
            assignment,
            submission.copy(
                attachments = arrayListOf(attachment),
                submissionType = Assignment.SubmissionType.ONLINE_UPLOAD.apiString
            ),
            SubmissionDetailsContentType.OtherAttachmentContent(attachment)
        )
    }

    @Test
    fun `ONLINE_UPLOAD with wildcard type uses filename for type and results in SubmissionDetailsContentType of OtherAttachmentContent`() {
        val attachment = Attachment(contentType = "*/*", filename = "stuff.apk")

        mockkStatic(MimeTypeMap::class)
        every { MimeTypeMap.getSingleton().getMimeTypeFromExtension(any()) } returns "apk"

        verifyGetSubmissionContentType(
            assignment,
            submission.copy(
                attachments = arrayListOf(attachment),
                submissionType = Assignment.SubmissionType.ONLINE_UPLOAD.apiString
            ),
            SubmissionDetailsContentType.OtherAttachmentContent(attachment)
        )
    }

    @Test
    fun `ONLINE_UPLOAD uses url for type and results in SubmissionDetailsContentType of OtherAttachmentContent`() {
        val attachment = Attachment(contentType = "*/*", filename = null, url = "www.google.com/thing.apk")

        mockkStatic(MimeTypeMap::class)
        every { MimeTypeMap.getSingleton().getMimeTypeFromExtension(any()) } returns null
        every { MimeTypeMap.getFileExtensionFromUrl(any()) } returns "apk"

        verifyGetSubmissionContentType(
            assignment,
            submission.copy(
                attachments = arrayListOf(attachment),
                submissionType = Assignment.SubmissionType.ONLINE_UPLOAD.apiString
            ),
            SubmissionDetailsContentType.OtherAttachmentContent(attachment)
        )
    }

    @Test
    fun `ONLINE_UPLOAD with wildcard type results in SubmissionDetailsContentType of OtherAttachmentContent`() {
        val attachment = Attachment(contentType = "*/*", filename = null, url = null)

        mockkStatic(MimeTypeMap::class)
        every { MimeTypeMap.getSingleton().getMimeTypeFromExtension(any()) } returns null
        every { MimeTypeMap.getFileExtensionFromUrl(any()) } returns null

        verifyGetSubmissionContentType(
            assignment,
            submission.copy(
                attachments = arrayListOf(attachment),
                submissionType = Assignment.SubmissionType.ONLINE_UPLOAD.apiString
            ),
            SubmissionDetailsContentType.OtherAttachmentContent(attachment)
        )
    }

    @Test
    fun `ONLINE_UPLOAD with canvadoc preview url results in SubmissionDetailsContentType of PdfContent`() {
        val attachment =
            Attachment(contentType = "can be anything, just not null", previewUrl = url + "/" + Const.CANVADOC)

        verifyGetSubmissionContentType(
            assignment,
            submission.copy(
                attachments = arrayListOf(attachment),
                submissionType = Assignment.SubmissionType.ONLINE_UPLOAD.apiString
            ),
            SubmissionDetailsContentType.PdfContent(attachment.previewUrl!!)
        )
    }

    @Test
    fun `ONLINE_UPLOAD with pdf type and canvadoc preview url results in SubmissionDetailsContentType of PdfContent`() {
        val attachment = Attachment(contentType = "application/pdf", previewUrl = url + "/" + Const.CANVADOC)

        verifyGetSubmissionContentType(
            assignment,
            submission.copy(
                attachments = arrayListOf(attachment),
                submissionType = Assignment.SubmissionType.ONLINE_UPLOAD.apiString
            ),
            SubmissionDetailsContentType.PdfContent(attachment.previewUrl!!)
        )
    }

    @Test
    fun `ONLINE_UPLOAD with pdf type and url results in SubmissionDetailsContentType of PdfContent`() {
        val attachment = Attachment(contentType = "application/pdf", url = url)

        verifyGetSubmissionContentType(
            assignment,
            submission.copy(
                attachments = arrayListOf(attachment),
                submissionType = Assignment.SubmissionType.ONLINE_UPLOAD.apiString
            ),
            SubmissionDetailsContentType.PdfContent(attachment.url!!)
        )
    }

    @Test
    fun `ONLINE_UPLOAD with pdf type and no url results in SubmissionDetailsContentType of PdfContent`() {
        val attachment = Attachment(contentType = "application/pdf", url = null)

        verifyGetSubmissionContentType(
            assignment,
            submission.copy(
                attachments = arrayListOf(attachment),
                submissionType = Assignment.SubmissionType.ONLINE_UPLOAD.apiString
            ),
            SubmissionDetailsContentType.PdfContent("")
        )
    }

    @Test
    fun `ONLINE_UPLOAD with audio type results in SubmissionDetailsContentType of MediaContent`() {
        val attachment = Attachment(contentType = "audio", url = url)

        val uri = mockk<Uri>()
        mockkStatic(Uri::class)
        every { Uri.parse(url) } returns uri

        verifyGetSubmissionContentType(
            assignment,
            submission.copy(
                attachments = arrayListOf(attachment),
                submissionType = Assignment.SubmissionType.ONLINE_UPLOAD.apiString
            ),
            SubmissionDetailsContentType.MediaContent(
                uri,
                attachment.contentType,
                attachment.thumbnailUrl,
                attachment.displayName
            )
        )
    }

    @Test
    fun `ONLINE_UPLOAD with video type results in SubmissionDetailsContentType of MediaContent`() {
        val attachment = Attachment(contentType = "video", url = url)

        val uri = mockk<Uri>()
        mockkStatic(Uri::class)
        every { Uri.parse(url) } returns uri

        verifyGetSubmissionContentType(
            assignment,
            submission.copy(
                attachments = arrayListOf(attachment),
                submissionType = Assignment.SubmissionType.ONLINE_UPLOAD.apiString
            ),
            SubmissionDetailsContentType.MediaContent(
                uri,
                attachment.contentType,
                attachment.thumbnailUrl,
                attachment.displayName
            )
        )
    }

    @Test
    fun `ONLINE_UPLOAD with image type results in SubmissionDetailsContentType of ImageContent`() {
        val attachment = Attachment(contentType = "image", url = url, displayName = "Image.jpg")

        verifyGetSubmissionContentType(
            assignment,
            submission.copy(
                attachments = arrayListOf(attachment),
                submissionType = Assignment.SubmissionType.ONLINE_UPLOAD.apiString
            ),
            SubmissionDetailsContentType.ImageContent("Image.jpg", url, attachment.contentType!!)
        )
    }

    @Test
    fun `ONLINE_UPLOAD with image type and no url results in SubmissionDetailsContentType of ImageContent`() {
        val attachment = Attachment(contentType = "image", url = null, displayName = "Image.jpg")

        verifyGetSubmissionContentType(
            assignment,
            submission.copy(
                attachments = arrayListOf(attachment),
                submissionType = Assignment.SubmissionType.ONLINE_UPLOAD.apiString
            ),
            SubmissionDetailsContentType.ImageContent("Image.jpg", "", attachment.contentType!!)
        )
    }

    private fun verifyGetSubmissionContentType(
        assignment: Assignment,
        submission: Submission,
        expectedContentType: SubmissionDetailsContentType,
        lti: LTITool? = null,
        quiz: Quiz? = null
    ) {
        val assignmentResult = DataResult.Success(assignment)
        val submissionResult = DataResult.Success(submission)
        val ltiToolResult = if (lti == null) null else DataResult.Success(lti)
        val expectedModel = initModel.copy(
            isLoading = false,
            assignmentResult = assignmentResult,
            rootSubmissionResult = submissionResult,
            selectedSubmissionAttempt = submission.attempt
        )

        updateSpec
            .given(initModel)
            .whenEvent(
                SubmissionDetailsEvent.DataLoaded(
                    assignmentResult,
                    submissionResult,
                    ltiToolResult,
                    isStudioEnabled,
                    null,
                    null,
                    assignmentEnhancementsEnabled = true
                )
            )
            .then(
                assertThatNext(
                    hasModel(expectedModel),
                    matchesEffects<SubmissionDetailsModel, SubmissionDetailsEffect>(
                        SubmissionDetailsEffect.ShowSubmissionContentType(expectedContentType)
                    )
                )
            )
    }
    // endregion getSubmissionContentType

    // endregion DataLoaded

    // region FloatingRecordingView Tests

    @Test
    fun `AudioRecordingClicked results in ShowAudioRecordingView effect`() {
        updateSpec
            .given(initModel)
            .whenEvent(SubmissionDetailsEvent.AudioRecordingClicked)
            .then(
                assertThatNext(
                    matchesEffects<SubmissionDetailsModel, SubmissionDetailsEffect>(
                        SubmissionDetailsEffect.ShowAudioRecordingView
                    )
                )
            )
    }

    @Test
    fun `VideoRecordingClicked results in ShowVideoRecordingView effect`() {
        updateSpec
            .given(initModel)
            .whenEvent(SubmissionDetailsEvent.VideoRecordingClicked)
            .then(
                assertThatNext(
                    matchesEffects<SubmissionDetailsModel, SubmissionDetailsEffect>(
                        SubmissionDetailsEffect.ShowVideoRecordingView
                    )
                )
            )
    }

    @Test
    fun `VideoRecordingReplayClicked with null file results in ShowVideoRecordingPlaybackError effect`() {
        updateSpec
            .given(initModel)
            .whenEvent(SubmissionDetailsEvent.VideoRecordingReplayClicked(null))
            .then(
                assertThatNext(
                    matchesEffects<SubmissionDetailsModel, SubmissionDetailsEffect>(
                        SubmissionDetailsEffect.ShowVideoRecordingPlaybackError
                    )
                )
            )
    }

    @Test
    fun `VideoRecordingReplayClicked with file results in ShowVideoRecordingPlayback effect`() {
        val file = File("test")
        updateSpec
            .given(initModel)
            .whenEvent(SubmissionDetailsEvent.VideoRecordingReplayClicked(file))
            .then(
                assertThatNext(
                    matchesEffects<SubmissionDetailsModel, SubmissionDetailsEffect>(
                        SubmissionDetailsEffect.ShowVideoRecordingPlayback(file)
                    )
                )
            )
    }

    @Test
    fun `StopMediaRecordingClicked results in MediaCommentDialogClosed effect`() {
        updateSpec
            .given(initModel)
            .whenEvent(SubmissionDetailsEvent.StopMediaRecordingClicked)
            .then(
                assertThatNext(
                    matchesEffects<SubmissionDetailsModel, SubmissionDetailsEffect>(
                        SubmissionDetailsEffect.MediaCommentDialogClosed
                    )
                )
            )
    }

    @Test
    fun `SendMediaCommentClicked with null file results in ShowMediaCommentError effect`() {
        updateSpec
            .given(initModel)
            .whenEvent(SubmissionDetailsEvent.SendMediaCommentClicked(null))
            .then(
                assertThatNext(
                    matchesEffects<SubmissionDetailsModel, SubmissionDetailsEffect>(
                        SubmissionDetailsEffect.ShowMediaCommentError
                    )
                )
            )
    }

    @Test
    fun `SendMediaCommentClicked with file results in UploadMediaComment effect`() {
        val file = File("test")
        updateSpec
            .given(initModel)
            .whenEvent(SubmissionDetailsEvent.SendMediaCommentClicked(file))
            .then(
                assertThatNext(
                    matchesEffects<SubmissionDetailsModel, SubmissionDetailsEffect>(
                        SubmissionDetailsEffect.UploadMediaComment(file)
                    )
                )
            )
    }
    // endregion FloatingRecordingView
}

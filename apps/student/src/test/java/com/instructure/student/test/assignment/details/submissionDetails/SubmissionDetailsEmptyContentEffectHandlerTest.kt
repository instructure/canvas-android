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

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.FilePrefs
import com.instructure.pandautils.utils.FileUploadUtils
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.requestPermissions
import com.instructure.student.mobius.assignmentDetails.chooseMediaIntent
import com.instructure.student.mobius.assignmentDetails.getVideoIntent
import com.instructure.student.mobius.assignmentDetails.isIntentAvailable
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentEffect
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentEffectHandler
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionDetailsEmptyContentFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionDetailsEmptyContentView
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionTypesVisibilities
import com.instructure.student.mobius.common.ui.SubmissionHelper
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.concurrent.Executors


class SubmissionDetailsEmptyContentEffectHandlerTest : Assert() {
    private val assignmentId = 2468L
    private val view: SubmissionDetailsEmptyContentView = mockk(relaxed = true)
    private val context: FragmentActivity = mockk(relaxed = true)
    private val submissionHelper: SubmissionHelper = mockk(relaxed = true)
    private val effectHandler = SubmissionDetailsEmptyContentEffectHandler(context, assignmentId, submissionHelper).apply { view = this@SubmissionDetailsEmptyContentEffectHandlerTest.view }
    private val eventConsumer: Consumer<SubmissionDetailsEmptyContentEvent> = mockk(relaxed = true)
    private lateinit var connection: Connection<SubmissionDetailsEmptyContentEffect>

    lateinit var assignment: Assignment
    lateinit var course: Course
    private var userId: Long = 0
    val uri = mockk<Uri>(relaxed = true)

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
        course = Course(id = 1234L)
        assignment = Assignment(id = 2468, courseId = course.id, name = "Instructure")
        userId = 6789L

        mockkObject(ApiPrefs)
        every { ApiPrefs.user } returns User(id = userId)

        val intent = mockk<Intent>()
        every { intent.action } returns ""
        every { intent.addFlags(any()) } returns intent
        every { intent.putExtra(MediaStore.EXTRA_OUTPUT, uri) } returns intent

        connection = effectHandler.connect(eventConsumer)
    }

    @Test
    fun `ShowVideoRecordingView event with permission results in launching intent`() {
        mockPermissions(true)
        testVideo()
    }

    @Test
    fun `ShowVideoRecordingView event without permission will request permission and show an error message when denied`() {
        mockPermissions(false, permissionGranted = false)

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowVideoRecordingView)

        verify(timeout = 100) {
            view.showPermissionDeniedToast()
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowVideoRecordingView event without permission will request permission and results in launching intent`() {
        mockPermissions(false, permissionGranted = true)
        testVideo()
    }

    @Test
    fun `ShowAudioRecordingView event with permission results in view calling showAudioRecordingView`() {
        mockPermissions(hasPermission = true)

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowAudioRecordingView)

        verify(timeout = 100) {
            view.showAudioRecordingView()
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowAudioRecordingView event without permission results in view calling showPermissionDeniedToast`() {
        mockPermissions(hasPermission = false)

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowAudioRecordingView)

        verify(timeout = 100) {
            view.showPermissionDeniedToast()
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowMediaPickerView event results in startActivityForResult with choose media request code`() {
        testMediaPicker()
    }

    @Test
    fun `ShowVideoRecordingError event calls showVideoRecordingError() on view`() {
        connection.accept(SubmissionDetailsEmptyContentEffect.ShowVideoRecordingError)

        verify(timeout = 100) {
            view.showVideoRecordingError()
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowAudioRecordingError event calls showAudioRecordingError() on view`() {
        connection.accept(SubmissionDetailsEmptyContentEffect.ShowAudioRecordingError)

        verify(timeout = 100) {
            view.showAudioRecordingError()
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowMediaPickingError event calls showMediaPickingError() on view`() {
        connection.accept(SubmissionDetailsEmptyContentEffect.ShowMediaPickingError)

        verify(timeout = 100) {
            view.showMediaPickingError()
        }

        confirmVerified(view)
    }

    @Test
    fun `UploadVideoSubmission event calls launchFilePickerView() on view`() {
        connection.accept(SubmissionDetailsEmptyContentEffect.UploadVideoSubmission(uri, course, assignment))

        verify(timeout = 100) {
            view.launchFilePickerView(uri, course, assignment)
        }

        confirmVerified(view)
    }

    @Test
    fun `UploadMediaFileSubmission event calls launchFilePickerView() on view`() {
        connection.accept(SubmissionDetailsEmptyContentEffect.UploadMediaFileSubmission(uri, course, assignment))

        verify(timeout = 100) {
            view.launchFilePickerView(uri, course, assignment)
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowSubmitDialogView event calls showSubmitDialogView() on view`() {
        connection.accept(SubmissionDetailsEmptyContentEffect.ShowSubmitDialogView(assignment, course, false))

        verify(timeout = 100) {
            view.showSubmitDialogView(assignment,
                SubmissionTypesVisibilities()
            )
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowQuizStartView event calls showQuizStartView() on view`() {
        val quiz = Quiz(id = 123L)

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowQuizStartView(quiz, course))

        verify(timeout = 100) {
            view.showQuizStartView(course, quiz)
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowDiscussionDetailView event calls showDiscussionDetailView() on view`() {
        val discussionTopicHeaderId = 112233L

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowDiscussionDetailView(discussionTopicHeaderId, course))

        verify(timeout = 100) {
            view.showDiscussionDetailView(course, discussionTopicHeaderId)
        }

        confirmVerified(view)
    }

    @Test
    fun `UploadAudioSubmission event calls uploadAudioRecording`() {
        val file: File = mockk()
        every { file.path } returns "Path"

        every {
            submissionHelper.startMediaSubmission(
                course,
                assignment.id,
                assignment.name,
                assignment.groupCategoryId,
                "Path",
                mediaSource = "audio_recorder"
            )
        } returns 1L


        connection.accept(SubmissionDetailsEmptyContentEffect.UploadAudioSubmission(file, course, assignment))
        verify(timeout = 100) {
            submissionHelper.startMediaSubmission(
                course,
                assignment.id,
                assignment.name,
                assignment.groupCategoryId,
                "Path",
                mediaSource = "audio_recorder"
            )
        }
    }

    @Test
    fun `ShowCreateSubmissionView event with ONLINE_QUIZ submissionType calls showQuizOrDiscussionView`() {
        val quizId = 1234L
        val domain = "mobiledev.instructure.com/api/v1"
        val protocol = "https"
        val submissionType = Assignment.SubmissionType.ONLINE_QUIZ
        val assignment = assignment.copy(quizId = quizId)

        mockkObject(ApiPrefs)
        every { ApiPrefs.protocol } returns protocol
        every { ApiPrefs.domain } returns domain

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowCreateSubmissionView(submissionType, course, assignment))


        val url = "$protocol://$domain/courses/${course.id}/quizzes/$quizId"

        verify(timeout = 100) {
            view.showQuizOrDiscussionView(url)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView event with DISCUSSION_TOPIC submissionType calls showQuizOrDiscussionView`() {
        val discussionTopicId = 1234L
        val domain = "mobiledev.instructure.com/api/v1"
        val protocol = "https"
        val submissionType = Assignment.SubmissionType.DISCUSSION_TOPIC
        val assignment = assignment.copy(discussionTopicHeader = DiscussionTopicHeader(id = discussionTopicId))

        mockkObject(ApiPrefs)
        every { ApiPrefs.protocol } returns protocol
        every { ApiPrefs.domain } returns domain

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowCreateSubmissionView(submissionType, course, assignment))


        val url = "$protocol://$domain/courses/${course.id}/discussion_topics/$discussionTopicId"

        verify(timeout = 100) {
            view.showQuizOrDiscussionView(url)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView event with ONLINE_UPLOAD submissionType calls showFileUploadView`() {
        val submissionType = Assignment.SubmissionType.ONLINE_UPLOAD

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowCreateSubmissionView(submissionType, course, assignment))

        verify(timeout = 100) {
            view.showFileUploadView(assignment)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView event with ONLINE_TEXT_ENTRY submissionType calls showOnlineTextEntryView`() {
        val submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowCreateSubmissionView(submissionType, course, assignment))

        verify(timeout = 100) {
            view.showOnlineTextEntryView(assignment.id, assignment.name)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView event with ONLINE_URL submissionType calls showOnlineUrlEntryView`() {
        val submissionType = Assignment.SubmissionType.ONLINE_URL

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowCreateSubmissionView(submissionType, course, assignment))

        verify(timeout = 100) {
            view.showOnlineUrlEntryView(assignment.id, assignment.name, course)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView with student annotation submissionType shows student annotation view`() {
        val submissionType = Assignment.SubmissionType.STUDENT_ANNOTATION
        val assignmentWithStudentAnnotation = assignment.copy(annotatableAttachmentId = 123L)

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowCreateSubmissionView(submissionType, course, assignmentWithStudentAnnotation))

        verify(timeout = 100) {
            view.showStudentAnnotationView(assignmentWithStudentAnnotation)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView event with EXTERNAL_TOOL submissionType calls showLTIView() on the view`() {
        val submissionType = Assignment.SubmissionType.EXTERNAL_TOOL
        val ltiUrl = "https://www.instructure.com"
        val ltiTool = LTITool(id = 1L, url = ltiUrl)

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowCreateSubmissionView(
            submissionType = submissionType,
            course = course,
            assignment = assignment,
            ltiTool = ltiTool))

        verify(timeout = 100) {
            view.showLTIView(course, assignment.name!!, ltiTool)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView event with BASIC_LTI_LAUNCH submissionType calls showLTIView() on the view`() {
        val submissionType = Assignment.SubmissionType.BASIC_LTI_LAUNCH
        val ltiUrl = "https://www.instructure.com"
        val ltiTool = LTITool(id = 1L, url = ltiUrl)

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowCreateSubmissionView(
            submissionType = submissionType,
            course = course,
            assignment = assignment,
            ltiTool = ltiTool))

        verify(timeout = 100) {
            view.showLTIView(course, assignment.name!!, ltiTool)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView event with MEDIA_RECORDING submissionType calls showMediaRecordingView() on the view`() {
        val submissionType = Assignment.SubmissionType.MEDIA_RECORDING

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowCreateSubmissionView(submissionType, course, assignment))

        verify(timeout = 100) {
            view.showMediaRecordingView()
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowSubmitAssignmentView event calls ShowSubmitDialogView on the view`() {
        connection.accept(SubmissionDetailsEmptyContentEffect.ShowSubmitDialogView(assignment, course, false))

        verify(timeout = 100) {
            view.showSubmitDialogView(assignment,
                SubmissionTypesVisibilities()
            )
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowSubmitDialogView event calls showSubmitDialogView() with fileUpload|studioUpload == true when submission type is ONLINE_UPLOAD`() {
        val course = Course()

        val assignment = assignment.copy(submissionTypesRaw = listOf("online_upload"))
        connection.accept(SubmissionDetailsEmptyContentEffect.ShowSubmitDialogView(assignment, course, true))

        verify(timeout = 100) {
            view.showSubmitDialogView(
                assignment,
                SubmissionTypesVisibilities(
                    fileUpload = true,
                    studioUpload = true
                )
            )
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowSubmitDialogView event calls showSubmitDialogView() with fileUpload == true when submission type is ONLINE_UPLOAD`() {
        val course = Course()
        val assignment = assignment.copy(
                submissionTypesRaw = listOf("online_upload")
        )

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowSubmitDialogView(assignment, course, false))

        verify(timeout = 100) {
            view.showSubmitDialogView(assignment,
                SubmissionTypesVisibilities(
                    fileUpload = true
                )
            )
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowSubmitDialogView event calls showSubmitDialogView() with textEntry == true when submission type is ONLINE_TEXT_ENTRY`() {
        val assignment = assignment.copy(
                submissionTypesRaw = listOf("online_text_entry")
        )

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowSubmitDialogView(assignment, course, false))

        verify(timeout = 100) {
            view.showSubmitDialogView(assignment,
                SubmissionTypesVisibilities(
                    textEntry = true
                )
            )
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowSubmitDialogView event calls showSubmitDialogView() with urlEntry == true when submission type is ONLINE_URL`() {
        val assignment = assignment.copy(
                submissionTypesRaw = listOf("online_url")
        )

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowSubmitDialogView(assignment, course, false))

        verify(timeout = 100) {
            view.showSubmitDialogView(assignment,
                SubmissionTypesVisibilities(
                    urlEntry = true
                )
            )
        }

        confirmVerified(view)
    }

    @Test
    fun `Displays student annotation type in dialog when submission type is student annotation`() {
        val course = Course()
        val assignment = assignment.copy(
            submissionTypesRaw = listOf("student_annotation")
        )

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowSubmitDialogView(assignment, course, false))

        verify(timeout = 100) {
            view.showSubmitDialogView(assignment,
                SubmissionTypesVisibilities(
                    studentAnnotation = true
                )
            )
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowSubmitDialogView event calls showSubmitDialogView() with mediaRecording == true when submission type is MEDIA_RECORDING`() {
        val assignment = assignment.copy(
                submissionTypesRaw = listOf("media_recording")
        )

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowSubmitDialogView(assignment, course, false))

        verify(timeout = 100) {
            view.showSubmitDialogView(
                assignment,
                SubmissionTypesVisibilities(
                    mediaRecording = true
                )
            )
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowSubmitDialogView event calls showSubmitDialogView() with all submission types == true when submission type is all submittable submission types`() {
        val assignment = assignment.copy(
                submissionTypesRaw = listOf("media_recording", "online_url", "online_text_entry", "online_upload", "student_annotation")
        )

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowSubmitDialogView(assignment, course, true))

        verify(timeout = 100) {
            view.showSubmitDialogView(
                assignment,
                SubmissionTypesVisibilities(
                    textEntry = true,
                    urlEntry = true,
                    fileUpload = true,
                    mediaRecording = true,
                    studioUpload = true,
                    studentAnnotation = true
                )
            )
        }

        confirmVerified(view)
    }

    @Test
    fun `SubmissionStarted event calls returnToAssignmentDetails`() {
        connection.accept(SubmissionDetailsEmptyContentEffect.SubmissionStarted)

        verify(timeout = 100) {
            view.returnToAssignmentDetails()
        }

        confirmVerified(view)
    }

    private fun testVideo() {
        val uri = mockk<Uri>()
        val intent = mockk<Intent>()

        every { intent.action } returns ""

        mockkStatic("com.instructure.student.mobius.assignmentDetails.SubmissionUtilsKt")
        every { any<Uri>().getVideoIntent() } returns intent
        every { any<Context>().isIntentAvailable(any()) } returns true

        mockkObject(FileUploadUtils)
        every { FileUploadUtils.getExternalCacheDir(context) } returns File("")

        mockkStatic(FileProvider::class)
        every { FileProvider.getUriForFile(any(), any(), any()) } returns uri

        mockkObject(FilePrefs)
        every { FilePrefs.tempCaptureUri = any() }

        excludeRecords {
            context.packageName
            context.packageManager
        }

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowVideoRecordingView)

        verify(timeout = 100) {
            eventConsumer.accept(SubmissionDetailsEmptyContentEvent.StoreVideoUri(uri))
            context.startActivityForResult(intent, SubmissionDetailsEmptyContentFragment.VIDEO_REQUEST_CODE)
        }

        confirmVerified(eventConsumer, context)
    }

    private fun mockPermissions(hasPermission: Boolean, permissionGranted: Boolean = false) {
        // Mock both so we can mock the class and the extensions in the same file
        mockkObject(PermissionUtils)
        mockkStatic("${PermissionUtils::class.java.canonicalName}Kt")
        every { PermissionUtils.hasPermissions(context, *anyVararg()) } returns hasPermission andThen permissionGranted

        val block = slot<(Map<String, Boolean>) -> Unit>()

        every { context.requestPermissions(any(), capture(block)) } answers {
            block.invoke(mapOf(Pair("any", permissionGranted)))
        }
    }

    private fun testMediaPicker() {
        val intent = mockk<Intent>()
        every { intent.action } returns ""
        every { intent.addFlags(any()) } returns intent
        every { intent.putExtra(MediaStore.EXTRA_OUTPUT, uri) } returns intent

        mockkStatic("com.instructure.student.mobius.assignmentDetails.SubmissionUtilsKt")
        every { chooseMediaIntent } returns intent
        every { any<Context>().isIntentAvailable(any()) } returns true

        excludeRecords {
            context.packageName
            context.packageManager
        }

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowMediaPickerView)

        verify(timeout = 100) {
            context.startActivityForResult(intent, SubmissionDetailsEmptyContentFragment.CHOOSE_MEDIA_REQUEST_CODE)
        }

        confirmVerified(context)
    }
}

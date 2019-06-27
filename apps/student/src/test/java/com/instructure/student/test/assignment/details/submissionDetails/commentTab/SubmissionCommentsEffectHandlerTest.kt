/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.instructure.student.test.assignment.details.submissionDetails.commentTab

import android.app.Activity
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.requestPermissions
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsSharedEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsEffect
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsEffectHandler
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.ui.SubmissionCommentsView
import com.instructure.student.mobius.common.ChannelSource
import com.instructure.student.test.util.receiveOnce
import com.spotify.mobius.functions.Consumer
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.concurrent.Executors

class SubmissionCommentsEffectHandlerTest : Assert(){

    private val mockView: SubmissionCommentsView = mockk(relaxed = true)
    private val context: Activity = mockk(relaxed = true)
    private val effectHandler = SubmissionCommentsEffectHandler(context).apply { view = mockView }
    private val eventConsumer: Consumer<SubmissionCommentsEvent> = mockk(relaxed = true)
    private val connection = effectHandler.connect(eventConsumer)

    @Before
    fun setup() {
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    }

    @Test
    fun `ShowMediaCommentDialog effect results in view calling showMediaCommentDialog`() {
        connection.accept(SubmissionCommentsEffect.ShowMediaCommentDialog)

        verify(timeout = 100) {
            mockView.showMediaCommentDialog()
        }

        confirmVerified(mockView)
    }

    @Test
    fun `UploadMediaComment effect results in view calling showMediaUploadToast`() {
        connection.accept(SubmissionCommentsEffect.UploadMediaComment(File("test"), 123L, 123L))

        verify(timeout = 100) {
            mockView.showMediaUploadToast()
        }

        confirmVerified(mockView)
    }

    @Test
    fun `ShowAudioRecordingView effect with no permission results in view calling showPermissionDeniedToast`() {
        mockPermissions(false)

        connection.accept(SubmissionCommentsEffect.ShowAudioRecordingView)

        verify(timeout = 100) {
            mockView.showPermissionDeniedToast()
        }
    }

    @Test
    fun `ShowVideoRecordingView effect with no permission results in view calling showPermissionDeniedToast`() {
        mockPermissions(false)

        connection.accept(SubmissionCommentsEffect.ShowVideoRecordingView)

        verify(timeout = 100) {
            mockView.showPermissionDeniedToast()
        }
    }

    @Test
    fun `ShowAudioRecordingView effect with permission results in AudioRecordingViewLaunched shared event`() {
        mockPermissions(true)

        val channel = ChannelSource.getChannel<SubmissionDetailsSharedEvent>()
        val expectedEvent = SubmissionDetailsSharedEvent.AudioRecordingViewLaunched
        val actualEvent = channel.receiveOnce {
            connection.accept(SubmissionCommentsEffect.ShowAudioRecordingView)
        }
        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun `ShowVideoRecordingView effect with permission results in VideoRecordingViewLaunched shared event`() {
        mockPermissions(true)

        val channel = ChannelSource.getChannel<SubmissionDetailsSharedEvent>()
        val expectedEvent = SubmissionDetailsSharedEvent.VideoRecordingViewLaunched
        val actualEvent = channel.receiveOnce {
            connection.accept(SubmissionCommentsEffect.ShowVideoRecordingView)
        }
        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun `ShowAudioRecordingView effect without permissions results in AudioRecordingViewLaunched shared event`() {
        mockPermissions(hasPermission = true, permissionGranted = true)

        val channel = ChannelSource.getChannel<SubmissionDetailsSharedEvent>()
        val expectedEvent = SubmissionDetailsSharedEvent.AudioRecordingViewLaunched
        val actualEvent = channel.receiveOnce {
            connection.accept(SubmissionCommentsEffect.ShowAudioRecordingView)
        }
        assertEquals(expectedEvent, actualEvent)
    }


    @Test
    fun `ShowVideoRecordingView effect without permission results in VideoRecordingViewLaunched shared event`() {
        mockPermissions(hasPermission = true, permissionGranted = true)

        val channel = ChannelSource.getChannel<SubmissionDetailsSharedEvent>()
        val expectedEvent = SubmissionDetailsSharedEvent.VideoRecordingViewLaunched
        val actualEvent = channel.receiveOnce {
            connection.accept(SubmissionCommentsEffect.ShowVideoRecordingView)
        }
        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun `ShowAudioRecordingView effect with permission check results in AudioRecordingViewLaunched shared event`() {
        mockPermissions(hasPermission = false, permissionGranted = true)

        val channel = ChannelSource.getChannel<SubmissionDetailsSharedEvent>()
        val expectedEvent = SubmissionDetailsSharedEvent.AudioRecordingViewLaunched
        val actualEvent = channel.receiveOnce {
            connection.accept(SubmissionCommentsEffect.ShowAudioRecordingView)
        }
        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun `ShowVideoRecordingView effect with permission check results in VideoRecordingViewLaunched shared event`() {
        mockPermissions(hasPermission = false, permissionGranted = true)

        val channel = ChannelSource.getChannel<SubmissionDetailsSharedEvent>()
        val expectedEvent = SubmissionDetailsSharedEvent.VideoRecordingViewLaunched
        val actualEvent = channel.receiveOnce {
            connection.accept(SubmissionCommentsEffect.ShowVideoRecordingView)
        }
        assertEquals(expectedEvent, actualEvent)
    }

    private fun mockPermissions(hasPermission: Boolean, permissionGranted: Boolean = false) {
        // Mock both so we can mockk the class and the extensions in the same file
        mockkStatic(PermissionUtils::class)
        mockkStatic("com.instructure.pandautils.utils.PermissionUtilsKt")
        every { PermissionUtils.hasPermissions(context, *anyVararg()) } returns hasPermission andThen permissionGranted

        val block = slot<(Map<String, Boolean>) -> Unit>()

        every { context.requestPermissions(any(), capture(block)) } answers {
            block.invoke(mapOf(Pair("any", permissionGranted)))
        }
    }

}
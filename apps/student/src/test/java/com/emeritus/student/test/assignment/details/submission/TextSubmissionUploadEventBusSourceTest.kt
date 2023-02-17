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
package com.emeritus.student.test.assignment.details.submission

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.instructure.pandautils.utils.ActivityResult
import com.instructure.pandautils.utils.OnActivityResults
import com.instructure.pandautils.utils.RequestCodes
import com.emeritus.student.mobius.assignmentDetails.submission.text.TextSubmissionUploadEvent
import com.emeritus.student.mobius.assignmentDetails.submission.text.TextSubmissionUploadEventBusSource
import com.spotify.mobius.functions.Consumer
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TextSubmissionUploadEventBusSourceTest : Assert() {

    lateinit var consumer: Consumer<TextSubmissionUploadEvent>
    lateinit var source: TextSubmissionUploadEventBusSource

    @Before
    fun setup() {
        consumer = mockk(relaxed = true)
        source = TextSubmissionUploadEventBusSource().apply {
            this.subscribe(consumer)
        }
    }

    @Test
    fun `onActivityResults with PICK_IMAGE_GALLERY code results in ImageAdded event`() {
        val uri = mockk<Uri>()
        val intent = mockk<Intent>()
        val event = OnActivityResults(ActivityResult(RequestCodes.PICK_IMAGE_GALLERY, Activity.RESULT_OK, intent), null)

        every { intent.data } returns uri

        source.onActivityResults(event)

        verify(timeout = 100) {
            consumer.accept(TextSubmissionUploadEvent.ImageAdded(uri))
        }

        confirmVerified(consumer)
    }

    @Test
    fun `onActivityResults with PICK_IMAGE_GALLERY code with no uri results in ImageFailed event`() {
        val intent = mockk<Intent>()
        val event = OnActivityResults(ActivityResult(RequestCodes.PICK_IMAGE_GALLERY, Activity.RESULT_OK, intent), null)

        every { intent.data } returns null

        source.onActivityResults(event)

        verify(timeout = 100) {
            consumer.accept(TextSubmissionUploadEvent.ImageFailed)
        }

        confirmVerified(consumer)
    }

    @Test
    fun `onActivityResults with CAMERA_PIC_REQUEST code results in CameraImageTaken event`() {
        val event = OnActivityResults(ActivityResult(RequestCodes.CAMERA_PIC_REQUEST, Activity.RESULT_OK, null), null)

        source.onActivityResults(event)

        verify(timeout = 100) {
            consumer.accept(TextSubmissionUploadEvent.CameraImageTaken)
        }

        confirmVerified(consumer)
    }
}

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
package com.instructure.student.mobius.assignmentDetails.submissions.text

import android.app.Activity
import com.instructure.pandautils.utils.OnActivityResults
import com.instructure.pandautils.utils.RequestCodes
import com.instructure.pandautils.utils.remove
import com.instructure.student.mobius.common.EventBusSource
import org.greenrobot.eventbus.Subscribe

@Suppress("unused", "UNUSED_PARAMETER")
class TextSubmissionUploadEventBusSource : EventBusSource<TextSubmissionUploadEvent>() {
    private val subId = TextSubmissionUploadEventBusSource::class.java.name

    @Subscribe(sticky = true)
    fun onActivityResults(event: OnActivityResults) {
        event.once(subId) {
            if (it.resultCode == Activity.RESULT_OK) {
                when (it.requestCode) {
                    RequestCodes.PICK_IMAGE_GALLERY -> {
                        event.remove() //Remove the event so it doesn't show up again somewhere else
                        it.data?.data?.let { uri ->
                            sendEvent(TextSubmissionUploadEvent.ImageAdded(uri))
                        } ?: sendEvent(TextSubmissionUploadEvent.ImageFailed)
                    }
                    RequestCodes.CAMERA_PIC_REQUEST -> {
                        event.remove() //Remove the event so it doesn't show up again somewhere else
                        sendEvent(TextSubmissionUploadEvent.CameraImageTaken)
                    }
                }
            }
        }
    }
}

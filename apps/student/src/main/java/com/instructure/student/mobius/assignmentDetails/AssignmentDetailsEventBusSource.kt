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
package com.instructure.student.mobius.assignmentDetails

import android.app.Activity
import android.net.Uri
import com.instructure.canvasapi2.utils.Logger
import com.instructure.pandautils.utils.FilePrefs
import com.instructure.pandautils.utils.OnActivityResults
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsFragment
import com.instructure.student.mobius.common.EventBusSource
import org.greenrobot.eventbus.Subscribe

@Suppress("unused", "UNUSED_PARAMETER")
class AssignmentDetailsEventBusSource : EventBusSource<AssignmentDetailsEvent>() {
    private val subId = AssignmentDetailsEventBusSource::class.java.name

    @Subscribe(sticky = true)
    fun onActivityResults(event: OnActivityResults) {
        event.once(subId) {
            if (it.requestCode == AssignmentDetailsFragment.VIDEO_REQUEST_CODE) {
                if (it.resultCode == Activity.RESULT_OK) {
                    // Attempt to restore URI in case were were booted from memory
                    val cameraImageUri = Uri.parse(FilePrefs.tempCaptureUri)

                    // If it's still null, tell the user there is an error and return.
                    if (cameraImageUri == null) {
                        sendEvent(AssignmentDetailsEvent.OnVideoRecordingError)
                        return@once
                    }

                    sendEvent(AssignmentDetailsEvent.SendVideoRecording(cameraImageUri))
                } else {
                    sendEvent(AssignmentDetailsEvent.OnVideoRecordingError)
                }
            }
        }
    }

}

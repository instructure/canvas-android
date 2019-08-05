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
            when(it.requestCode) {
                AssignmentDetailsFragment.VIDEO_REQUEST_CODE -> {
                    if (it.resultCode == Activity.RESULT_OK) {
                        sendEvent(AssignmentDetailsEvent.SendVideoRecording)
                    } else {
                        sendEvent(AssignmentDetailsEvent.OnVideoRecordingError)
                    }
                }
                AssignmentDetailsFragment.CHOOSE_MEDIA_REQUEST_CODE -> {
                    if (it.resultCode == Activity.RESULT_OK && it.data != null && it.data?.data != null) {
                        sendEvent(AssignmentDetailsEvent.SendMediaFile(it.data!!.data!!))
                    } else {
                        sendEvent(AssignmentDetailsEvent.OnMediaPickingError)
                    }
                }
            }
        }
    }

}

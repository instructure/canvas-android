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
package com.emeritus.student.mobius.assignmentDetails.submissionDetails

import android.app.Activity
import com.instructure.pandautils.utils.OnActivityResults
import com.instructure.pandautils.utils.remove
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentEvent
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionDetailsEmptyContentFragment
import com.emeritus.student.mobius.common.EventBusSource
import org.greenrobot.eventbus.Subscribe

@Suppress("unused", "UNUSED_PARAMETER")
class SubmissionDetailsEmptyContentEventBusSource : EventBusSource<SubmissionDetailsEmptyContentEvent>() {
    private val subId = SubmissionDetailsEmptyContentEventBusSource::class.java.name

    @Subscribe(sticky = true)
    fun onActivityResults(event: OnActivityResults) {
        event.once(subId) {
            when(it.requestCode) {
                SubmissionDetailsEmptyContentFragment.VIDEO_REQUEST_CODE -> {
                    event.remove()
                    if (it.resultCode == Activity.RESULT_OK) {
                        sendEvent(SubmissionDetailsEmptyContentEvent.SendVideoRecording)
                    } else {
                        sendEvent(SubmissionDetailsEmptyContentEvent.OnVideoRecordingError)
                    }
                }
                SubmissionDetailsEmptyContentFragment.CHOOSE_MEDIA_REQUEST_CODE -> {
                    event.remove()
                    if (it.resultCode == Activity.RESULT_OK && it.data != null && it.data?.data != null) {
                        sendEvent(SubmissionDetailsEmptyContentEvent.SendMediaFile(it.data!!.data!!))
                    } else {
                        sendEvent(SubmissionDetailsEmptyContentEvent.OnMediaPickingError)
                    }
                }
            }
        }
    }
}

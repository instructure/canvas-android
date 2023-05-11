/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.emeritus.student.mobius.conferences.conference_details

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.utils.DataResult

sealed class ConferenceDetailsEvent {
    object PullToRefresh : ConferenceDetailsEvent()
    object JoinConferenceClicked : ConferenceDetailsEvent()
    object JoinConferenceFinished : ConferenceDetailsEvent()
    data class RecordingClicked(val recordingId: String) : ConferenceDetailsEvent()
    data class ShowRecordingFinished(val recordingId: String) : ConferenceDetailsEvent()
    data class RefreshFinished(val result: DataResult<List<Conference>>) : ConferenceDetailsEvent()
}

sealed class ConferenceDetailsEffect {
    object DisplayRefreshError: ConferenceDetailsEffect()
    data class JoinConference(val url: String, val authenticate: Boolean) : ConferenceDetailsEffect()
    data class ShowRecording(val recordingId: String, val url: String) : ConferenceDetailsEffect()
    data class RefreshData(val canvasContext: CanvasContext) : ConferenceDetailsEffect()
}

data class ConferenceDetailsModel(
    val canvasContext: CanvasContext,
    val conference: Conference,
    val isLoading: Boolean = false,
    val isJoining: Boolean = false,
    val launchingRecordings: Map<String, Boolean> = emptyMap()
)

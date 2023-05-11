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

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.instructure.canvasapi2.models.ConferenceRecording
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.validOrNull
import com.emeritus.student.R
import com.emeritus.student.mobius.common.ui.Presenter
import com.emeritus.student.mobius.conferences.conference_details.ui.ConferenceDetailsViewState
import com.emeritus.student.mobius.conferences.conference_details.ui.ConferenceRecordingViewState

object ConferenceDetailsPresenter : Presenter<ConferenceDetailsModel, ConferenceDetailsViewState> {
    override fun present(model: ConferenceDetailsModel, context: Context): ConferenceDetailsViewState {
        val status : String = when {
            model.conference.endedAt != null -> {
                val date = DateHelper.dayMonthDateFormat.format(model.conference.endedAt)
                val time = DateHelper.getFormattedTime(context, model.conference.endedAt)
                context.getString(R.string.conferenceConcludedDateAtTime, date, time)
            }
            model.conference.startedAt != null -> {
                val date = DateHelper.dayMonthDateFormat.format(model.conference.startedAt)
                val time = DateHelper.getFormattedTime(context, model.conference.startedAt)
                context.getString(R.string.conferenceStartedDateAtTime, date, time)
            }
            else -> context.getString(R.string.notStarted)
        }

        val description = model.conference.description.validOrNull() ?: context.getString(R.string.noConferenceDescription)

        val showJoinContainer = model.conference.startedAt != null && model.conference.endedAt == null

        return ConferenceDetailsViewState(
            isLoading = model.isLoading,
            isJoining = model.isJoining,
            title = model.conference.title.orEmpty(),
            status = status,
            description = description,
            showJoinContainer = showJoinContainer,
            showRecordingSection = model.conference.recordings.isNotEmpty(),
            recordings = model.conference.recordings.map { mapRecordingStates(it, context, model.launchingRecordings) }
        )
    }

    @VisibleForTesting
    fun mapRecordingStates(
        recording: ConferenceRecording,
        context: Context,
        launchingRecordings: Map<String, Boolean>
    ): ConferenceRecordingViewState {
        val date = DateHelper.getMonthDayAtTime(context, recording.createdAt, R.string.at)
        val duration = context.resources.getQuantityString(
            R.plurals.minutes,
            recording.durationMinutes.toInt(),
            recording.durationMinutes.toInt()
        )
        return ConferenceRecordingViewState(
            recordingId = recording.recordingId,
            title = recording.title,
            date = date,
            duration = duration,
            isLaunching = launchingRecordings[recording.recordingId] ?: false
        )
    }
}

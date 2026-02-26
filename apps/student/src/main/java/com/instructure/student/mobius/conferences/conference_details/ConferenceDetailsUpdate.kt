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
package com.instructure.student.mobius.conferences.conference_details

import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.student.mobius.common.ui.UpdateInit
import com.spotify.mobius.First
import com.spotify.mobius.Next

class ConferenceDetailsUpdate : UpdateInit<ConferenceDetailsModel, ConferenceDetailsEvent, ConferenceDetailsEffect>() {
    override fun performInit(model: ConferenceDetailsModel): First<ConferenceDetailsModel, ConferenceDetailsEffect> {
        return First.first(model, emptySet())
    }

    override fun update(
        model: ConferenceDetailsModel,
        event: ConferenceDetailsEvent
    ): Next<ConferenceDetailsModel, ConferenceDetailsEffect> {
        return when (event) {
            ConferenceDetailsEvent.PullToRefresh -> Next.next(
                model.copy(isLoading = true),
                setOf<ConferenceDetailsEffect>(ConferenceDetailsEffect.RefreshData(model.canvasContext))
            )
            is ConferenceDetailsEvent.RefreshFinished -> {
                if (event.result.isFail) {
                    Next.next<ConferenceDetailsModel, ConferenceDetailsEffect>(
                        model.copy(isLoading = false),
                        setOf(ConferenceDetailsEffect.DisplayRefreshError)
                    )
                } else {
                    val conference = event.result.dataOrThrow.find { it.id == model.conference.id }!!
                    Next.next<ConferenceDetailsModel, ConferenceDetailsEffect>(model.copy(conference = conference, isLoading = false))
                }
            }
            is ConferenceDetailsEvent.JoinConferenceClicked -> {
                val conference = model.conference
                val url: String = conference.joinUrl
                    ?: "${ApiPrefs.fullDomain}${model.canvasContext.toAPIString()}/conferences/${conference.id}/join"
                val authenticate: Boolean = url.startsWith(ApiPrefs.fullDomain)
                Next.next(
                    model.copy(isJoining = true),
                    setOf<ConferenceDetailsEffect>(ConferenceDetailsEffect.JoinConference(url, authenticate))
                )
            }
            is ConferenceDetailsEvent.RecordingClicked -> {
                val recording = model.conference.recordings.first { it.recordingId == event.recordingId }
                val url = recording.playbackUrl
                    ?: recording.playbackFormats.find { it.type == "presentation" }?.url
                    ?: recording.playbackFormats.firstOrNull()?.url.orEmpty()
                val newModel = model.copy(
                    launchingRecordings = model.launchingRecordings.plus(recording.recordingId to true)
                )
                Next.next(newModel, setOf(ConferenceDetailsEffect.ShowRecording(recording.recordingId, url)))
            }
            ConferenceDetailsEvent.JoinConferenceFinished -> Next.next(model.copy(isJoining = false))
            is ConferenceDetailsEvent.ShowRecordingFinished -> {
                val newModel = model.copy(
                    launchingRecordings = model.launchingRecordings.plus(event.recordingId to false)
                )
                Next.next(newModel)
            }
        }
    }
}

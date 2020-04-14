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

import com.instructure.canvasapi2.managers.ConferenceManager
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.student.mobius.common.ui.EffectHandler
import com.instructure.student.mobius.conferences.conference_details.ui.ConferenceDetailsView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ConferenceDetailsEffectHandler : EffectHandler<ConferenceDetailsView, ConferenceDetailsEvent, ConferenceDetailsEffect>() {
    override fun accept(effect: ConferenceDetailsEffect) {
        when (effect) {
            is ConferenceDetailsEffect.JoinConference -> joinConference(effect)
            is ConferenceDetailsEffect.RefreshData -> refreshData(effect)
            ConferenceDetailsEffect.DisplayRefreshError -> view?.displayRefreshError()
            is ConferenceDetailsEffect.ShowRecording -> showRecording(effect)
        }.exhaustive
    }

    private fun showRecording(effect: ConferenceDetailsEffect.ShowRecording) {
        launch {
            view?.launchUrl(effect.url)
            delay(3000) // Give the CustomTabIntent a chance to do its thing
            consumer.accept(ConferenceDetailsEvent.ShowRecordingFinished(effect.recordingId))
        }
    }

    private fun joinConference(effect: ConferenceDetailsEffect.JoinConference) {
        launch {
            var url = effect.url
            if (effect.authenticate) {
                try {
                    val authSession = awaitApi<AuthenticatedSession> { OAuthManager.getAuthenticatedSession(url, it) }
                    url = authSession.sessionUrl
                } catch (e: Throwable) {
                    // Try launching without authenticated URL
                }
            }
            view?.launchUrl(url)
            delay(3000) // Give the CustomTabIntent a chance to do its thing
            consumer.accept(ConferenceDetailsEvent.JoinConferenceFinished)
        }
    }

    private fun refreshData(effect: ConferenceDetailsEffect.RefreshData) {
        launch {
            val conferencesResult = ConferenceManager.getConferencesForContextAsync(effect.canvasContext, true).await()
            consumer.accept(ConferenceDetailsEvent.RefreshFinished(conferencesResult))
        }
    }
}

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
package com.instructure.student.mobius.conferences.conference_list

import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.student.mobius.common.ui.EffectHandler
import com.instructure.student.mobius.conferences.conference_list.ui.ConferenceListView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ConferenceListEffectHandler(
    private val repository: ConferenceListRepository
) : EffectHandler<ConferenceListView, ConferenceListEvent, ConferenceListEffect>() {
    override fun accept(effect: ConferenceListEffect) {
        when (effect) {
            is ConferenceListEffect.LoadData -> loadData(effect)
            is ConferenceListEffect.ShowConferenceDetails -> view?.showConferenceDetails(effect.conference)
            is ConferenceListEffect.LaunchInBrowser -> authenticateAndLaunchUrl(effect.url)
        }.exhaustive
    }

    private fun loadData(effect: ConferenceListEffect.LoadData) {
        launch {
            val conferencesResult = repository.getConferencesForContext(effect.canvasContext, effect.forceNetwork)
            consumer.accept(ConferenceListEvent.DataLoaded(conferencesResult))
        }
    }

    private fun authenticateAndLaunchUrl(url: String) {
        launch {
            var authenticatedUrl = url
            try {
                authenticatedUrl = repository.getAuthenticatedSession(url).sessionUrl
            } catch (e: Throwable) {
                // Try launching without authenticated URL
            }
            view?.launchUrl(authenticatedUrl)
            delay(3000) // Give the CustomTabIntent a chance to do its thing
            consumer.accept(ConferenceListEvent.LaunchInBrowserFinished)
        }
    }
}

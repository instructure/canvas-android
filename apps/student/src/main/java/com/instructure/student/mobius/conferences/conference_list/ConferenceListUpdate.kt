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

import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.student.mobius.common.ui.UpdateInit
import com.spotify.mobius.First
import com.spotify.mobius.Next

class ConferenceListUpdate : UpdateInit<ConferenceListModel, ConferenceListEvent, ConferenceListEffect>() {
    override fun performInit(model: ConferenceListModel): First<ConferenceListModel, ConferenceListEffect> {
        return First.first(
            model.copy(isLoading = true),
            setOf<ConferenceListEffect>(
                ConferenceListEffect.LoadData(
                    model.canvasContext,
                    false
                )
            )
        )
    }

    override fun update(
        model: ConferenceListModel,
        event: ConferenceListEvent
    ): Next<ConferenceListModel, ConferenceListEffect> {
        return when (event) {
            ConferenceListEvent.PullToRefresh -> Next.next(
                model.copy(isLoading = true),
                setOf<ConferenceListEffect>(
                    ConferenceListEffect.LoadData(
                        model.canvasContext,
                        true
                    )
                )
            )
            is ConferenceListEvent.DataLoaded -> {
                Next.next(model.copy(isLoading = false, listResult = event.listResult))
            }
            is ConferenceListEvent.ConferenceClicked -> {
                val conference = model.listResult!!.dataOrThrow.find { it.id == event.conferenceId }!!
                Next.dispatch(setOf(ConferenceListEffect.ShowConferenceDetails(conference)))
            }
            ConferenceListEvent.LaunchInBrowser -> {
                val url = ApiPrefs.fullDomain + model.canvasContext.toAPIString() + "/conferences"
                Next.next(
                    model.copy(isLaunchingInBrowser = true),
                    setOf<ConferenceListEffect>(ConferenceListEffect.LaunchInBrowser(url))
                )
            }
            ConferenceListEvent.LaunchInBrowserFinished -> Next.next(model.copy(isLaunchingInBrowser = false))
            is ConferenceListEvent.HeaderClicked -> {
                when (event.headerType) {
                    ConferenceHeaderType.NEW_CONFERENCES -> {
                        Next.next(model.copy(isNewConferencesExpanded = !model.isNewConferencesExpanded))
                    }
                    ConferenceHeaderType.CONCLUDED_CONFERENCES -> {
                        Next.next(model.copy(isConcludedConferencesExpanded = !model.isConcludedConferencesExpanded))
                    }
                }
            }
        }
    }
}

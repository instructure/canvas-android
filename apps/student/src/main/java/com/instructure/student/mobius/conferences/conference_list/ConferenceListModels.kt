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

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.utils.DataResult

sealed class ConferenceListEvent {
    object PullToRefresh : ConferenceListEvent()
    object LaunchInBrowser : ConferenceListEvent()
    object LaunchInBrowserFinished : ConferenceListEvent()
    data class DataLoaded(val listResult: DataResult<List<Conference>>) : ConferenceListEvent()
    data class ConferenceClicked(val conferenceId: Long) : ConferenceListEvent()
    data class HeaderClicked(val headerType: ConferenceHeaderType) : ConferenceListEvent()
}

enum class ConferenceHeaderType {
    NEW_CONFERENCES,
    CONCLUDED_CONFERENCES
}

sealed class ConferenceListEffect {
    data class LoadData(val canvasContext: CanvasContext, val forceNetwork: Boolean) : ConferenceListEffect()
    data class ShowConferenceDetails(val conference: Conference) : ConferenceListEffect()
    data class LaunchInBrowser(val url: String) : ConferenceListEffect()
}

data class ConferenceListModel(
    val canvasContext: CanvasContext,
    val isLoading: Boolean = false,
    val isLaunchingInBrowser: Boolean = false,
    val listResult: DataResult<List<Conference>>? = null,
    val isNewConferencesExpanded: Boolean = true,
    val isConcludedConferencesExpanded: Boolean = true
)

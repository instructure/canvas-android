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
package com.instructure.student.mobius.conferences.conference_list.ui

import com.instructure.student.mobius.conferences.conference_list.ConferenceHeaderType

sealed class ConferenceListViewState(open val isLaunchingInBrowser: Boolean) {
    class Loading(isLaunchingInBrowser: Boolean) : ConferenceListViewState(isLaunchingInBrowser)
    data class Loaded(
        override val isLaunchingInBrowser: Boolean,
        val itemStates: List<ConferenceListItemViewState>
    ) : ConferenceListViewState(isLaunchingInBrowser)
}

sealed class ConferenceListItemViewState {
    object Empty : ConferenceListItemViewState()
    object Error : ConferenceListItemViewState()
    data class ConferenceHeader(
        val title: String,
        val headerType: ConferenceHeaderType,
        val isExpanded: Boolean
    ): ConferenceListItemViewState()
    data class ConferenceItem(
        val tint: Int,
        val title: String,
        val subtitle: String,
        val label: String,
        val labelTint: Int,
        val conferenceId: Long,
        val isJoinable: Boolean
    ): ConferenceListItemViewState()
}

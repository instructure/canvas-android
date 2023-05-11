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
package com.emeritus.student.mobius.conferences.conference_details.ui

data class ConferenceDetailsViewState(
    val isLoading: Boolean,
    val isJoining: Boolean,
    val title: String,
    val status: String,
    val description: String,
    val showJoinContainer: Boolean,
    val showRecordingSection: Boolean,
    val recordings: List<ConferenceRecordingViewState>
)

data class ConferenceRecordingViewState(
    val recordingId: String,
    val title: String,
    val date: String,
    val duration: String,
    val isLaunching: Boolean
)

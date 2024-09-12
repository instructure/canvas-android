/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.features.alerts.settings

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    colorKeeper: ColorKeeper,
    private val repository: AlertSettingsRepository,
    private val crashlytics: FirebaseCrashlytics
) : ViewModel() {

    private val student =
        savedStateHandle.get<User>(Const.USER) ?: throw IllegalArgumentException("User not found")

    private val _uiState = MutableStateFlow(
        AlertSettingsUiState(
            student = student,
            avatarUrl = student.avatarUrl.orEmpty(),
            studentName = student.shortName ?: student.name,
            studentPronouns = student.pronouns,
            userColor = colorKeeper.getOrGenerateUserColor(student).backgroundColor()
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadAlertThresholds()
        }
    }

    private suspend fun loadAlertThresholds() {
        try {
            val alertThresholds = repository.loadAlertThresholds(student.id)
            _uiState.update {
                it.copy(
                    thresholds = alertThresholds.associateBy { it.alertType },
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Log.e("AlertSettingsViewModel", "Error loading alert thresholds", e)
        }
    }

}
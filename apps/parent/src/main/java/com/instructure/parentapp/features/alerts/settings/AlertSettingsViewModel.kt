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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.AlertType
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const
import com.instructure.parentapp.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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

    private val student = savedStateHandle.get<User>(Const.USER)
        ?: throw IllegalArgumentException("Student not found")

    private val _uiState = MutableStateFlow(
        AlertSettingsUiState(
            student = student,
            avatarUrl = student.avatarUrl.orEmpty(),
            studentName = student.shortName ?: student.name,
            studentPronouns = student.pronouns,
            userColor = colorKeeper.getOrGenerateUserColor(student).backgroundColor(),
            actionHandler = this::handleAction
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<AlertSettingsViewModelAction>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            loadAlertThresholds()
        }
    }

    private fun handleAction(alertSettingsAction: AlertSettingsAction) {
        viewModelScope.launch {
            when (alertSettingsAction) {
                is AlertSettingsAction.CreateThreshold -> {
                    try {
                        createAlertThreshold(
                            alertSettingsAction.alertType,
                            alertSettingsAction.threshold
                        )

                    } catch (e: Exception) {
                        crashlytics.recordException(e)
                        e.printStackTrace()
                        _events.send(AlertSettingsViewModelAction.ShowSnackbar(
                            message = R.string.generalUnexpectedError,
                            actionCallback = {
                                handleAction(alertSettingsAction)
                            }
                        ))
                    } finally {
                        loadAlertThresholds()
                    }
                }

                is AlertSettingsAction.DeleteThreshold -> {
                    try {
                        deleteAlertThreshold(
                            _uiState.value.thresholds[alertSettingsAction.alertType]?.id
                                ?: throw IllegalArgumentException("Threshold not found")
                        )
                    } catch (e: Exception) {
                        crashlytics.recordException(e)
                        e.printStackTrace()
                        _events.send(AlertSettingsViewModelAction.ShowSnackbar(
                            message = R.string.generalUnexpectedError,
                            actionCallback = {
                                handleAction(alertSettingsAction)
                            }
                        ))
                    } finally {
                        loadAlertThresholds()
                    }
                }

                is AlertSettingsAction.UnpairStudent -> {
                    _uiState.update {
                        it.copy(isLoading = true)
                    }
                    try {
                        _events.send(
                            AlertSettingsViewModelAction.UnpairStudent(
                                alertSettingsAction.studentId
                            )
                        )
                    } catch (e: Exception) {
                        crashlytics.recordException(e)
                        e.printStackTrace()
                        _uiState.update {
                            it.copy(isLoading = false)
                        }
                        _events.send(AlertSettingsViewModelAction.ShowSnackbar(
                            message = R.string.generalUnexpectedError,
                            actionCallback = {
                                handleAction(alertSettingsAction)
                            }
                        ))
                    }
                }

                is AlertSettingsAction.ReloadAlertSettings -> {
                    loadAlertThresholds()
                }
            }
        }
    }

    private suspend fun loadAlertThresholds() {
        _uiState.update {
            it.copy(
                isLoading = true,
                isError = false
            )
        }
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
            e.printStackTrace()
            _uiState.update {
                it.copy(isLoading = false, isError = true)
            }
        }
    }

    private suspend fun createAlertThreshold(alertType: AlertType, threshold: String?) {
        repository.createAlertThreshold(alertType, student.id, threshold)
    }

    private suspend fun deleteAlertThreshold(thresholdId: Long) {
        repository.deleteAlertThreshold(thresholdId)
    }

}
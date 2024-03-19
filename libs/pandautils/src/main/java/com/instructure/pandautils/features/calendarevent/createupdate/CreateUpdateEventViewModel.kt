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

package com.instructure.pandautils.features.calendarevent.createupdate

import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toSimpleDate
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.R
import com.instructure.pandautils.features.calendarevent.createupdate.CreateUpdateEventFragment.Companion.INITIAL_DATE
import com.instructure.pandautils.utils.toLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import javax.inject.Inject


@HiltViewModel
class CreateUpdateEventViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resources: Resources,
    private val repository: CreateUpdateEventRepository,
    private val apiPrefs: ApiPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateUpdateEventUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<CreateUpdateEventViewModelAction>()
    val events = _events.receiveAsFlow()

    private val initialDate = savedStateHandle.get<String>(INITIAL_DATE).toSimpleDate()?.toLocalDate() ?: LocalDate.now()

    init {
        loadCanvasContexts()
        setInitialState()
    }

    fun handleAction(action: CreateUpdateEventAction) {
        when (action) {
            is CreateUpdateEventAction.UpdateTitle -> {
                _uiState.update { it.copy(title = action.title) }
            }

            is CreateUpdateEventAction.UpdateDate -> {
                _uiState.update { it.copy(date = action.date) }
            }

            is CreateUpdateEventAction.UpdateStartTime -> {
                _uiState.update { it.copy(startTime = action.time) }
            }

            is CreateUpdateEventAction.UpdateEndTime -> {
                _uiState.update { it.copy(endTime = action.time) }
            }

            is CreateUpdateEventAction.UpdateFrequency -> {
                _uiState.update { it.copy(frequency = action.frequency) }
            }

            is CreateUpdateEventAction.UpdateCanvasContext -> {
                _uiState.update {
                    val selectCalendarUiState = it.selectCalendarUiState.copy(selectedCanvasContext = action.canvasContext)
                    it.copy(selectCalendarUiState = selectCalendarUiState)
                }
            }

            is CreateUpdateEventAction.UpdateLocation -> {
                _uiState.update { it.copy(location = action.location) }
            }

            is CreateUpdateEventAction.UpdateAddress -> {
                _uiState.update { it.copy(address = action.address) }
            }

            is CreateUpdateEventAction.UpdateDetails -> {
                _uiState.update { it.copy(details = action.details) }
            }

            is CreateUpdateEventAction.Save -> saveEvent()

            is CreateUpdateEventAction.SnackbarDismissed -> {
                _uiState.update { it.copy(errorSnack = null) }
            }

            is CreateUpdateEventAction.ShowSelectCalendarScreen -> {
                _uiState.update {
                    val selectCalendarUiState = it.selectCalendarUiState.copy(show = true)
                    it.copy(selectCalendarUiState = selectCalendarUiState)
                }
            }

            is CreateUpdateEventAction.HideSelectCalendarScreen -> {
                _uiState.update {
                    val selectCalendarUiState = it.selectCalendarUiState.copy(show = false)
                    it.copy(selectCalendarUiState = selectCalendarUiState)
                }
            }

            is CreateUpdateEventAction.CheckUnsavedChanges -> {
                action.result(checkUnsavedChanges())
            }
        }
    }

    private fun setInitialState() {
        _uiState.update { it.copy(date = initialDate) }
        // TODO
    }

    private fun loadCanvasContexts() {
        _uiState.update { it.copy(loadingCanvasContexts = true) }
        val userList = listOfNotNull(apiPrefs.user)

        //TODO Get canvas contexts for Teacher

        _uiState.update {
            it.copy(
                loadingCanvasContexts = false,
                selectCalendarUiState = it.selectCalendarUiState.copy(
                    canvasContexts = userList,
                    selectedCanvasContext = apiPrefs.user
                )
            )
        }
    }

    private fun saveEvent() {
        _uiState.update { it.copy(saving = true) }
        viewModelScope.tryLaunch {
            val startDate = uiState.value.startTime?.let {
                LocalDateTime.of(uiState.value.date, it).toApiString()
            } ?: uiState.value.date.toApiString()
            val endDate = uiState.value.endTime?.let {
                LocalDateTime.of(uiState.value.date, it).toApiString()
            } ?: uiState.value.date.toApiString()

            repository.createEvent(
                title = uiState.value.title,
                startDate = startDate.orEmpty(),
                endDate = endDate.orEmpty(),
                contextCode = uiState.value.selectCalendarUiState.selectedCanvasContext?.contextId.orEmpty(),
                locationName = uiState.value.location,
                locationAddress = uiState.value.address,
                description = uiState.value.details
            )

            _uiState.update { it.copy(saving = false) }
            _events.send(
                CreateUpdateEventViewModelAction.RefreshCalendarDays(
                    listOfNotNull(uiState.value.date)
                )
            )
        } catch {
            _uiState.update {
                it.copy(
                    saving = false,
                    errorSnack = resources.getString(R.string.eventSaveErrorMessage)
                )
            }
        }
    }

    private fun checkUnsavedChanges(): Boolean {
        return false
    }
}

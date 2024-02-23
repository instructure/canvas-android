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

package com.instructure.pandautils.features.todo.createupdate

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toDate
import com.instructure.canvasapi2.utils.toSimpleDate
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.toLocalDate
import com.instructure.pandautils.utils.toLocalTime
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class CreateUpdateToDoViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val repository: CreateUpdateToDoRepository,
    private val apiPrefs: ApiPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateUpdateToDoUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<CreateUpdateToDoViewModelAction>()
    val events = _events.receiveAsFlow()

    private val initialDate = savedStateHandle.get<String>(CreateUpdateToDoFragment.INITIAL_DATE).toSimpleDate()?.toLocalDate() ?: LocalDate.now()
    private val plannerItem: PlannerItem? = savedStateHandle.get<PlannerItem>(CreateUpdateToDoFragment.PLANNER_ITEM)

    init {
        loadCanvasContexts()
        setInitialState()
    }

    fun handleAction(action: CreateUpdateToDoAction) {
        when (action) {
            is CreateUpdateToDoAction.UpdateTitle -> {
                _uiState.update { it.copy(title = action.title) }
            }

            is CreateUpdateToDoAction.UpdateDate -> {
                _uiState.update { it.copy(date = action.date) }
            }

            is CreateUpdateToDoAction.UpdateTime -> {
                _uiState.update { it.copy(time = action.time) }
            }

            is CreateUpdateToDoAction.UpdateCanvasContext -> {
                _uiState.update { it.copy(selectedCanvasContext = action.canvasContext) }
            }

            is CreateUpdateToDoAction.UpdateDetails -> {
                _uiState.update { it.copy(details = action.details) }
            }

            is CreateUpdateToDoAction.Save -> saveToDo()

            is CreateUpdateToDoAction.SnackbarDismissed -> {
                _uiState.update { it.copy(errorSnack = null) }
            }

            is CreateUpdateToDoAction.ShowSelectCalendarScreen -> {
                _uiState.update { it.copy(showCalendarSelector = true) }
            }

            is CreateUpdateToDoAction.HideSelectCalendarScreen -> {
                _uiState.update { it.copy(showCalendarSelector = false) }
            }

            is CreateUpdateToDoAction.CheckUnsavedChanges -> {
                action.result(checkUnsavedChanges())
            }
        }
    }

    private fun setInitialState() {
        _uiState.update { it.copy(date = initialDate) }
        plannerItem?.let {
            val date = it.plannable.todoDate.toDate()
            _uiState.update { state ->
                state.copy(
                    title = it.plannable.title,
                    date = date?.toLocalDate() ?: state.date,
                    time = date?.toLocalTime() ?: state.time,
                    details = it.plannable.details.orEmpty()
                )
            }
        }
    }

    private fun loadCanvasContexts() {
        _uiState.update { it.copy(loadingCanvasContexts = true) }
        val userList = listOfNotNull(apiPrefs.user)
        viewModelScope.tryLaunch {
            val courses = repository.getCourses()
            _uiState.update {
                it.copy(
                    loadingCanvasContexts = false,
                    canvasContexts = userList + courses,
                    selectedCanvasContext = courses.firstOrNull { course ->
                        course.id == plannerItem?.plannable?.courseId
                    } ?: apiPrefs.user
                )
            }
        } catch {
            _uiState.update {
                it.copy(
                    loadingCanvasContexts = false,
                    canvasContexts = userList,
                    selectedCanvasContext = apiPrefs.user
                )
            }
        }
    }

    private fun saveToDo() {
        _uiState.update { it.copy(saving = true) }
        viewModelScope.tryLaunch {
            plannerItem?.let { plannerItem ->
                repository.updateToDo(
                    id = plannerItem.plannable.id,
                    title = uiState.value.title,
                    details = uiState.value.details,
                    toDoDate = LocalDateTime.of(uiState.value.date, uiState.value.time).toApiString().orEmpty(),
                    courseId = uiState.value.selectedCanvasContext.takeIf { it is Course }?.id,
                )
            } ?: run {
                repository.createToDo(
                    title = uiState.value.title,
                    details = uiState.value.details,
                    toDoDate = LocalDateTime.of(uiState.value.date, uiState.value.time).toApiString().orEmpty(),
                    courseId = uiState.value.selectedCanvasContext.takeIf { it is Course }?.id,
                )
            }
            _uiState.update { it.copy(saving = false) }
            _events.send(
                CreateUpdateToDoViewModelAction.RefreshCalendarDays(
                    listOfNotNull(plannerItem?.plannable?.todoDate?.toDate()?.toLocalDate(), uiState.value.date)
                )
            )
        } catch {
            _uiState.update {
                it.copy(
                    saving = false,
                    errorSnack = context.getString(R.string.todoSaveErrorMessage)
                )
            }
        }
    }

    private fun checkUnsavedChanges(): Boolean {
        return plannerItem?.let { plannerItem ->
            uiState.value.title != plannerItem.plannable.title ||
                    uiState.value.details != plannerItem.plannable.details.orEmpty() ||
                    uiState.value.date != plannerItem.plannable.todoDate.toDate()?.toLocalDate() ||
                    uiState.value.time != plannerItem.plannable.todoDate.toDate()?.toLocalTime() ||
                    uiState.value.selectedCanvasContext.takeIf { it is Course }?.id != plannerItem.plannable.courseId
        } ?: run {
            uiState.value.title.isNotEmpty() ||
                    uiState.value.details.isNotEmpty() ||
                    uiState.value.selectedCanvasContext != apiPrefs.user ||
                    uiState.value.date != initialDate ||
                    uiState.value.time != LocalTime.of(12, 0)
        }
    }
}

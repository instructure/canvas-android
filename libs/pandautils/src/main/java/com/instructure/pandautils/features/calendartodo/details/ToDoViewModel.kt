/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.features.calendartodo.details

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.ColorInt
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.toDate
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.R
import com.instructure.pandautils.features.calendartodo.details.ToDoFragment.Companion.PLANNABLE_ID
import com.instructure.pandautils.features.calendartodo.details.ToDoFragment.Companion.PLANNER_ITEM
import com.instructure.pandautils.features.reminder.ReminderItem
import com.instructure.pandautils.features.reminder.ReminderManager
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.toLocalDate
import com.instructure.pandautils.utils.todoHtmlUrl
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class ToDoViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val toDoRepository: ToDoRepository,
    private val apiPrefs: ApiPrefs,
    private val themePrefs: ThemePrefs,
    private val reminderManager: ReminderManager,
    private val toDoViewModelBehavior: ToDoViewModelBehavior
) : ViewModel() {

    private val _uiState = MutableStateFlow(ToDoUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<ToDoViewModelAction>()
    val events = _events.receiveAsFlow()

    private val plannableId: Long? = savedStateHandle.get<Long>(PLANNABLE_ID)
    private var plannerItem: PlannerItem? = savedStateHandle.get<PlannerItem>(PLANNER_ITEM)

    var checkingReminderPermission = false
    var checkingNotificationPermission = false

    init {
        loadData()
    }

    override fun onCleared() {
        super.onCleared()
        reminderManager.removeLiveDataObserver()
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            initDataById()

            plannerItem?.let { plannerItem ->
                val dateText = plannerItem.plannable.todoDate.toDate()?.let {
                    val dateText = DateHelper.dayMonthDateFormat.format(it)
                    val timeText = DateHelper.getFormattedTime(context, it)
                    context.getString(R.string.calendarAtDateTime, dateText, timeText)
                }

                _uiState.update {
                    it.copy(
                        title = plannerItem.plannable.title,
                        contextName = plannerItem.contextName,
                        contextColor = plannerItem.canvasContext.color,
                        date = dateText.orEmpty(),
                        description = plannerItem.plannable.details.orEmpty()
                    )
                }
            }

            observeReminders()
        } catch {
            _uiState.update {
                it.copy(
                    loading = false,
                    loadError = context.getString(R.string.errorLoadingTodo)
                )
            }
        }
    }

    private suspend fun initDataById() {
        plannableId?.let { plannableId ->
            _uiState.update { it.copy(loading = true) }
            val plannable = toDoRepository.getPlannerNote(plannableId)

            plannerItem = when {
                plannable.courseId != null -> {
                    val course = toDoRepository.getCourse(plannable.courseId.orDefault())
                    plannable.toPlannableItem(course.name)
                }
                plannable.groupId != null -> {
                    val group = toDoRepository.getGroup(plannable.groupId.orDefault())
                    plannable.toPlannableItem(group.name)
                }
                plannable.userId != null -> {
                    val user = toDoRepository.getUser(plannable.userId.orDefault())
                    plannable.toPlannableItem(user.name)
                }
                else -> {
                    plannable.toPlannableItem()
                }
            }

            _uiState.update { it.copy(loading = false) }
        }
    }

    private fun deleteToDo() {
        _uiState.update { it.copy(deleting = true) }
        viewModelScope.tryLaunch {
            plannerItem?.let { plannerItem ->
                toDoRepository.deletePlannerNote(plannerItem.plannable.id)
                _uiState.update { it.copy(deleting = false) }
                plannerItem.plannable.todoDate.toDate()?.toLocalDate()?.let {
                    _events.send(ToDoViewModelAction.RefreshCalendarDay(it))
                }
            }
            toDoViewModelBehavior.updateWidget()
        } catch {
            _uiState.update {
                it.copy(
                    deleting = false,
                    errorSnack = context.getString(R.string.todoDeleteErrorMessage)
                )
            }
        }
    }

    fun handleAction(action: ToDoAction) {
        when (action) {
            is ToDoAction.DeleteToDo -> deleteToDo()
            is ToDoAction.SnackbarDismissed -> _uiState.update { it.copy(errorSnack = null) }
            is ToDoAction.EditToDo -> plannerItem?.let {
                viewModelScope.launch {
                    _events.send(ToDoViewModelAction.OpenEditToDo(it))
                }
            }
            is ToDoAction.OnReminderAddClicked -> plannerItem?.let {
                viewModelScope.launch {
                    _events.send(ToDoViewModelAction.OnReminderAddClicked)
                }
            }
            is ToDoAction.OnReminderDeleteClicked -> {
                showDeleteReminderConfirmationDialog(action.context, action.reminderId, themePrefs.textButtonColor)
            }
        }
    }

    fun showCreateReminderDialog(context: Context, @ColorInt color: Int) {
        plannerItem?.let { plannerItem ->
            viewModelScope.launch {
                when {
                    plannerItem.plannableDate.before(Date()).orDefault() -> reminderManager.showCustomReminderDialog(
                        context,
                        apiPrefs.user?.id.orDefault(),
                        plannerItem.plannable.id,
                        plannerItem.plannable.title,
                        plannerItem.todoHtmlUrl(apiPrefs),
                        plannerItem.plannableDate,
                    )
                    else -> reminderManager.showBeforeDueDateReminderDialog(
                        context,
                        apiPrefs.user?.id.orDefault(),
                        plannerItem.plannable.id,
                        plannerItem.plannable.title,
                        plannerItem.todoHtmlUrl(apiPrefs),
                        plannerItem.plannableDate,
                        color
                    )
                }
            }
        }
    }

    private fun showDeleteReminderConfirmationDialog(context: Context, reminderId: Long, @ColorInt color: Int) {
        viewModelScope.launch { reminderManager.showDeleteReminderDialog(context, reminderId, color) }
    }

    private fun observeReminders() {
        plannerItem?.let { plannerItem ->
            reminderManager.observeRemindersLiveData(apiPrefs.user?.id.orDefault(), plannerItem.plannable.id) { reminders ->
                _uiState.update {
                    it.copy(
                        reminderUiState = it.reminderUiState.copy(
                            reminders = reminders.map { ReminderItem(it.id, it.text, Date(it.time)) }
                        )
                    )
                }
            }
        }
    }
}

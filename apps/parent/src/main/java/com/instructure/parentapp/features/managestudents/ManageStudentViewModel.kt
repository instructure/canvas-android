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

package com.instructure.parentapp.features.managestudents

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.orDefault
import com.instructure.parentapp.R
import com.instructure.parentapp.features.dashboard.SelectedStudentHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ManageStudentViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val colorKeeper: ColorKeeper,
    private val repository: ManageStudentsRepository,
    private val selectedStudentHolder: SelectedStudentHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageStudentsUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<ManageStudentsViewModelAction>()
    val events = _events.receiveAsFlow()

    private val studentMap = mutableMapOf<Long, User>()

    init {
        loadStudents()
    }

    fun refresh() {
        loadStudents(true)
    }

    private val userColorContentDescriptionMap = mapOf(
        R.color.studentBlue to R.string.studentColorContentDescriptionBlue,
        R.color.studentPurple to R.string.studentColorContentDescriptionPurple,
        R.color.studentPink to R.string.studentColorContentDescriptionPink,
        R.color.studentRed to R.string.studentColorContentDescriptionRed,
        R.color.studentOrange to R.string.studentColorContentDescriptionOrange,
        R.color.studentGreen to R.string.studentColorContentDescriptionGreen,
    )

    private val userColors by lazy {
        colorKeeper.userColors.map {
            UserColor(
                colorRes = it,
                color = colorKeeper.createThemedColor(context.getColor(it)),
                contentDescriptionRes = userColorContentDescriptionMap[it].orDefault()
            )
        }
    }

    private fun loadStudents(forceRefresh: Boolean = false) {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isLoadError = false
                )
            }

            val students = repository.getStudents(forceRefresh)
            studentMap.putAll(students.associateBy { it.id })

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    studentListItems = students.map {
                        StudentItemUiState(
                            studentId = it.id,
                            avatarUrl = it.avatarUrl,
                            studentName = it.shortName ?: it.name,
                            studentPronouns = it.pronouns,
                            studentColor = colorKeeper.getOrGenerateUserColor(it)
                        )
                    }
                )
            }
        } catch {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isLoadError = true
                )
            }
        }
    }

    private fun saveStudentColor(studentId: Long, selected: UserColor) {
        viewModelScope.tryLaunch {
            val contextId = "user_$studentId"
            val color = context.getColor(selected.colorRes)

            _uiState.update {
                it.copy(
                    colorPickerDialogUiState = it.colorPickerDialogUiState.copy(
                        isSavingColor = true
                    )
                )
            }

            val result = repository.saveStudentColor(contextId, getHexString(color))
            if (result != null) {
                colorKeeper.addToCache(contextId, color)
                _uiState.update {
                    it.copy(
                        colorPickerDialogUiState = ColorPickerDialogUiState(),
                        studentListItems = it.studentListItems.map { studentItem ->
                            if (studentItem.studentId == studentId) {
                                studentItem.copy(studentColor = selected.color)
                            } else {
                                studentItem
                            }
                        }
                    )
                }
            } else {
                showSavingError()
            }
            selectedStudentHolder.selectedStudentColorChanged()
        } catch {
            showSavingError()
        }
    }

    private fun getHexString(color: Int): String {
        var hexColor = Integer.toHexString(color)
        hexColor = hexColor.substring(hexColor.length - 6)
        if (hexColor.contains("#")) {
            hexColor = hexColor.replace("#".toRegex(), "")
        }
        return hexColor
    }

    private fun showSavingError() {
        _uiState.update {
            it.copy(
                colorPickerDialogUiState = it.colorPickerDialogUiState.copy(
                    isSavingColor = false,
                    isSavingColorError = true
                )
            )
        }
    }

    fun handleAction(action: ManageStudentsAction) {
        when (action) {
            is ManageStudentsAction.StudentTapped -> {
                viewModelScope.launch {
                    _events.send(ManageStudentsViewModelAction.NavigateToAlertSettings(studentMap[action.studentId] ?: throw IllegalArgumentException("Student not found")))
                }
            }

            is ManageStudentsAction.Refresh -> loadStudents(true)
            is ManageStudentsAction.AddStudent -> {
                viewModelScope.launch {
                    _events.send(ManageStudentsViewModelAction.AddStudent)
                }
            }
            is ManageStudentsAction.ShowColorPickerDialog -> _uiState.update {
                it.copy(
                    colorPickerDialogUiState = it.colorPickerDialogUiState.copy(
                        showColorPickerDialog = true,
                        studentId = action.studentId,
                        initialUserColor = userColors.find { userColor ->
                            userColor.color == action.studentColor
                        },
                        userColors = userColors
                    )
                )
            }

            is ManageStudentsAction.HideColorPickerDialog -> _uiState.update {
                it.copy(colorPickerDialogUiState = ColorPickerDialogUiState())
            }

            is ManageStudentsAction.StudentColorChanged -> saveStudentColor(action.studentId, action.userColor)
        }
    }
}

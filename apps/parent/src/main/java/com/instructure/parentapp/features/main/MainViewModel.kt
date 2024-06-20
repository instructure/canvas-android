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

package com.instructure.parentapp.features.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.loginapi.login.util.PreviousUsersUtils
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.orDefault
import com.instructure.parentapp.R
import com.instructure.parentapp.util.ParentPrefs
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
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: MainRepository,
    private val previousUsersUtils: PreviousUsersUtils,
    private val apiPrefs: ApiPrefs,
    private val parentPrefs: ParentPrefs,
    private val colorKeeper: ColorKeeper,
    private val themePrefs: ThemePrefs,
    private val selectedStudentHolder: SelectedStudentHolder
) : ViewModel() {

    private val _events = Channel<MainAction>()
    val events = _events.receiveAsFlow()

    private val _data = MutableStateFlow(MainViewData())
    val data = _data.asStateFlow()

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    val state = _state.asStateFlow()

    private val currentUser = previousUsersUtils.getSignedInUser(context, apiPrefs.domain, apiPrefs.user?.id.orDefault())

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.tryLaunch {
            _state.value = ViewState.Loading

            val user = repository.getSelf()
            user?.let { saveUserInfo(it) }

            val colors = repository.getColors()
            colors?.let { colorKeeper.addToCache(it) }

            val theme = repository.getTheme()
            theme?.let { themePrefs.applyCanvasTheme(it, context) }

            loadUserInfo()

            loadStudents()

            if (_data.value.studentItems.isEmpty()) {
                _state.value = ViewState.Empty(
                    R.string.noStudentsError,
                    R.string.noStudentsErrorDescription,
                    R.drawable.panda_manage_students
                )
            } else {
                _state.value = ViewState.Success
            }
        } catch {
            viewModelScope.launch {
                _state.value = ViewState.Error(context.getString(R.string.errorOccurred))
            }
        }
    }

    private suspend fun saveUserInfo(user: User) {
        val oldLocale = apiPrefs.effectiveLocale
        apiPrefs.user = user
        if (apiPrefs.effectiveLocale != oldLocale) {
            _events.send(MainAction.LocaleChanged)
        }
    }

    private fun loadUserInfo() {
        apiPrefs.user?.let { user ->
            _data.update {
                it.copy(
                    userViewData = UserViewData(
                        user.name,
                        user.pronouns,
                        user.shortName,
                        user.avatarUrl,
                        user.primaryEmail
                    )
                )
            }
        }
    }

    private suspend fun loadStudents() {
        val students = repository.getStudents()
        val selectedStudent = students.find { it.id == currentUser?.selectedStudentId } ?: students.firstOrNull()
        parentPrefs.currentStudent = selectedStudent
        selectedStudent?.let {
            selectedStudentHolder.updateSelectedStudent(it)
        }

        _data.update { data ->
            data.copy(
                studentItems = students.map { user ->
                    StudentItemViewModel(
                        StudentItemViewData(
                            user.id,
                            user.name,
                            user.avatarUrl.orEmpty()
                        )
                    ) { userId ->
                        onStudentSelected(students.first { it.id == userId })
                    }
                },
                selectedStudent = selectedStudent
            )
        }
    }

    private fun onStudentSelected(student: User) {
        parentPrefs.currentStudent = student
        currentUser?.let {
            previousUsersUtils.add(context, it.copy(selectedStudentId = student.id))
        }
        viewModelScope.launch {
            selectedStudentHolder.updateSelectedStudent(student)
        }
        _data.update {
            it.copy(
                studentSelectorExpanded = false,
                selectedStudent = student
            )
        }
    }

    fun toggleStudentSelector() {
        _data.update {
            it.copy(studentSelectorExpanded = !it.studentSelectorExpanded)
        }
    }

    fun closeStudentSelector() {
        _data.update {
            it.copy(studentSelectorExpanded = false)
        }
    }
}

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

package com.instructure.parentapp.features.dashboard

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController.Companion.KEY_DEEP_LINK_INTENT
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.loginapi.login.util.PreviousUsersUtils
import com.instructure.pandautils.mvvm.ViewState
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
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: DashboardRepository,
    private val previousUsersUtils: PreviousUsersUtils,
    private val apiPrefs: ApiPrefs,
    private val parentPrefs: ParentPrefs,
    private val selectedStudentHolder: SelectedStudentHolder,
    private val inboxCountUpdater: InboxCountUpdater,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _data = MutableStateFlow(DashboardViewData())
    val data = _data.asStateFlow()

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    val state = _state.asStateFlow()

    private val _events = Channel<DashboardAction>()
    val events = _events.receiveAsFlow()

    private val currentUser = previousUsersUtils.getSignedInUser(context, apiPrefs.domain, apiPrefs.user?.id.orDefault())
    private val intent = savedStateHandle.get<Intent>(KEY_DEEP_LINK_INTENT)

    init {
        handleDeeplink()
        loadData()
    }

    private fun handleDeeplink() {
        try {
            val uri = intent?.data

            uri?.let {
                viewModelScope.launch {
                    _events.send(DashboardAction.NavigateDeepLink(it))
                }
            }
        } catch (e: Exception) {
            Log.e(this.javaClass.simpleName, e.message.orEmpty())
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            inboxCountUpdater.shouldRefreshInboxCountFlow.collect {shouldUpdate ->
                if (shouldUpdate) {
                    updateUnreadCount()
                    inboxCountUpdater.updateShouldRefreshInboxCount(false)
                }
            }
        }

        viewModelScope.tryLaunch {
            _state.value = ViewState.Loading

            setupUserInfo()
            loadStudents()
            updateUnreadCount()

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

    private fun setupUserInfo() {
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
                            user.shortName.orEmpty(),
                            user.avatarUrl.orEmpty()
                        )
                    ) { userId ->
                        onStudentSelected(students.first { it.id == userId })
                    }
                },
                selectedStudent = selectedStudent
            )
        }

        if (_data.value.studentItems.isEmpty()) {
            _state.value = ViewState.Empty(
                R.string.noStudentsError,
                R.string.noStudentsErrorDescription,
                R.drawable.panda_manage_students
            )
        } else {
            _state.value = ViewState.Success
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

    private suspend fun updateUnreadCount() {
        val unreadCount = repository.getUnreadCounts()
        _data.update { data ->
            data.copy(
                unreadCount = unreadCount
            )
        }
    }

    fun toggleStudentSelector() {
        _data.update {
            it.copy(studentSelectorExpanded = !it.studentSelectorExpanded)
        }
    }
}

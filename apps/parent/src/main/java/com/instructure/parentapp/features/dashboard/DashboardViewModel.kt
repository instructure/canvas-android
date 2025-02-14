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

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.annotation.ColorInt
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController.Companion.KEY_DEEP_LINK_INTENT
import com.instructure.canvasapi2.models.LaunchDefinition
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.loginapi.login.util.PreviousUsersUtils
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.studentColor
import com.instructure.parentapp.R
import com.instructure.parentapp.features.alerts.list.AlertsRepository
import com.instructure.parentapp.util.ParentPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@SuppressLint("StaticFieldLeak")
@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: DashboardRepository,
    private val alertsRepository: AlertsRepository,
    private val previousUsersUtils: PreviousUsersUtils,
    private val apiPrefs: ApiPrefs,
    private val parentPrefs: ParentPrefs,
    private val selectedStudentHolder: SelectedStudentHolder,
    private val inboxCountUpdater: InboxCountUpdater,
    private val alertCountUpdater: AlertCountUpdater,
    private val analytics: Analytics,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _data = MutableStateFlow(DashboardViewData())
    val data = _data.asStateFlow()

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    val state = _state.asStateFlow()

    private val _events = Channel<DashboardViewModelAction>()
    val events = _events.receiveAsFlow()

    private val currentUser = previousUsersUtils.getSignedInUser(context, apiPrefs.domain, apiPrefs.user?.id.orDefault())
    private val intent = savedStateHandle.get<Intent>(KEY_DEEP_LINK_INTENT)

    private var students = mutableListOf<User>()

    init {
        handleDeeplink()
        loadData()
    }

    fun reloadData() {
        loadData(true)
    }

    private fun handleDeeplink() {
        val uri = intent?.data

        uri?.let {
            viewModelScope.launch {
                _events.send(DashboardViewModelAction.NavigateDeepLink(it))
            }
        }
    }

    private fun loadData(forceNetwork: Boolean = false) {
        viewModelScope.launch {
            inboxCountUpdater.shouldRefreshInboxCountFlow.collect { shouldUpdate ->
                if (shouldUpdate) {
                    updateUnreadCount()
                    inboxCountUpdater.updateShouldRefreshInboxCount(false)
                }
            }
        }

        viewModelScope.launch {
            alertCountUpdater.shouldRefreshAlertCountFlow.collect { shouldUpdate ->
                if (shouldUpdate) {
                    updateAlertCount()
                    alertCountUpdater.updateShouldRefreshAlertCount(false)
                }
            }
        }

        viewModelScope.tryLaunch {
            _state.value = ViewState.Loading

            loadLaunchDefinitions()
            setupUserInfo()
            loadStudents(forceNetwork)
            updateUnreadCount()
            updateAlertCount()

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

    private fun loadLaunchDefinitions() {
        viewModelScope.async {
            val launchDefinitions = repository.getLaunchDefinitions()
            val launchDefinitionsViewData = launchDefinitions
                .filter { it.domain != null && it.placements?.globalNavigation?.url != null }
                .map { LaunchDefinitionViewData(it.name.orEmpty(), it.domain.orEmpty(), it.placements?.globalNavigation?.url.orEmpty()) }
            if (launchDefinitionsViewData.isNotEmpty()) {
                _data.update {
                    it.copy(
                        launchDefinitionViewData = launchDefinitionsViewData
                    )
                }
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

    private suspend fun loadStudents(forceNetwork: Boolean) {
        selectedStudentHolder.updateSelectedStudent(null)
        val students = repository.getStudents(forceNetwork)
        val selectedStudent = if (this.students.isEmpty()) {
            students.find { it.id == currentUser?.selectedStudentId } ?: students.firstOrNull()
        } else {
            students.subtract(this.students.toSet()).firstOrNull() ?: students.firstOrNull()
        }
        this.students = students.toMutableList()

        parentPrefs.currentStudent = selectedStudent
        selectedStudentHolder.updateSelectedStudent(selectedStudent)

        val studentItems = students.map { user ->
            StudentItemViewModel(
                StudentItemViewData(
                    user.id,
                    user.shortName.orEmpty(),
                    user.avatarUrl.orEmpty()
                )
            ) { userId ->
                onStudentSelected(students.first { it.id == userId })
            }
        }

        val studentItemsWithAddStudent = if (studentItems.isNotEmpty()) {
            studentItems + AddStudentItemViewModel(
                selectedStudent.studentColor,
                ::addStudent
            )
        } else {
            studentItems
        }

        _data.update { data ->
            data.copy(
                studentSelectorContentDescription = getStudentSelectorContentDescription(
                    data.studentSelectorExpanded,
                    selectedStudent?.shortName.orEmpty()
                ),
                studentItems = studentItemsWithAddStudent,
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

    private fun addStudent() {
        viewModelScope.launch {
            analytics.logEvent(AnalyticsEventConstants.ADD_STUDENT_DASHBOARD)
            _events.send(DashboardViewModelAction.AddStudent)
        }
    }

    private fun onStudentSelected(student: User) {
        parentPrefs.currentStudent = student
        currentUser?.let {
            previousUsersUtils.add(context, it.copy(selectedStudentId = student.id))
        }
        _data.update {
            it.copy(
                studentSelectorExpanded = false,
                studentSelectorContentDescription = getStudentSelectorContentDescription(
                    false,
                    student.shortName.orEmpty()
                ),
                selectedStudent = student,
                studentItems = it.studentItems.map { item ->
                    if (item is AddStudentItemViewModel) {
                        item.copy(color = student.studentColor)
                    } else {
                        item
                    }
                }
            )
        }
        viewModelScope.launch {
            selectedStudentHolder.updateSelectedStudent(student)
            updateAlertCount()
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

    private suspend fun updateAlertCount() {
        _data.value.selectedStudent?.id?.let { selectedStudentId ->
            val alertCount = alertsRepository.getUnreadAlertCount(selectedStudentId)
            _data.update {
                it.copy(
                    alertCount = alertCount
                )
            }
        }
    }

    private fun openLtiTool(ltiViewData: LaunchDefinitionViewData?) {
        ltiViewData?.let {
            viewModelScope.launch {
                _events.send(DashboardViewModelAction.OpenLtiTool(it.url, it.name))
            }
        }
    }

    private fun getStudentSelectorContentDescription(expanded: Boolean, selectedStudentName: String) = context.getString(
        R.string.a11y_studentSelectorContentDescription,
        context.getString(if (expanded) R.string.a11y_studentSelectorCollapse else R.string.a11y_studentSelectorExpand),
        selectedStudentName
    )

    fun toggleStudentSelector() {
        _data.update {
            it.copy(
                studentSelectorExpanded = !it.studentSelectorExpanded,
                studentSelectorContentDescription = getStudentSelectorContentDescription(
                    !it.studentSelectorExpanded,
                    it.selectedStudent?.shortName.orEmpty()
                )
            )
        }
    }

    fun updateColor(@ColorInt color: Int) {
        _data.value.studentItems.find { it is AddStudentItemViewModel }?.let { addStudentItem ->
            (addStudentItem as AddStudentItemViewModel).updateColor(color)
        }
    }

    fun openMastery() {
        val masteryLaunchDefinition = _data.value.launchDefinitionViewData.find { it.domain == LaunchDefinition.MASTERY_DOMAIN }
        openLtiTool(masteryLaunchDefinition)
    }

    fun openStudio() {
        val studioLaunchDefinition = _data.value.launchDefinitionViewData.find { it.domain == LaunchDefinition.STUDIO_DOMAIN }
        openLtiTool(studioLaunchDefinition)
    }
}

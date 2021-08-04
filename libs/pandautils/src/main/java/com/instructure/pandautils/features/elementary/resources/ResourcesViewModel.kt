/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.pandautils.features.elementary.resources

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.ExternalToolManager
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.resources.itemviewmodels.ContactInfoItemViewModel
import com.instructure.pandautils.features.elementary.resources.itemviewmodels.ImportantLinksItemViewModel
import com.instructure.pandautils.features.elementary.resources.itemviewmodels.LtiApplicationItemViewModel
import com.instructure.pandautils.features.elementary.resources.itemviewmodels.ResourcesHeaderViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.HtmlContentFormatter
import com.instructure.pandautils.utils.toPx
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class ResourcesViewModel @Inject constructor(
    private val resources: Resources,
    private val courseManager: CourseManager,
    private val userManager: UserManager,
    private val externalToolManager: ExternalToolManager,
    private val oAuthManager: OAuthManager,
    private val htmlContentFormatter: HtmlContentFormatter
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<ResourcesViewData>
        get() = _data
    private val _data = MutableLiveData(ResourcesViewData(emptyList(), emptyList()))

    val events: LiveData<Event<ResourcesAction>>
        get() = _events
    private val _events = MutableLiveData<Event<ResourcesAction>>()

    init {
        _state.postValue(ViewState.Loading)
        loadData(false)
    }

    private fun loadData(forceNetwork: Boolean) {
        viewModelScope.launch {

            val coursesResult = courseManager.getCoursesWithSyllabusAsync(forceNetwork).await()

            val courses = coursesResult.dataOrThrow
                .filter { !it.homeroomCourse }

            val homeroomCourses = coursesResult.dataOrThrow.filter { it.homeroomCourse }
            val importantLinks = createImportantLinks(homeroomCourses)

            val actionItems = createActionItems(courses, homeroomCourses, forceNetwork)

            _state.postValue(ViewState.Success)
            _data.postValue(ResourcesViewData(importantLinks, actionItems))
        }
    }

    private fun createImportantLinks(homeroomCourses: List<Course>): List<ItemViewModel> {
        return homeroomCourses
            .map { ImportantLinksItemViewModel(it.syllabusBody ?: "", ::ltiButtonPressed) }
            .filter { it.htmlContent.isNotEmpty() }
    }

    private fun ltiButtonPressed(html: String, htmlContent: String) {
        viewModelScope.launch {
            try {
                val matcher = Pattern.compile("src=\"([^\"]+)\"").matcher(htmlContent)
                matcher.find()
                val url = matcher.group(1)

                if (url == null) {
                    _events.postValue(Event(ResourcesAction.WebLtiButtonPressed(html)))
                    return@launch
                }

                // Get an authenticated session so the user doesn't have to log in
                val authenticatedSessionURL = oAuthManager.getAuthenticatedSessionAsync(url).await().dataOrThrow.sessionUrl
                val newUrl = htmlContentFormatter.createAuthenticatedLtiUrl(html, authenticatedSessionURL)

                _events.postValue(Event(ResourcesAction.WebLtiButtonPressed(newUrl)))
            } catch (e: Exception) {
                // Couldn't get the authenticated session, try to load it without it
                _events.postValue(Event(ResourcesAction.WebLtiButtonPressed(html)))
            }
        }
    }

    private suspend fun createActionItems(courses: List<Course>, homeroomCourses: List<Course>, forceNetwork: Boolean): List<ItemViewModel> {
        val actionItems = mutableListOf<ItemViewModel>()
        val ltiApps = createLtiApps(courses, forceNetwork)
        if (ltiApps.isNotEmpty()) {
            actionItems.add(ResourcesHeaderViewModel(ResourcesHeaderViewData(resources.getString(R.string.studentApplications))))
            actionItems.addAll(ltiApps)
        }

        val staffInfo = createStaffInfo(homeroomCourses, forceNetwork)
        if (staffInfo.isNotEmpty()) {
            actionItems.add(ResourcesHeaderViewModel(ResourcesHeaderViewData(resources.getString(R.string.staffContactInfo), true)))
            actionItems.addAll(staffInfo)
        }

        return actionItems
    }

    private suspend fun createLtiApps(courses: List<Course>, forceNetwork: Boolean): List<ItemViewModel> {
        val contextIds = courses
            .map { it.contextId }

        val ltiTools = externalToolManager.getExternalToolsForCoursesAsync(contextIds, forceNetwork).await().dataOrNull

        val ltiToolsMapById = mutableMapOf<Long, MutableList<LTITool>>()
        ltiTools?.forEach {
            if (!ltiToolsMapById.contains(it.id)) {
                ltiToolsMapById[it.id] = mutableListOf()
            }
            ltiToolsMapById[it.id]?.add(it)
        }

        val displayedLtiTools = ltiTools?.distinctBy { it.id }

        return displayedLtiTools
            ?.mapIndexed { i: Int, ltiTool: LTITool ->
                createLtiApplicationItem(ltiTool, i == displayedLtiTools.size - 1, ltiToolsMapById[ltiTool.id] ?: emptyList()) }
            ?: emptyList()
    }

    private fun createLtiApplicationItem(ltiTool: LTITool, isLast: Boolean, courseLtiTools: List<LTITool>): LtiApplicationItemViewModel {
        val margin = if (isLast) 28.toPx else 0
        return LtiApplicationItemViewModel(
            LtiApplicationViewData(ltiTool.collaboration?.text ?: "", ltiTool.iconUrl ?: "", ltiTool.url ?: ""),
            margin
        ) { _events.postValue(Event(ResourcesAction.OpenLtiApp(courseLtiTools))) }
    }

    private suspend fun createStaffInfo(courses: List<Course>, forceNetwork: Boolean): List<ItemViewModel> {
        val teachers = courses
            .map { userManager.getTeacherListForCourseAsync(it.id, forceNetwork) }
            .awaitAll()
            .map { result -> result.dataOrNull ?: emptyList() }
            .flatten()
            .distinctBy { user: User -> user.id }

        return teachers.map {
            ContactInfoItemViewModel(ContactInfoViewData(it.shortName ?: "", getRoleString(it.enrollments[0].role), it.avatarUrl ?: "")) {
                _events.postValue(Event(ResourcesAction.OpenComposeMessage(it)))
            }
        }
    }

    private fun getRoleString(role: Enrollment.EnrollmentType?): String {
        return when (role) {
            Enrollment.EnrollmentType.Teacher -> resources.getString(R.string.staffRoleTeacher)
            Enrollment.EnrollmentType.Ta -> resources.getString(R.string.staffRoleTeacherAssistant)
            else -> ""
        }
    }

    fun refresh() {
        _state.postValue(ViewState.Refresh)
        loadData(true)
    }

    fun onImportantLinksViewsReady() {
        _events.postValue(Event(ResourcesAction.ImportantLinksViewsReady))
    }
}
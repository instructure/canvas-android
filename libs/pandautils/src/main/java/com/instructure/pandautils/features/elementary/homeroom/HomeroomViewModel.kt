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
package com.instructure.pandautils.features.elementary.homeroom

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.*
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.homeroom.itemviewmodels.AnnouncementItemViewModel
import com.instructure.pandautils.features.elementary.homeroom.itemviewmodels.CourseCardItemViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.ColorApiHelper
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.HtmlContentFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class HomeroomViewModel @Inject constructor(
    private val apiPrefs: ApiPrefs,
    private val resources: Resources,
    private val courseManager: CourseManager,
    private val announcementManager: AnnouncementManager,
    private val htmlContentFormatter: HtmlContentFormatter,
    private val oAuthManager: OAuthManager,
    private val colorKeeper: ColorKeeper,
    private val courseCardCreator: CourseCardCreator
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<HomeroomViewData>
        get() = _data
    private val _data = MutableLiveData(HomeroomViewData("", emptyList(), emptyList()))

    val events: LiveData<Event<HomeroomAction>>
        get() = _events
    private val _events = MutableLiveData<Event<HomeroomAction>>()

    var shouldUpdateAnnouncements: Boolean = true

    private var dashboardCourses: List<Course> = emptyList()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        _state.postValue(ViewState.Loading)
        loadData(false)
    }

    private fun loadData(forceNetwork: Boolean) {
        val greetingString = resources.getString(R.string.homeroomWelcomeMessage, apiPrefs.user?.shortName)

        viewModelScope.launch {
            try {
                val courses = courseManager.getCoursesAsync(forceNetwork).await()
                val dashboardCards = courseManager.getDashboardCoursesAsync(forceNetwork).await()

                val coursesMap = courses.dataOrThrow
                    .filter { !it.homeroomCourse }
                    .associateBy { it.id }

                dashboardCourses = dashboardCards.dataOrThrow.mapNotNull { coursesMap[it.id] }
                val homeroomCourses = courses.dataOrThrow.filter { it.homeroomCourse }

                courses.dataOrThrow.forEach {
                    colorKeeper.addToCache(it.contextId, getCourseColor(it))
                }

                val announcementsData = homeroomCourses
                    .map { announcementManager.getLatestAnnouncementAsync(it, forceNetwork) }
                    .awaitAll()

                val announcementViewModels = createAnnouncements(homeroomCourses, announcementsData)
                val courseViewModels = if (dashboardCourses.isNotEmpty()) {
                    createCourseCards(dashboardCourses, forceNetwork)
                } else {
                    emptyList()
                }

                val viewData = HomeroomViewData(greetingString, announcementViewModels, courseViewModels)
                shouldUpdateAnnouncements = true
                _data.postValue(viewData)

                if (viewData.isEmpty()) {
                    _state.postValue(
                        ViewState.Empty(
                            R.string.homeroomEmptyTitle,
                            R.string.homeroomEmptyMessage,
                            R.drawable.ic_panda_super
                        )
                    )
                } else {
                    _state.postValue(ViewState.Success)
                }
            } catch (e: Exception) {
                if (_data.value == null || _data.value?.isEmpty() == true) {
                    _state.postValue(ViewState.Error(resources.getString(R.string.homeroomError)))
                } else {
                    _state.postValue(ViewState.Error())
                    _events.postValue(Event(HomeroomAction.ShowRefreshError))
                }
            }
        }
    }

    private suspend fun createCourseCards(
        dashboardCourses: List<Course>,
        forceNetwork: Boolean,
        updateAssignments: Boolean = false
    ): List<CourseCardItemViewModel> {
        return courseCardCreator.createCourseCards(dashboardCourses, forceNetwork, updateAssignments, _events)
    }

    private fun getCourseColor(course: Course): String {
        return if (!course.courseColor.isNullOrEmpty()) {
            course.courseColor!!
        } else {
            ColorApiHelper.K5_DEFAULT_COLOR
        }
    }

    private suspend fun createAnnouncements(
        homeroomCourses: List<Course>,
        announcementsData: List<DataResult<List<DiscussionTopicHeader>>>
    ): List<AnnouncementItemViewModel> {
        return homeroomCourses
            .mapIndexed { index, course ->
                createAnnouncementViewModel(
                    course,
                    announcementsData[index].dataOrNull?.firstOrNull()
                )
            }
            .filterNotNull()
    }

    private suspend fun createAnnouncementViewModel(
        course: Course,
        announcement: DiscussionTopicHeader?
    ): AnnouncementItemViewModel? {
        return if (announcement != null) {
            val htmlWithIframes = htmlContentFormatter.formatHtmlWithIframes(
                announcement.message ?: "",
                course.id
            )
            AnnouncementItemViewModel(
                AnnouncementViewData(course.name, announcement.title ?: "", htmlWithIframes),
                { _events.postValue(Event(HomeroomAction.OpenAnnouncements(course))) },
                ::ltiButtonPressed
            )
        } else {
            null
        }
    }

    private fun ltiButtonPressed(html: String, announcementMessage: String) {
        viewModelScope.launch {
            try {
                val matcher = Pattern.compile("src=\"([^\"]+)\"").matcher(announcementMessage)
                matcher.find()
                val url = matcher.group(1)

                if (url == null) {
                    _events.postValue(Event(HomeroomAction.LtiButtonPressed(html)))
                    return@launch
                }

                // Get an authenticated session so the user doesn't have to log in
                val authenticatedSessionURL =
                    oAuthManager.getAuthenticatedSessionAsync(url).await().dataOrThrow.sessionUrl
                val newUrl = htmlContentFormatter.createAuthenticatedLtiUrl(html, authenticatedSessionURL)

                _events.postValue(Event(HomeroomAction.LtiButtonPressed(newUrl)))
            } catch (e: Exception) {
                // Couldn't get the authenticated session, try to load it without it
                _events.postValue(Event(HomeroomAction.LtiButtonPressed(html)))
            }
        }
    }

    fun refresh() {
        _state.postValue(ViewState.Refresh)
        loadData(true)
    }

    fun onAnnouncementViewsReady() {
        _events.postValue(Event(HomeroomAction.AnnouncementViewsReady))
    }

    fun refreshAssignmentsStatus() {
        _state.postValue(ViewState.Refresh)

        viewModelScope.launch {
            try {
                val courseViewModels = createCourseCards(dashboardCourses, false, updateAssignments = true)
                val viewData = _data.value

                shouldUpdateAnnouncements = false
                _data.postValue(
                    HomeroomViewData(
                        viewData?.greetingMessage ?: "",
                        viewData?.announcements ?: emptyList(),
                        courseViewModels
                    )
                )

                _state.postValue(ViewState.Success)
            } catch (e: Exception) {
                _state.postValue(ViewState.Error())
                _events.postValue(Event(HomeroomAction.ShowRefreshError))
            }
        }
    }
}
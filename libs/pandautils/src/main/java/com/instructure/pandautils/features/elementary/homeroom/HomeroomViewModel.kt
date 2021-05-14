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
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.AnnouncementManager
import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.homeroom.itemviewmodels.AnnouncementViewModel
import com.instructure.pandautils.features.elementary.homeroom.itemviewmodels.CourseCardViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.ColorApiHelper
import com.instructure.pandautils.utils.HtmlContentFormatter
import com.instructure.pandautils.utils.color
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
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
    private val assignmentManager: AssignmentManager,
    private val colorApiHelper: ColorApiHelper
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<HomeroomViewData>
        get() = _data
    private val _data = MutableLiveData<HomeroomViewData>(HomeroomViewData("", emptyList(), emptyList()))

    val events: LiveData<Event<HomeroomAction>>
        get() = _events
    private val _events = MutableLiveData<Event<HomeroomAction>>()

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
                if (forceNetwork) {
                    val colorsSynced = colorApiHelper.awaitSync()
                    if (colorsSynced) {
                        _events.postValue(Event(HomeroomAction.UpdateColors))
                    }
                }
                val courses = courseManager.getCoursesAsync(forceNetwork).await()
                val dashboardCards = courseManager.getDashboardCoursesAsync(forceNetwork).await()

                val coursesMap = courses.dataOrThrow
                    .filter { !it.homeroomCourse }
                    .associateBy { it.id }

                val dashboardCourses = dashboardCards.dataOrThrow.mapNotNull { coursesMap[it.id] }
                val homeroomCourses = courses.dataOrThrow.filter { it.homeroomCourse }

                val announcementsData = homeroomCourses
                    .map { announcementManager.getAnnouncementsAsync(it, forceNetwork) }
                    .awaitAll()

                val announcementViewModels = createAnnouncements(homeroomCourses, announcementsData)
                val courseViewModels = createCourseCards(dashboardCourses, forceNetwork)

                _data.postValue(HomeroomViewData(greetingString, announcementViewModels, courseViewModels))
                _state.postValue(ViewState.Success)
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

    private suspend fun createCourseCards(dashboardCourses: List<Course>, forceNetwork: Boolean): List<CourseCardViewModel> {
        val announcements = dashboardCourses
            .map { announcementManager.getAnnouncementsAsync(it, forceNetwork) }
            .awaitAll()
            .map { it.dataOrNull?.firstOrNull() }

        val assignmentsDueText = dashboardCourses
            .map { assignmentManager.getAllAssignmentsAsync(it.id, forceNetwork) }
            .awaitAll()
            .map { createDueTextFromAssignmentList(it.dataOrNull) }

        return dashboardCourses
            .mapIndexed { index, course ->
                val viewData = CourseCardViewData(
                    course.name,
                    assignmentsDueText[index],
                    announcements[index]?.title ?: "",
                    course.color,
                    course.imageUrl ?: "")

                CourseCardViewModel(
                    viewData,
                    { _events.postValue(Event(HomeroomAction.OpenCourse(course))) },
                    { _events.postValue(Event(HomeroomAction.OpenAssignments(course))) },
                    { openAnnouncementDetails(course, announcements[index]) }
                )
            }
    }

    private fun createDueTextFromAssignmentList(assignments: List<Assignment>?): SpannableString {
        var missing = 0
        var dueToday = 0
        assignments?.forEach {
            when {
                isAssignmentMissing(it) -> missing++
                isAssignmentDueToday(it) -> dueToday++
            }
        }

        val dueTodayString = if (dueToday == 0) {
            resources.getString(R.string.nothingDueToday)
        } else {
            resources.getString(R.string.dueToday, dueToday)
        }

        return if (missing == 0) {
            SpannableString(dueTodayString)
        } else {
            val missingString = resources.getString(R.string.missing, missing)
            val separator = " | "
            val completeString = SpannableString(dueTodayString + separator + missingString)
            val spanColor = resources.getColor(R.color.destructive, null)
            completeString.setSpan(ForegroundColorSpan(spanColor), dueTodayString.length + separator.length, completeString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return completeString
        }
    }

    private fun isAssignmentMissing(assignment: Assignment): Boolean {
        return assignment.dueAt?.let {
            val now = OffsetDateTime.now()
            val dueDate = OffsetDateTime.parse(it).withOffsetSameInstant(OffsetDateTime.now().offset)
            now.isAfter(dueDate)
        } ?: false
    }

    private fun isAssignmentDueToday(assignment: Assignment): Boolean {
        return assignment.dueAt?.let {
            val dueDate = OffsetDateTime.parse(it).withOffsetSameInstant(OffsetDateTime.now().offset)
            dueDate.toLocalDate().equals(LocalDate.now())
        } ?: false
    }

    private fun openAnnouncementDetails(course: Course, announcement: DiscussionTopicHeader?) {
        if (announcement != null) {
            _events.postValue(Event(HomeroomAction.OpenAnnouncementDetails(course, announcement)))
        }
    }

    private suspend fun createAnnouncements(homeroomCourses: List<Course>, announcementsData: List<DataResult<List<DiscussionTopicHeader>>>): List<AnnouncementViewModel> {
        return homeroomCourses
            .mapIndexed { index, course -> createAnnouncementViewModel(course, announcementsData[index].dataOrNull?.firstOrNull()) }
            .filterNotNull()
    }

    private suspend fun createAnnouncementViewModel(course: Course, announcement: DiscussionTopicHeader?): AnnouncementViewModel? {
        return if (announcement != null) {
            val htmlWithIframes = htmlContentFormatter.formatHtmlWithIframes(announcement.message
                ?: "")
            AnnouncementViewModel(
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
                val authenticatedSessionURL = oAuthManager.getAuthenticatedSessionAsync(url).await().dataOrThrow.sessionUrl
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
}
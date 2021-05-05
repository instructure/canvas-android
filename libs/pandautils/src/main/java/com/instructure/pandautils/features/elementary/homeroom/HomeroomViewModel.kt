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

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.AnnouncementManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.homeroom.itemviewmodels.AnnouncementViewModel
import com.instructure.pandautils.features.elementary.homeroom.itemviewmodels.CourseCardViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeroomViewModel @Inject constructor(
    private val apiPrefs: ApiPrefs,
    @ApplicationContext private val context: Context,
    private val courseManager: CourseManager,
    private val announcementManager: AnnouncementManager
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<HomeroomViewData>
        get() = _data
    private val _data = MutableLiveData<HomeroomViewData>(HomeroomViewData("", emptyList(), emptyList(), true))

    val events: LiveData<Event<HomeroomAction>>
        get() = _events
    private val _events = MutableLiveData<Event<HomeroomAction>>()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        _state.postValue(ViewState.Loading)
        loadData(true) // TODO change back to false when finished
    }

    private fun loadData(forceNetwork: Boolean) {
        val greetingString = context.getString(R.string.homeroomWelcomeMessage, apiPrefs.user?.shortName)

        viewModelScope.launch {
            val courses = courseManager.getCoursesAsync(forceNetwork).await()

            val homeroomCourses = courses.dataOrThrow.filter { it.homeroomCourse }

            val announcementsData = homeroomCourses
                .map { announcementManager.getAnnouncementsFromLastTwoWeeksAsync(it, forceNetwork) }
                .awaitAll()

            val announcementViewModels = createAnnouncements(homeroomCourses, announcementsData)
            val courseCards = createDummyCourses()
            val isEmpty = announcementViewModels.isEmpty() && courseCards.isEmpty()

            _data.postValue(HomeroomViewData(greetingString, announcementViewModels, courseCards, isEmpty))
            _state.postValue(ViewState.Success)
        }
    }

    private fun createAnnouncements(homeroomCourses: List<Course>, announcementsData: List<DataResult<List<DiscussionTopicHeader>>>): List<AnnouncementViewModel> {
        return homeroomCourses
            .mapIndexed { index, course -> createAnnouncementViewModel(course, announcementsData[index].dataOrNull?.firstOrNull()) }
            .filterNotNull()
    }

    private fun createAnnouncementViewModel(course: Course, announcement: DiscussionTopicHeader?): AnnouncementViewModel? {
        return if (announcement != null) {
            AnnouncementViewModel(
                AnnouncementViewData(course.name, announcement.title ?: "", announcement.message ?: ""),
                { _events.postValue(Event(HomeroomAction.OpenAnnouncements(course))) }
            )
        } else {
            null
        }
    }

    // TODO Courses will be implemented in a separate ticket
    private fun createDummyCourses(): List<ItemViewModel> {
        return (1..10).map { CourseCardViewModel(CourseCardViewData("Course: $it")) }
    }

    fun refresh() {
        _state.postValue(ViewState.Refresh)
        loadData(true)
    }
}
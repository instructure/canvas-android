/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.student.features.elementary.course

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.managers.TabManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Logger
import com.instructure.pandautils.R
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.isCourse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ElementaryCourseViewModel @Inject constructor(
    private val tabManager: TabManager,
    private val resources: Resources,
    private val apiPrefs: ApiPrefs,
    private val oauthManager: OAuthManager,
    private val courseManager: CourseManager,
    private val firebaseCrashlytics: FirebaseCrashlytics
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<ElementaryCourseViewData>
        get() = _data
    private val _data = MutableLiveData<ElementaryCourseViewData>()

    val events: LiveData<Event<ElementaryCourseAction>>
        get() = _events
    private val _events = MutableLiveData<Event<ElementaryCourseAction>>()

    fun getData(canvasContext: CanvasContext, tabId: String) {
        _state.postValue(ViewState.Loading)
        viewModelScope.launch {
            try {
                val dashboardCards = courseManager.getDashboardCoursesAsync(false).await().dataOrNull ?: emptyList()
                val isElementaryCourse = dashboardCards.any { it.id == canvasContext.id && it.isK5Subject }

                if (isElementaryCourse) {
                    val tabs = loadTabs(canvasContext)

                    val isTabHidden = tabId.isNotEmpty() && tabs.find { it.tabId == tabId } == null
                    if (isTabHidden) {
                        handleNonElementaryCourse(tabId)
                        return@launch
                    }

                    val tabViewData = createTabs(canvasContext, tabs).toMutableList()

                    _data.postValue(ElementaryCourseViewData(tabViewData, canvasContext.color))
                    _state.postValue(ViewState.Success)
                } else {
                    handleNonElementaryCourse(tabId)
                }
            } catch (e: Exception) {
                _state.postValue(ViewState.Error(resources.getString(R.string.error_loading_course_details)))
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    private suspend fun loadTabs(canvasContext: CanvasContext): List<Tab> {
        val tabs = tabManager.getTabsForElementaryAsync(canvasContext, false).await().dataOrThrow
        val hasResources = tabs.firstOrNull { it.isExternal } != null
        var filteredTabs = tabs.filter { !it.isHidden && !it.isExternal }.sortedBy { it.position }

        if (hasResources) {
            filteredTabs = filteredTabs.toMutableList()
            filteredTabs.add(
                Tab(
                    tabId = Tab.RESOURCES_ID,
                    label = resources.getString(R.string.dashboardTabResources)
                )
            )
        }

        return filteredTabs
    }

    private fun handleNonElementaryCourse(tabId: String) {
        when (tabId) {
            Tab.GRADES_ID -> _events.postValue(Event(ElementaryCourseAction.RedirectToGrades))
            Tab.MODULES_ID -> _events.postValue(Event(ElementaryCourseAction.RedirectToModules))
            else -> _events.postValue(Event(ElementaryCourseAction.RedirectToCourseBrowserPage))
        }
    }

    private fun getIconDrawable(tabId: String): Drawable? {
        return when (tabId) {
            Tab.HOME_ID -> {
                resources.getDrawable(R.drawable.ic_home)
            }

            Tab.SCHEDULE_ID -> {
                resources.getDrawable(R.drawable.ic_schedule)
            }

            Tab.MODULES_ID -> {
                resources.getDrawable(R.drawable.ic_modules)
            }

            Tab.GRADES_ID -> {
                resources.getDrawable(R.drawable.ic_grades)
            }

            Tab.RESOURCES_ID -> {
                resources.getDrawable(R.drawable.ic_resources)
            }

            else -> {
                null
            }
        }
    }

    private suspend fun createTabs(canvasContext: CanvasContext, tabs: List<Tab>): List<ElementaryCourseTab> {
        val prefix =
            if (canvasContext.isCourse) "${apiPrefs.fullDomain}/courses/${canvasContext.id}?embed=true" else "${apiPrefs.fullDomain}/groups/${canvasContext.id}?embed=true"
        return tabs.map {
            val drawable = getIconDrawable(it.tabId)
            val url = when (it.tabId) {
                Tab.HOME_ID -> "$prefix#home"
                Tab.SCHEDULE_ID -> "$prefix#schedule"
                Tab.MODULES_ID -> "$prefix#modules"
                Tab.GRADES_ID -> "$prefix#grades"
                Tab.RESOURCES_ID -> "$prefix#resources"
                else -> it.htmlUrl ?: ""
            }

            val authenticatedUrl = oauthManager.getAuthenticatedSessionAsync(url).await().dataOrNull?.sessionUrl
            ElementaryCourseTab(it.tabId, drawable, it.label, authenticatedUrl ?: url)

        }
    }
}
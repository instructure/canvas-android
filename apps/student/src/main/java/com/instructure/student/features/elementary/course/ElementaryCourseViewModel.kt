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
import com.instructure.canvasapi2.managers.TabManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Logger
import com.instructure.pandautils.R
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.isCourse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ElementaryCourseViewModel @Inject constructor(
    private val tabManager: TabManager,
    private val resources: Resources,
    private val apiPrefs: ApiPrefs
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<ElementaryCourseViewData>
        get() = _data
    private val _data = MutableLiveData<ElementaryCourseViewData>()

    fun getData(canvasContext: CanvasContext, forceNetwork: Boolean = false) {
        _state.postValue(ViewState.Loading)
        viewModelScope.launch {
            try {
                val tabs = tabManager.getTabsForElementaryAsync(canvasContext, forceNetwork).await().dataOrThrow
                val filteredTabs = tabs.filter { !it.isHidden && !it.isExternal }.sortedBy { it.position }

                val tabViewData = createTabs(canvasContext, filteredTabs)
                _data.postValue(ElementaryCourseViewData(tabViewData))
                _state.postValue(ViewState.Success)
            } catch (e: Exception) {
                _state.postValue(ViewState.Error(resources.getString(R.string.error_loading_course_details)))
                Logger.e("Failed to load tabs")
            }
        }
    }

    private fun createTabs(canvasContext: CanvasContext, tabs: List<Tab>): List<ElementaryCourseTab> {
        val prefix = if (canvasContext.isCourse) "${apiPrefs.fullDomain}/courses/${canvasContext.id}?embed=true" else "${apiPrefs.fullDomain}/groups/${canvasContext.id}?embed=true"
        return tabs.map {
            val drawable: Drawable?
            val url: String?
            when (it.tabId) {
                Tab.HOME_ID -> {
                    drawable = resources.getDrawable(R.drawable.ic_home)
                    url = "$prefix#home"
                }
                Tab.SCHEDULE_ID -> {
                    drawable = resources.getDrawable(R.drawable.ic_schedule)
                    url = "$prefix#schedule"
                }
                Tab.MODULES_ID -> {
                    drawable = resources.getDrawable(R.drawable.ic_modules)
                    url = "$prefix#modules"
                }
                Tab.GRADES_ID -> {
                    drawable = resources.getDrawable(R.drawable.ic_grades)
                    url = "$prefix#grades"
                }
                Tab.RESOURCES_ID -> {
                    drawable = resources.getDrawable(R.drawable.ic_resources)
                    url = "$prefix#resources"
                }
                else -> {
                    drawable = null
                    url = it.htmlUrl
                }
            }
            ElementaryCourseTab(drawable, it.label, url)
        }
    }
}
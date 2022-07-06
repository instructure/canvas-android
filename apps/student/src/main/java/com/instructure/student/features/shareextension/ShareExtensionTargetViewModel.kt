/*
 * Copyright (C) 2022 - present Instructure, Inc.
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

package com.instructure.student.features.shareextension

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.student.features.elementary.course.ElementaryCourseAction
import com.instructure.student.features.elementary.course.ElementaryCourseViewData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShareExtensionTargetViewModel @Inject constructor(
        private val courseManager: CourseManager
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<ShareExtensionTargetViewData>
        get() = _data
    private val _data = MutableLiveData<ShareExtensionTargetViewData>()

    val events: LiveData<Event<ShareExtensionTargetAction>>
        get() = _events
    private val _events = MutableLiveData<Event<ShareExtensionTargetAction>>()

    init {
        fetchCourses()
    }

    private fun fetchCourses() {
        viewModelScope.launch {
            try {
                val courses = courseManager.getCoursesAsync(true).await().dataOrThrow
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
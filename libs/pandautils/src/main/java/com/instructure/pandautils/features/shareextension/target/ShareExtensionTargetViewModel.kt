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

package com.instructure.pandautils.features.shareextension.target

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.R
import com.instructure.pandautils.BR
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.features.shareextension.target.itemviewmodels.ShareExtensionAssignmentItemViewModel
import com.instructure.pandautils.features.shareextension.target.itemviewmodels.ShareExtensionCourseItemViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ShareExtensionTargetViewModel @Inject constructor(
        private val courseManager: CourseManager,
        private val assignmentManager: AssignmentManager,
        private val resources: Resources
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

    private var courses: List<Course> = emptyList()
    private var assignments: List<Assignment> = emptyList()

    private var selectedCourse: Course? = null
    private var selectedAssignment: Assignment? = null

    private var selectedTarget: ShareTarget = ShareTarget.MY_FILES

    init {
        fetchCourses()
    }

    private fun fetchCourses() {
        viewModelScope.launch {
            try {
                courses = courseManager.getCoursesAsync(true)
                        .await()
                        .dataOrThrow

                val courseViewModels = courses
                        .map { ShareExtensionCourseItemViewModel(ShareExtensionCourseViewData(it.name, it.color)) }
                _data.postValue(ShareExtensionTargetViewData(courseViewModels))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onCourseSelected(position: Int) {
        selectedCourse = courses[position]
        fetchAssignment(selectedCourse?.id ?: throw IllegalArgumentException())
    }

    fun onAssignmentSelected(position: Int) {
        if (assignments.isNotEmpty()) {
            selectedAssignment = assignments[position]
        }
    }

    private fun fetchAssignment(courseId: Long) {
        _data.value?.assignments = listOf(ShareExtensionAssignmentItemViewModel(ShareExtensionAssignmentViewData(resources.getString(R.string.loadingAssignments))))
        _data.value?.notifyPropertyChanged(BR.assignments)

        viewModelScope.launch {
            assignments = assignmentManager.getAllAssignmentsAsync(courseId, true)
                    .await()
                    .dataOrThrow
                    .filter {
                        val currentDate = Date()
                        ((it.lockDate == null || it.lockDate != null && currentDate.before(it.lockDate)) && (it.unlockDate == null || it.unlockDate != null && currentDate.after(it.unlockDate)))
                                && it.getSubmissionTypes().contains(Assignment.SubmissionType.ONLINE_UPLOAD)
                    }

            val assignmentViewModels = if (assignments.isNullOrEmpty()) {
                listOf(ShareExtensionAssignmentItemViewModel(ShareExtensionAssignmentViewData(resources.getString(R.string.noAssignmentsWithFileUpload))))
            } else {
                assignments.map {
                    ShareExtensionAssignmentItemViewModel(ShareExtensionAssignmentViewData(it.name ?: ""))
                }
            }
            _data.value?.assignments = assignmentViewModels
            _data.value?.notifyPropertyChanged(BR.assignments)
        }
    }

    fun assignmentTargetSelected() {
        selectedTarget = ShareTarget.ASSIGNMENT
        _events.postValue(Event(ShareExtensionTargetAction.AssignmentTargetSelected))
    }

    fun filesTargetSelected() {
        selectedTarget = ShareTarget.MY_FILES
        _events.postValue(Event(ShareExtensionTargetAction.FilesTargetSelected))
    }
}

enum class ShareTarget {
    ASSIGNMENT,
    MY_FILES
}
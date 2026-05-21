/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.instructure.ngc.features.coursehome

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Tab
import com.instructure.pandautils.domain.usecase.courses.LoadCourseUseCase
import com.instructure.pandautils.domain.usecase.courses.LoadCourseUseCaseParams
import com.instructure.pandautils.domain.usecase.pages.LoadCourseFrontPageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseHomeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loadCourseUseCase: LoadCourseUseCase,
    private val loadCourseFrontPageUseCase: LoadCourseFrontPageUseCase,
    private val crashlytics: FirebaseCrashlytics,
) : ViewModel() {

    private val courseId: Long = savedStateHandle.get<Long>(ARG_COURSE_ID) ?: 0L

    private val _uiState = MutableStateFlow(CourseHomeUiState())
    val uiState: StateFlow<CourseHomeUiState> = _uiState.asStateFlow()

    init {
        loadCourse()
    }

    fun onTabSelected(tab: CourseHomeTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    private fun loadCourse() {
        viewModelScope.launch {
            try {
                val course = loadCourseUseCase(
                    LoadCourseUseCaseParams(courseId, false)
                )
                val hasHomePageContent = isHomeTabAPage(course) && hasFrontPageContent()
                val availableTabs = buildAvailableTabs(hasHomePageContent)
                _uiState.update { state ->
                    val nextSelectedTab = state.selectedTab
                        .takeIf { it in availableTabs }
                        ?: availableTabs.first()
                    state.copy(
                        courseName = course.name,
                        courseImageUrl = course.imageUrl,
                        availableTabs = availableTabs,
                        selectedTab = nextSelectedTab,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                crashlytics.recordException(e)
                _uiState.update { it.copy(isLoading = false, isError = true) }
            }
        }
    }

    private suspend fun hasFrontPageContent(): Boolean {
        val page = loadFrontPageSafely() ?: return false
        return !page.body.isNullOrBlank()
    }

    private suspend fun loadFrontPageSafely() = try {
        loadCourseFrontPageUseCase(LoadCourseFrontPageUseCase.Params(courseId, false))
    } catch (e: Exception) {
        crashlytics.recordException(e)
        null
    }

    private fun isHomeTabAPage(course: Course): Boolean = course.homePageID == Tab.FRONT_PAGE_ID

    private fun buildAvailableTabs(hasHomePage: Boolean): List<CourseHomeTab> {
        return if (hasHomePage) {
            CourseHomeTab.entries
        } else {
            CourseHomeTab.entries.filterNot { it == CourseHomeTab.HOME }
        }
    }

    companion object {
        const val ARG_COURSE_ID = "courseId"
    }
}
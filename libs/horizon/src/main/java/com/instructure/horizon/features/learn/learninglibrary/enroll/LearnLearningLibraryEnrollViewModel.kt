/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.features.learn.learninglibrary.enroll

import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.navigation.LearnRoute
import com.instructure.horizon.horizonui.platform.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LearnLearningLibraryEnrollViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resources: Resources,
    private val repository: LearnLearningLibraryEnrollRepository
) : ViewModel() {
    private var learningLibraryItemId: String? = savedStateHandle.get<String>(LearnRoute.LearnLearningLibraryEnrollScreen.learningLibraryIdAttr)
    private var learningLibraryItem: LearningLibraryCollectionItem? = null

    private val _state = MutableStateFlow(
        LearnLearningLibraryEnrollState(
            onEnrollClicked = ::onEnroll,
            resetNavigateToCourseId = ::resetNavigateToCourseId,
            loadingState = LoadingState(
                isPullToRefreshEnabled = false
            )
        )
    )
    val state = _state.asStateFlow()

    init {
        learningLibraryItemId?.let { loadData(it) }
    }

    fun loadData(learningLibraryItemId: String) {
        this.learningLibraryItemId = learningLibraryItemId
        viewModelScope.tryLaunch {
            _state.update { it.copy(loadingState = it.loadingState.copy(isLoading = true)) }

            val collectionItem = repository.loadLearningLibraryItem(learningLibraryItemId)
            learningLibraryItem = collectionItem
            val courseDetails = repository.loadCourseDetails(collectionItem.canvasCourse!!.courseId.toLong())

            _state.update { it.copy(
                loadingState = it.loadingState.copy(isLoading = false),
                syllabus = courseDetails.courseSyllabus,
            ) }
        } catch {
            // Silent error
            _state.update { it.copy(loadingState = it.loadingState.copy(isLoading = false)) }
        }
    }

    private fun onEnroll() {
        viewModelScope.tryLaunch {
            _state.update { it.copy(isEnrollLoading = true) }
            repository.enrollLearningLibraryItem(learningLibraryItem!!.id)
            _state.update { it.copy(isEnrollLoading = false, navigateToCourseId = learningLibraryItem!!.canvasCourse!!.courseId.toLong()) }
        } catch {
            _state.update { it.copy(
                loadingState = it.loadingState.copy(
                    errorMessage = resources.getString(R.string.learnLearningLibraryEnrollDialogFailedToEnrollMessage)
                ),
                isEnrollLoading = false
            ) }
        }
    }

    private fun resetNavigateToCourseId() {
        _state.update { it.copy(navigateToCourseId = null) }
    }
}
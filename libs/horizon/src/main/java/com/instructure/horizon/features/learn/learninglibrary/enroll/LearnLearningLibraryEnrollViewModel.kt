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
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.domain.usecase.EnrollLearnLearningLibraryItemParams
import com.instructure.horizon.domain.usecase.EnrollLearnLearningLibraryItemUseCase
import com.instructure.horizon.domain.usecase.GetCourseWithProgressParams
import com.instructure.horizon.domain.usecase.GetCourseWithProgressUseCase
import com.instructure.horizon.domain.usecase.GetLastSyncedAtUseCase
import com.instructure.horizon.domain.usecase.GetLearnLearningLibraryItemParams
import com.instructure.horizon.domain.usecase.GetLearnLearningLibraryItemUseCase
import com.instructure.horizon.features.learn.LearnEvent
import com.instructure.horizon.features.learn.LearnEventHandler
import com.instructure.horizon.features.learn.navigation.LearnRoute
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.horizon.offline.HorizonOfflineViewModel
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LearnLearningLibraryEnrollViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resources: Resources,
    private val getLearnLearningLibraryItemUseCase: GetLearnLearningLibraryItemUseCase,
    private val getCourseWithProgressUseCase: GetCourseWithProgressUseCase,
    private val enrollLearnLearningLibraryItemUseCase: EnrollLearnLearningLibraryItemUseCase,
    private val eventHandler: LearnEventHandler,
    private val apiPrefs: ApiPrefs,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
    getLastSyncedAtUseCase: GetLastSyncedAtUseCase,
) : HorizonOfflineViewModel(networkStateProvider, featureFlagProvider, getLastSyncedAtUseCase) {
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

    override fun onNetworkRestored() {
        learningLibraryItemId?.let { loadData(it) }
    }

    override fun onNetworkLost() {
        // Offline banner is handled at the screen level
    }

    fun loadData(learningLibraryItemId: String) {
        this.learningLibraryItemId = learningLibraryItemId
        viewModelScope.tryLaunch {
            _state.update { it.copy(loadingState = it.loadingState.copy(isLoading = true)) }

            val collectionItem = getLearnLearningLibraryItemUseCase(
                GetLearnLearningLibraryItemParams(itemId = learningLibraryItemId, forceRefresh = false)
            )
            learningLibraryItem = collectionItem
            val syllabus = if (!isOffline()) {
                collectionItem.canvasCourse?.courseId?.toLongOrNull()?.let { courseId ->
                    runCatching {
                        getCourseWithProgressUseCase(
                            GetCourseWithProgressParams(courseId = courseId, userId = apiPrefs.user?.id ?: -1L)
                        ).courseSyllabus
                    }.getOrNull()
                }
            } else null

            _state.update { it.copy(
                loadingState = it.loadingState.copy(isLoading = false),
                syllabus = syllabus,
            ) }
        } catch {
            _state.update { it.copy(loadingState = it.loadingState.copy(isLoading = false)) }
        }
    }

    private fun onEnroll() {
        if (isOffline()) {
            _state.update { it.copy(
                loadingState = it.loadingState.copy(
                    snackbarMessage = resources.getString(R.string.learnLearningLibraryEnrollDialogFailedToEnrollMessage)
                )
            ) }
            return
        }
        viewModelScope.tryLaunch {
            _state.update { it.copy(isEnrollLoading = true) }
            enrollLearnLearningLibraryItemUseCase(EnrollLearnLearningLibraryItemParams(itemId = learningLibraryItem!!.id))
            _state.update { it.copy(isEnrollLoading = false, navigateToCourseId = learningLibraryItem!!.canvasCourse!!.courseId.toLong()) }
            eventHandler.postEvent(LearnEvent.RefreshLearningLibraryList)
        } catch {
            _state.update { it.copy(
                loadingState = it.loadingState.copy(
                    snackbarMessage = resources.getString(R.string.learnLearningLibraryEnrollDialogFailedToEnrollMessage)
                ),
                isEnrollLoading = false
            ) }
        }
    }

    private fun resetNavigateToCourseId() {
        _state.update { it.copy(navigateToCourseId = null) }
    }
}

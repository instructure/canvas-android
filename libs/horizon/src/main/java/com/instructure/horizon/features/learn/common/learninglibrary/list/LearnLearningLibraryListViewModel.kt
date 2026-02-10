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
package com.instructure.horizon.features.learn.common.learninglibrary.list

import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.horizonui.platform.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LearnLearningLibraryListViewModel @Inject constructor(
    private val repository: LearnLearningLibraryListRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(LearnLearningLibraryListUiState(
        loadingState = LoadingState(
            onRefresh = ::refreshData,
            onSnackbarDismiss = ::onDismissSnackbar
        ),
        updateSearchQuery = ::updateSearchQuery
    ))
    val uiState = _uiState

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = true)) }
            val result = fetchData()
            Log.d("LearnLearningLibraryListViewModel", "loadData: $result")
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false)) }
        } catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false, isError = true)) }
            Log.e("LearnLearningLibraryListViewModel", "loadData: $it")
        }
    }

    private fun refreshData() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = true)) }
            val result = fetchData(true)
            Log.d("LearnLearningLibraryListViewModel", "refreshData: $result")
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = true, isError = false)) }
        } catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = false, snackbarMessage = "Failed to load Learning Library")) }
            Log.e("LearnLearningLibraryListViewModel", "refreshData: $it")
        }
    }

    private suspend fun fetchData(forceNetwork: Boolean = false): List<EnrolledLearningLibraryCollection> {
        return repository.getEnrolledLearningLibraries(forceNetwork)
    }

    private fun updateSearchQuery(value: TextFieldValue) {
        _uiState.update { it.copy(searchQuery = value) }
    }

    private fun onDismissSnackbar() {
        _uiState.update { it.copy(loadingState = it.loadingState.copy(snackbarMessage = null)) }
    }
}
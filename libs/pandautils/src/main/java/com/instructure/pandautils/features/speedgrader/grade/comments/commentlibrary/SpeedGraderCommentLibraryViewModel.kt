/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.pandautils.features.speedgrader.grade.comments.commentlibrary

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.unaccent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


@HiltViewModel
class SpeedGraderCommentLibraryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SpeedGraderCommentLibraryRepository,
    private val apiPrefs: ApiPrefs
) : ViewModel() {

    private val initialCommentValue = savedStateHandle.get<String>(COMMENT_LIBRARY_INITIAL_COMMENT_VALUE_ROUTE_PARAM).orEmpty()

    private val _uiState = MutableStateFlow(
        SpeedGraderCommentLibraryUiState(
            commentValue = Uri.decode(initialCommentValue),
            onCommentValueChanged = ::onCommentValueChanged
        )
    )
    val uiState = _uiState.asStateFlow()

    private var allCommentLibraryItems: List<String> = emptyList()

    init {
        loadCommentLibraryItems()
    }

    private fun loadCommentLibraryItems() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(isLoading = true)
            }
            allCommentLibraryItems = repository.getCommentLibraryItems(apiPrefs.user?.id.orDefault())
            _uiState.update {
                it.copy(
                    isLoading = false,
                    items = filterCommentLibraryItems(it.commentValue)
                )
            }
        } catch {
            _uiState.update {
                it.copy(isLoading = false)
            }
        }
    }

    private fun onCommentValueChanged(newValue: String) {
        _uiState.update { currentState ->
            currentState.copy(
                commentValue = newValue,
                items = filterCommentLibraryItems(newValue)
            )
        }
    }

    private fun filterCommentLibraryItems(query: String): List<String> {
        return allCommentLibraryItems.filter {
            it.unaccent().contains(query.unaccent(), ignoreCase = true)
        }
    }
}

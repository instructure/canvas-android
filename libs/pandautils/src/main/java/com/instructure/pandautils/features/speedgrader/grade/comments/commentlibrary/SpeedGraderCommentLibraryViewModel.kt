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

import androidx.compose.ui.text.input.TextFieldValue
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
    private val repository: SpeedGraderCommentLibraryRepository,
    private val apiPrefs: ApiPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SpeedGraderCommentLibraryUiState(
            ::onCommentValueChanged
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
                    items = filterCommentLibraryItems(it.commentValue.text)
                )
            }
        } catch {
            _uiState.update {
                it.copy(isLoading = false)
            }
        }
    }

    private fun onCommentValueChanged(newValue: TextFieldValue) {
        _uiState.update { currentState ->
            currentState.copy(
                commentValue = newValue,
                items = filterCommentLibraryItems(newValue.text)
            )
        }
    }

    private fun filterCommentLibraryItems(query: String): List<String> {
        return allCommentLibraryItems.filter {
            it.unaccent().contains(query.unaccent(), ignoreCase = true)
        }
    }
}

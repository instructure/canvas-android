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
package com.instructure.pandautils.features.speedgrader.grade.rubric

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpeedGraderRubricViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SpeedGraderRubricRepository
) : ViewModel() {

    private val assignmentId: Long = savedStateHandle.get<Long>("assignmentId") ?: throw IllegalArgumentException("Missing assignmentId")
    private val userId: Long = savedStateHandle.get<Long>("submissionId") ?: throw IllegalArgumentException("Missing submissionId")

    private val _uiState = MutableStateFlow(SpeedGraderRubricUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadData()
        }
    }

    private suspend fun loadData() {
        _uiState.value = _uiState.value.copy(loading = true)
        try {
            val submission = repository.getRubrics(assignmentId, userId).submission
            val rubrics = submission?.assignment?.rubric
            val criterions = rubrics?.criteria?.map {
                RubricCriterion(
                    id = it._id,
                    longDescription = it.longDescription,
                    description = it.description,
                    points = it.points,
                    ratings = it.ratings?.map { rating ->
                        RubricRating(
                            id = rating._id,
                            description = rating.description,
                            points = rating.points,
                            longDescription = rating.longDescription,
                        )
                    } ?: emptyList()
                )
            }
            _uiState.value = _uiState.value.copy(
                loading = false,
                criterions = criterions ?: emptyList()
            )
        } catch (e: Exception) {
            // Handle exceptions, possibly update UI state to show error
        } finally {
            _uiState.value = _uiState.value.copy(loading = false)
        }
    }
}
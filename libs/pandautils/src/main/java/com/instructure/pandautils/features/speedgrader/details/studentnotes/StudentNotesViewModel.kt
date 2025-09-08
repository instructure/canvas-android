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

package com.instructure.pandautils.features.speedgrader.details.studentnotes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.features.speedgrader.content.SpeedGraderContentViewModel.Companion.STUDENT_ID_KEY
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


@HiltViewModel
class StudentNotesViewModel @Inject constructor(
    private val repository: StudentNotesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val courseId = savedStateHandle.get<Long>(Const.COURSE_ID) ?: -1
    private val studentId = savedStateHandle.get<Long>(STUDENT_ID_KEY) ?: -1

    private val _uiState = MutableStateFlow(StudentNotesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadStudentNotes()
    }

    private fun loadStudentNotes(forceRefresh: Boolean = false) {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(state = ScreenState.Loading) }

            val customColumns = repository.getCustomGradeBookColumns(
                courseId = courseId,
                forceRefresh = forceRefresh
            )

            val notes = customColumns
                .filter { it.teacherNotes && !it.hidden }
                .flatMap { column ->
                    val entries = repository.getCustomGradeBookColumnsEntries(
                        courseId = courseId,
                        columnId = column.id,
                        forceRefresh = forceRefresh
                    )

                    entries
                        .filter { it.userId == studentId }
                        .map {
                            StudentNote(
                                title = column.title,
                                description = it.content
                            )
                        }
                }

            _uiState.update {
                it.copy(
                    state = if (notes.isEmpty()) ScreenState.Empty else ScreenState.Content,
                    studentNotes = notes
                )
            }
        } catch {
            _uiState.update {
                it.copy(
                    state = ScreenState.Error,
                    studentNotes = emptyList(),
                    onRefresh = {
                        loadStudentNotes(true)
                    }
                )
            }
        }
    }
}

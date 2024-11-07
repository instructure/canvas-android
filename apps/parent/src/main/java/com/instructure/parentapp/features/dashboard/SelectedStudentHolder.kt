/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.features.dashboard


import com.instructure.canvasapi2.models.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

interface SelectedStudentHolder {
    val selectedStudentState: StateFlow<User?>
    val selectedStudentChangedFlow: SharedFlow<User>
    val selectedStudentColorChanged: SharedFlow<Unit>
    suspend fun updateSelectedStudent(user: User)
    suspend fun selectedStudentColorChanged()
}

class SelectedStudentHolderImpl : SelectedStudentHolder {
    private val _selectedStudentState = MutableStateFlow<User?>(null)
    override val selectedStudentState = _selectedStudentState.asStateFlow()

    private val _selectedStudentChangedFlow = MutableSharedFlow<User>()
    override val selectedStudentChangedFlow: SharedFlow<User> = _selectedStudentChangedFlow.asSharedFlow()

    private val _selectedStudentColorChanged = MutableSharedFlow<Unit>()
    override val selectedStudentColorChanged: SharedFlow<Unit> = _selectedStudentColorChanged.asSharedFlow()

    override suspend fun updateSelectedStudent(user: User) {
        _selectedStudentState.value = user
        _selectedStudentChangedFlow.emit(user)
    }

    override suspend fun selectedStudentColorChanged() {
        _selectedStudentColorChanged.emit(Unit)
    }
}

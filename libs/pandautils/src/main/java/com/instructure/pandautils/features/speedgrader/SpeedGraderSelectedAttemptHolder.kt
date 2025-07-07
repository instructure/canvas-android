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

package com.instructure.pandautils.features.speedgrader

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update


class SpeedGraderSelectedAttemptHolder {

    private val _selectedAttemptIds: MutableStateFlow<Map<Long, Long>> = MutableStateFlow(emptyMap())
    val selectedAttemptIds: StateFlow<Map<Long, Long>> = _selectedAttemptIds.asStateFlow()

    fun setSelectedAttemptId(studentId: Long, attemptId: Long?) {
        attemptId?.let {
            _selectedAttemptIds.update { currentMap ->
                currentMap + (studentId to it)
            }
        }
    }

    fun selectedAttemptIdFlowFor(studentId: Long): Flow<Long?> {
        return selectedAttemptIds.map { it[studentId] }.distinctUntilChanged()
    }
}

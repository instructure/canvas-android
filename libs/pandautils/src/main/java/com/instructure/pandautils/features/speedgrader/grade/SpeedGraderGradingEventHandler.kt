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
package com.instructure.pandautils.features.speedgrader.grade

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class GradingEvent {
    data object RubricUpdated : GradingEvent()
    data object PostPolicyUpdated : GradingEvent()
    data object GradeChanged : GradingEvent()
    data class GradeSaving(val studentId: Long) : GradingEvent()
    data class GradeSaved(val studentId: Long) : GradingEvent()
    data class GradeSaveFailed(val studentId: Long, val retry: () -> Unit) : GradingEvent()
}

@Singleton
class SpeedGraderGradingEventHandler @Inject constructor() {

    private val _events = MutableSharedFlow<GradingEvent>(replay = 0)
    val events = _events.asSharedFlow()

    suspend fun postEvent(event: GradingEvent) {
        _events.emit(event)
    }
}
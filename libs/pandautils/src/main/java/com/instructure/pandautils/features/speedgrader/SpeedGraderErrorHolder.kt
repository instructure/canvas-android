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
 */package com.instructure.pandautils.features.speedgrader

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

data class ErrorEvent(
    val message: String,
    val retryAction: (() -> Unit)? = null
)

@Singleton
class SpeedGraderErrorHolder @Inject constructor() {

    private val _events = MutableSharedFlow<ErrorEvent>(replay = 0)
    val events = _events.asSharedFlow()

    suspend fun postError(message: String, retryAction: (() -> Unit)? = null) {
        _events.emit(ErrorEvent(message, retryAction))
    }
}
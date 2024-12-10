/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.parentapp.features.dashboard

import com.instructure.canvasapi2.models.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow


class TestSelectStudentHolder(
    override val selectedStudentState: MutableStateFlow<User?>,
    override val selectedStudentChangedFlow: SharedFlow<User> = MutableSharedFlow(),
    override val selectedStudentColorChanged: SharedFlow<Unit> = MutableSharedFlow()
) : SelectedStudentHolder {
    override suspend fun updateSelectedStudent(user: User?) {
        selectedStudentState.emit(user)
    }

    override suspend fun selectedStudentColorChanged() {
        (selectedStudentColorChanged as MutableSharedFlow).emit(Unit)
    }
}

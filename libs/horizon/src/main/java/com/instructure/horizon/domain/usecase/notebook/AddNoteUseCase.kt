/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.horizon.domain.usecase.notebook

import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedData
import com.instructure.horizon.features.notebook.addedit.add.AddNoteRepository
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

class AddNoteUseCase @Inject constructor(
    private val repository: AddNoteRepository,
) : BaseUseCase<AddNoteUseCase.Params, Unit>() {

    data class Params(
        val courseId: String,
        val objectId: String,
        val objectType: String,
        val highlightedData: NoteHighlightedData,
        val userComment: String,
        val type: NotebookType?,
    )

    override suspend fun execute(params: Params) {
        repository.addNote(
            courseId = params.courseId,
            objectId = params.objectId,
            objectType = params.objectType,
            highlightedData = params.highlightedData,
            userComment = params.userComment,
            type = params.type,
        )
    }
}

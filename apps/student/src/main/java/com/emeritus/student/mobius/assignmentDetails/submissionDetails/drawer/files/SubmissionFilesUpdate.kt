/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.files

import com.emeritus.student.mobius.common.ui.UpdateInit
import com.spotify.mobius.First
import com.spotify.mobius.Next

class SubmissionFilesUpdate : UpdateInit<SubmissionFilesModel, SubmissionFilesEvent, SubmissionFilesEffect>() {
    override fun performInit(model: SubmissionFilesModel): First<SubmissionFilesModel, SubmissionFilesEffect> {
        return First.first(model)
    }

    override fun update(
        model: SubmissionFilesModel,
        event: SubmissionFilesEvent
    ): Next<SubmissionFilesModel, SubmissionFilesEffect> {
        return when (event) {
            is SubmissionFilesEvent.FileClicked -> {
                if (event.fileId == model.selectedFileId) {
                    Next.noChange()
                } else {
                    val file = model.files.first { it.id == event.fileId }
                    Next.next<SubmissionFilesModel, SubmissionFilesEffect>(
                        model.copy(selectedFileId = event.fileId),
                        setOf(SubmissionFilesEffect.BroadcastFileSelected(file))
                    )
                }
            }
        }
    }
}

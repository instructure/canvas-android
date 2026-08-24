/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.instructure.parentapp.features.notes.notebook

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.LearningObjectType
import com.instructure.pandautils.features.notes.notebook.NotebookRouter
import javax.inject.Inject

/** Parent doesn't host the Notebook (canvas-lms's Study Notes are student-only server-side) - a no-op
 * binding is only needed to satisfy [com.instructure.pandautils.features.notes.notebook.NotebookViewModel]'s
 * Hilt graph. */
class ParentNotebookRouter @Inject constructor() : NotebookRouter {

    override fun routeToLearningObject(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        learningObjectId: String,
        learningObjectType: LearningObjectType,
        noteId: String
    ) = Unit
}

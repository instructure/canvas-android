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
package com.instructure.pandautils.features.notes.notebook

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.LearningObjectType

/**
 * Navigates away from the Notebook to the page/assignment/quiz a note belongs to (tapping a note
 * row's trailing arrow) - implemented per app since it goes through each app's own RouteMatcher/
 * Fragment graph, exactly like [com.instructure.pandautils.features.dashboard.widget.todo.TodoWidgetRouter].
 * [activity] is passed per call rather than injected, since [NotebookViewModel] has no Activity of its
 * own. [canvasContext] must be a real, fetched [com.instructure.canvasapi2.models.Course] (not a bare-id
 * placeholder) - the destination screen's own student/enrollment checks (e.g. whether to fetch and show
 * Notebook highlights there) read it directly, and a placeholder with no enrollments would fail them.
 * [noteId] lets the destination scroll straight to that note's highlight once it renders.
 */
interface NotebookRouter {
    fun routeToLearningObject(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        learningObjectId: String,
        learningObjectType: LearningObjectType,
        noteId: String
    )
}

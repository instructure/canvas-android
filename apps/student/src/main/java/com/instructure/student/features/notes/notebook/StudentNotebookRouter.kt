/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.student.features.notes.notebook

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.LearningObjectType
import com.instructure.pandautils.features.notes.notebook.NotebookRouter
import com.instructure.student.features.pages.details.PageDetailsFragment
import com.instructure.student.router.RouteMatcher
import javax.inject.Inject

class StudentNotebookRouter @Inject constructor() : NotebookRouter {

    override fun routeToLearningObject(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        learningObjectId: String,
        learningObjectType: LearningObjectType,
        noteId: String
    ) {
        when (learningObjectType) {
            // canvas-lms's own Study Notes web UI doesn't yet support creating Assignment/Quiz notes
            // (backend-only for now) - nothing to route to until that exists.
            LearningObjectType.Assignment, LearningObjectType.Quiz -> Unit
            LearningObjectType.WikiPage -> RouteMatcher.route(
                activity,
                // A StudyNote's learningObjectId for a WikiPage is the page's numeric database id, not
                // its url slug (canvas-lms resolves notes by course.wiki_pages.find_by(id:), see
                // create_study_note.rb) - but Wiki#find_page (which backs the pages/:url_or_id REST
                // endpoint PageDetailsViewModel fetches through) only does an id lookup for a param of
                // the literal form "page_id:<id>"; a bare numeric string is treated as a url slug and
                // 404s. This prefix is a documented canvas-lms API convention (see
                // wiki_pages_api_controller.rb's "explicitly request by ID" comment), not a workaround.
                PageDetailsFragment.makeRoute(
                    canvasContext,
                    pageName = null,
                    pageUrl = "page_id:$learningObjectId",
                    navigatedFromModules = false,
                    noteId = noteId
                )
            )
        }
    }
}

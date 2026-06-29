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
package com.instructure.canvasapi2.managers.graphql

import com.instructure.canvasapi2.models.AssignmentGroup

interface GradesGraphQlManager {

    /**
     * Loads the assignment groups (with their assignments and the viewer's submission) for the Grades
     * screen via GraphQL. Assignments are fully depaginated before returning (no progressive render).
     *
     * @param gradingPeriodId when non-null, scopes assignments to that grading period; null = all periods.
     * @param paginateComments when true, fully depaginates each submission's comments (NGC shows a
     * comment pill); when false, only the first page fetched by the main query is kept (the comments
     * aren't shown, so the extra round-trips are skipped).
     */
    suspend fun getAssignmentGroupsWithAssignments(
        courseId: Long,
        gradingPeriodId: Long?,
        forceNetwork: Boolean,
        paginateComments: Boolean = false
    ): List<AssignmentGroup>
}

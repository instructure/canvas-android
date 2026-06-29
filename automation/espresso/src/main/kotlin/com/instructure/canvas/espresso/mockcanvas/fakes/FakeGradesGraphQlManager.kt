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

package com.instructure.canvas.espresso.mockcanvas.fakes

import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvasapi2.managers.graphql.GradesGraphQlManager
import com.instructure.canvasapi2.models.AssignmentGroup

/**
 * MockCanvas has no GraphQL endpoint, so the Grades screen's assignment-group loading is faked here by
 * reading the seeded REST data directly. This mirrors the mock REST `assignment_groups` endpoint: an
 * invalid grading period (id == -1) returns nothing, otherwise all of the course's assignment groups.
 */
class FakeGradesGraphQlManager : GradesGraphQlManager {

    override suspend fun getAssignmentGroupsWithAssignments(
        courseId: Long,
        gradingPeriodId: Long?,
        forceNetwork: Boolean,
        paginateComments: Boolean
    ): List<AssignmentGroup> {
        if (gradingPeriodId == -1L) return emptyList()
        return MockCanvas.data.assignmentGroups[courseId].orEmpty()
    }
}

/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
 */package com.instructure.canvas.espresso.mockCanvas.fakes

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvasapi2.AssignmentDetailsQuery
import com.instructure.canvasapi2.managers.AssignmentDetailsManager

class FakeAssignmentDetailsManager : AssignmentDetailsManager {

    override suspend fun getAssignmentDetails(assignmentId: Long): AssignmentDetailsQuery.Data {
        val assignment = MockCanvas.data.assignments[assignmentId]
        val course = MockCanvas.data.courses[assignment?.courseId]
        return AssignmentDetailsQuery.Data(
            AssignmentDetailsQuery.Assignment(
                AssignmentDetailsQuery.Course(
                    course?.name.orEmpty(),
                    course?.id?.toString().orEmpty()
                ),
                assignment?.name.orEmpty()
            )
        )
    }
}
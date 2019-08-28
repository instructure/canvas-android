/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.*
import com.instructure.canvasapi2.utils.weave.awaitQL

object PostPolicyManager {

    suspend fun hideGradesAsync(assignmentId: Long) = awaitQL<HideAssignmentGradesMutation.Data> {
        val mutation = HideAssignmentGradesMutation.builder()
            .assignmentId(assignmentId.toString())
            .build()

        it.enqueueMutation(mutation)
    }

    suspend fun hideGradesForSectionsAsync(
        assignmentId: Long,
        sections: List<String>
    ) = awaitQL<HideAssignmentGradesForSectionsMutation.Data> {
        val mutation = HideAssignmentGradesForSectionsMutation.builder()
            .assignmentId(assignmentId.toString())
            .sectionIds(sections)
            .build()

        it.enqueueMutation(mutation)
    }

    suspend fun postGradesAsync(
        assignmentId: Long,
        gradedOnly: Boolean
    ) = awaitQL<PostAssignmentGradesMutation.Data> {
        val mutation = PostAssignmentGradesMutation.builder()
            .assignmentId(assignmentId.toString())
            .gradedOnly(gradedOnly)
            .build()

        it.enqueueMutation(mutation)
    }

    suspend fun postGradesForSectionsAsync(
        assignmentId: Long,
        gradedOnly: Boolean,
        sections: List<String>
    ) = awaitQL<PostAssignmentGradesForSectionsMutation.Data> {
        val mutation = PostAssignmentGradesForSectionsMutation.builder()
            .assignmentId(assignmentId.toString())
            .gradedOnly(gradedOnly)
            .sectionIds(sections)
            .build()

        it.enqueueMutation(mutation)
    }
}

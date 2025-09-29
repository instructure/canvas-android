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
 */package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.HideAssignmentGradesForSectionsMutation
import com.instructure.canvasapi2.HideAssignmentGradesMutation
import com.instructure.canvasapi2.PostAssignmentGradesForSectionsMutation
import com.instructure.canvasapi2.PostAssignmentGradesMutation

interface PostPolicyManager {

    suspend fun hideGradesAsync(assignmentId: Long): HideAssignmentGradesMutation.Data

    suspend fun hideGradesForSectionsAsync(
        assignmentId: Long,
        sections: List<String>
    ): HideAssignmentGradesForSectionsMutation.Data

    suspend fun postGradesAsync(
        assignmentId: Long,
        gradedOnly: Boolean
    ): PostAssignmentGradesMutation.Data

    suspend fun postGradesForSectionsAsync(
        assignmentId: Long,
        gradedOnly: Boolean,
        sections: List<String>
    ): PostAssignmentGradesForSectionsMutation.Data

}
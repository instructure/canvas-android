/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.canvasapi2.models

import kotlinx.parcelize.Parcelize

@Parcelize
data class GradeableStudentSubmission(
        val assignee: Assignee,
        var submission: Submission? = null,
        var isCached: Boolean = false
) : CanvasModel<GradeableStudentSubmission>() {

    override val id get() = when (assignee) {
        is StudentAssignee -> assignee.student.id
        is GroupAssignee -> assignee.group.id
    }

    val assigneeId: Long
        get() = when (assignee) {
            is StudentAssignee -> assignee.student.id
            is GroupAssignee -> assignee.students.firstOrNull()?.id ?: assignee.group.id
        }

    override val comparisonDate get() = submission?.comparisonDate ?: when (assignee) {
        is StudentAssignee -> assignee.student.comparisonDate
        is GroupAssignee -> assignee.group.comparisonDate
    }

    override val comparisonString get() = submission?.comparisonString ?: when (assignee) {
        is StudentAssignee -> assignee.student.comparisonString
        is GroupAssignee -> assignee.group.comparisonString
    }
}

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
package com.instructure.canvasapi2.models.postmodels

import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Editing an assignment requires us to use a GSON serializer that serializes null. Because of this, we need to
 * make sure that we're not replacing existing values with null. So don't put a value here unless you
 * are setting it in the EditAssignmentDetailsFragment
 */
class AssignmentPostBody {
    var name: String? = null

    var description: String? = null

    @SerializedName("points_possible")
    var pointsPossible: Double? = null

    @SerializedName("grading_type")
    var gradingType: String? = null

    @SerializedName("due_at")
    var dueAt: String? = null

    @SerializedName("notify_of_update")
    var notifyOfUpdate: Boolean? = null

    @SerializedName("unlock_at")
    var unlockAt: String? = null

    @SerializedName("lock_at")
    var lockAt: String? = null

    var published: Boolean? = null

    @SerializedName("assignment_overrides")
    var assignmentOverrides: List<OverrideBody>? = null

    @SerializedName("only_visible_to_overrides")
    var isOnlyVisibleToOverrides: Boolean? = null

    @SerializedName("submission_types")
    var submissionTypes: List<String>? = null

    @SerializedName("muted")
    var isMuted: Boolean? = null
}

class OverrideBody {
    @SerializedName("assignment_id")
    var assignmentId: Long? = null

    @SerializedName("due_at")
    var dueAt: Date? = null

    @SerializedName("unlock_at")
    var unlockAt: Date? = null

    @SerializedName("lock_at")
    var lockAt: Date? = null

    @SerializedName("student_ids")
    var studentIds: LongArray? = null

    @SerializedName("course_section_id")
    var courseSectionId: Long? = null

    @SerializedName("group_id")
    var groupId: Long? = null
}

class AssignmentPostBodyWrapper {
    var assignment: AssignmentPostBody? = null
}

data class QuizAssignmentPostBody(
    @SerializedName("due_at")
    val dueAt: String? = null,
    @SerializedName("notify_of_update")
    val notifyOfUpdate: Boolean? = null,
    @SerializedName("unlock_at")
    val unlockAt: String? = null,
    @SerializedName("lock_at")
    val lockAt: String? = null,
    val published: Boolean? = null,
    @SerializedName("assignment_overrides")
    val assignmentOverrides: List<OverrideBody>? = null,
    @SerializedName("only_visible_to_overrides")
    val isOnlyVisibleToOverrides: Boolean? = null
)

class QuizAssignmentPostBodyWrapper {
    var assignment: QuizAssignmentPostBody? = null
}

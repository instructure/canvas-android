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

import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.*

@JvmSuppressWildcards
@Parcelize
data class Submission(
        override var id: Long = 0,
        val grade: String? = null,
        val score: Double = 0.0,
        val attempt: Long = 0,
        @SerializedName("submitted_at")
        var submittedAt: Date? = null,
        @SerializedName("submission_comments")
        var submissionComments: List<SubmissionComment> = ArrayList(),
        val commentCreated: Date? = null,
        val mediaContentType: String? = null,
        val mediaCommentUrl: String? = null,
        val mediaCommentDisplay: String? = null,
        @SerializedName("submission_history")
        val submissionHistory: List<Submission?> = ArrayList(),
        val attachments: ArrayList<Attachment> = arrayListOf(),
        val body: String? = null,
        @SerializedName("rubric_assessment")
        var rubricAssessment: HashMap<String, RubricCriterionAssessment> = hashMapOf(),
        @SerializedName("grade_matches_current_submission")
        val isGradeMatchesCurrentSubmission: Boolean = false,
        @SerializedName("workflow_state")
        val workflowState: String? = null,
        @SerializedName("submission_type")
        val submissionType: String? = null,
        @SerializedName("preview_url")
        var previewUrl: String? = null,
        val url: String? = null,
        @SerializedName("late")
        val late: Boolean = false,
        @SerializedName("excused")
        val excused: Boolean = false,
        val missing: Boolean = false,
        @SerializedName("media_comment")
        val mediaComment: MediaComment? = null,
        // Conversation Stuff
        @SerializedName("assignment_id")
        val assignmentId: Long = 0,
        var assignment: Assignment? = null,
        @SerializedName("user_id")
        val userId: Long = 0,
        @SerializedName("grader_id")
        val graderId: Long = 0,
        val user: User? = null,
        // This value could be null. Currently will only be returned when getting the submission for
        // a user when the submission_type is discussion_topic
        @SerializedName("discussion_entries")
        val discussionEntries: ArrayList<DiscussionEntry> = arrayListOf(),
        // Group Info only available when including groups in the Submissions#index endpoint
        val group: Group? = null,
        @SerializedName("points_deducted")
        val pointsDeducted: Double? = null,
        @SerializedName("entered_score")
        val enteredScore: Double = 0.0,
        @SerializedName("entered_grade")
        val enteredGrade: String? = null,
        @SerializedName("posted_at")
        val postedAt: Date? = null,
        @SerializedName("grading_period_id")
        val gradingPeriodId: Long? = null
) : CanvasModel<Submission>() {
    override val comparisonDate get() = submittedAt
    override val comparisonString get() = submissionType

    val isWithoutGradedSubmission: Boolean get() = !isGraded && submissionType == null
    val isGraded: Boolean get() = grade != null

    /* Submissions will have dummy submissions if they grade an assignment with no actual submissions. We want to see if any are not dummy submissions */
    fun hasRealSubmission() = submissionHistory.any { it?.submissionType != null }
}

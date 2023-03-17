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
import com.instructure.canvasapi2.utils.toDate
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuizSubmission(
        override val id: Long = 0,

        @SerializedName("quiz_id")
        val quizId: Long = 0,

        // The ID of the Student that made the quiz submission.
        @SerializedName("user_id")
        val userId: Long = 0,

        @SerializedName("submission_id")
        val submissionId: Long = 0,

        @SerializedName("started_at")
        val startedAt: String? = null,

        @SerializedName("finished_at")
        val finishedAt: String? = null,

        // The time at which the quiz submission will be overdue, and be flagged as a late
        // submission.
        @SerializedName("end_at")
        val endAt: String? = null,

        // For quizzes that allow multiple attempts, this field specifies the quiz
        // submission attempt number.
        val attempt: Int = 0,

        // Number of times the student was allowed to re-take the quiz over the
        // multiple-attempt limit.
        @SerializedName("extra_attempts")
        val extraAttempts: Int = 0,

        // The number of attempts left. Note: the quiz object does not get updated with this information
        // in the allowed_attempts field.
        @SerializedName("attempts_left")
        val attemptsLeft: Int = 0,

        // Amount of extra time allowed for the quiz submission, in minutes.
        @SerializedName("extra_time")
        val extraTime: Int = 0,

        // The student can take the quiz even if it's Locked for everyone else
        @SerializedName("manually_unlocked")
        val manuallyUnlocked: Boolean = false,

        // Amount of time spent, in seconds.
        @SerializedName("time_spent")
        val timeSpent: Int = 0,

        // The score of the quiz submission, if graded.
        val score: Double = 0.0,

        @SerializedName("score_before_regrade")
        val scoreBeforeRegrade: Double = 0.0,

        // For quizzes that allow multiple attempts, this is the score that will be used,
        // which might be the score of the latest, or the highest, quiz submission.
        @SerializedName("kept_score")
        val keptScore: Double = 0.0,

        // Number of points the quiz submission's score was fudged by.
        @SerializedName("fudge_points")
        val fudgePoints: Double = 0.0,

        @SerializedName("has_seen_results")
        val hasSeenResults: Boolean = false,

        // The current state of the quiz submission. Possible values:
        // ['untaken'|'pending_review'|'complete'|'settings_only'|'preview'].
        @SerializedName("workflow_state")
        val workflowState: String? = null,

        @SerializedName("quiz_points_possible")
        val quizPointsPossible: Double = 0.0,

        // Token used to validate quiz answers when posting
        @SerializedName("validation_token")
        val validationToken: String? = null,

        @SerializedName("overdue_and_needs_submission")
        val overDueAndNeedsSubmission: Boolean = false
) : CanvasModel<QuizSubmission>() {
    override val comparisonDate get() = finishedAt.toDate()

    val finishedDate get() = finishedAt.toDate()
    val startedDate get() = startedAt.toDate()

    enum class WorkflowState {
        UNTAKEN, COMPLETE, PENDING_REVIEW, PREVIEW, SETTINGS_ONLY, UNKNOWN
    }

    companion object {
        fun parseWorkflowState(workflowState: String): WorkflowState {
            when (workflowState) {
                "untaken" -> return WorkflowState.UNTAKEN
                "complete" -> return WorkflowState.COMPLETE
                "preview" -> return WorkflowState.PREVIEW
                "settings_only" -> return WorkflowState.SETTINGS_ONLY
                "pending_review" -> return WorkflowState.PENDING_REVIEW
            }

            return WorkflowState.UNKNOWN
        }
    }
}

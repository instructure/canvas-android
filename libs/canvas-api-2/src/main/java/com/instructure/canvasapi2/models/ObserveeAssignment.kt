/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.canvasapi2.models

import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.utils.toDate
import kotlinx.parcelize.Parcelize

/**
 * This assignment model is a copy of the Assignment.kt model, with one change. Its 'submission' property is a list,
 * instead of a single object. This change is necessary to handle the api response for getAssignmentIncludeObservees,
 * due to the addition of `include[]=observed_users`.
 */
@Parcelize
data class ObserveeAssignment(
        override var id: Long = 0,
        var name: String? = null,
        val description: String? = null,
        @SerializedName("submission_types")
        val submissionTypesRaw: List<String> = arrayListOf(),
        @SerializedName("due_at")
        val dueAt: String? = null,
        @SerializedName("points_possible")
        val pointsPossible: Double = 0.0,
        @SerializedName("course_id")
        val courseId: Long = 0,
        @SerializedName("grade_group_students_individually")
        val isGradeGroupsIndividually: Boolean = false,
        @SerializedName("grading_type")
        val gradingType: String? = null,
        @SerializedName("needs_grading_count")
        val needsGradingCount: Long = 0,
        @SerializedName("html_url")
        val htmlUrl: String? = null,
        val url: String? = null,
        @SerializedName("quiz_id")
        val quizId: Long = 0, // (Optional) id of the associated quiz (applies only when submission_types is ["online_quiz"])
        val rubric: List<RubricCriterion>? = arrayListOf(),
        @SerializedName("use_rubric_for_grading")
        val isUseRubricForGrading: Boolean = false,
        @SerializedName("rubric_settings")
        val rubricSettings: RubricSettings? = null,
        @SerializedName("allowed_extensions")
        val allowedExtensions: List<String> = arrayListOf(),
        @SerializedName("submission")
        var submissionList: List<Submission>? = null,
        @SerializedName("assignment_group_id")
        val assignmentGroupId: Long = 0,
        val position: Int = 0,
        @SerializedName("peer_reviews")
        val isPeerReviews: Boolean = false,
        @SerializedName("lock_info") // Module lock info
        val lockInfo: LockInfo? = null,
        @SerializedName("locked_for_user")
        val lockedForUser: Boolean = false,
        @SerializedName("lock_at")
        val lockAt: String? = null, // Date the teacher no longer accepts submissions.
        @SerializedName("unlock_at")
        val unlockAt: String? = null,
        @SerializedName("lock_explanation")
        val lockExplanation: String? = null,
        @SerializedName("discussion_topic")
        var discussionTopicHeader: DiscussionTopicHeader? = null,
        @SerializedName("needs_grading_count_by_section")
        val needsGradingCountBySection: List<NeedsGradingCount> = arrayListOf(),
        @SerializedName("free_form_criterion_comments")
        val freeFormCriterionComments: Boolean = false,
        val published: Boolean = false,
        @SerializedName("group_category_id")
        val groupCategoryId: Long = 0,
        @SerializedName("all_dates")
        val allDates: List<AssignmentDueDate> = arrayListOf(),
        @SerializedName("user_submitted")
        val userSubmitted: Boolean = false,
        val unpublishable: Boolean = false,
        val overrides: List<AssignmentOverride>? = null,
        @SerializedName("only_visible_to_overrides")
        val onlyVisibleToOverrides: Boolean = false,
        @SerializedName("anonymous_peer_reviews")
        val anonymousPeerReviews: Boolean = false,
        @SerializedName("moderated_grading")
        val moderatedGrading: Boolean = false,
        @SerializedName("anonymous_grading")
        val anonymousGrading: Boolean = false,
        @SerializedName("allowed_attempts")
        val allowedAttempts: Long = -1, // API gives -1 for unlimited submissions
        @SerializedName("external_tool_tag_attributes")
        val externalToolAttributes: ExternalToolAttributes? = null,
        var isStudioEnabled: Boolean = false,
        @SerializedName("hide_in_gradebook")
        val isHiddenInGradeBook: Boolean = false,
        val checkpoints: List<Checkpoint> = emptyList()
) : CanvasModel<Assignment>() {
    override val comparisonDate get() = dueAt.toDate()
    override val comparisonString get() = dueAt

    /**
     * Converts an ObserveeAssignment to an Assignment, using the first submission found. Returns null if no submission
     * is found.
     */
    fun toAssignmentForObservee(): Assignment? {
        val submission = submissionList?.firstOrNull()
        if (submission != null) {
            return Assignment(
                id = this.id,
                name = this.name,
                description = this.description,
                submissionTypesRaw = this.submissionTypesRaw,
                dueAt = this.dueAt,
                pointsPossible = this.pointsPossible,
                courseId = this.courseId,
                isGradeGroupsIndividually = this.isGradeGroupsIndividually,
                gradingType = this.gradingType,
                needsGradingCount = this.needsGradingCount,
                htmlUrl = this.htmlUrl,
                url = this.url,
                quizId = this.quizId,
                rubric = this.rubric,
                isUseRubricForGrading = this.isUseRubricForGrading,
                rubricSettings = this.rubricSettings,
                allowedExtensions = this.allowedExtensions,
                submission = submission,
                assignmentGroupId = this.assignmentGroupId,
                position = this.position,
                isPeerReviews = this.isPeerReviews,
                lockInfo = this.lockInfo,
                lockedForUser = this.lockedForUser,
                lockAt = this.lockAt,
                unlockAt = this.unlockAt,
                lockExplanation = this.lockExplanation,
                discussionTopicHeader = this.discussionTopicHeader,
                needsGradingCountBySection = this.needsGradingCountBySection,
                freeFormCriterionComments = this.freeFormCriterionComments,
                published = this.published,
                groupCategoryId = this.groupCategoryId,
                allDates = this.allDates,
                userSubmitted = this.userSubmitted,
                unpublishable = this.unpublishable,
                overrides = this.overrides,
                onlyVisibleToOverrides = this.onlyVisibleToOverrides,
                anonymousPeerReviews = this.anonymousPeerReviews,
                moderatedGrading = this.moderatedGrading,
                anonymousGrading = this.anonymousGrading,
                allowedAttempts = this.allowedAttempts,
                isStudioEnabled = this.isStudioEnabled,
                isHiddenInGradeBook = this.isHiddenInGradeBook,
                checkpoints = this.checkpoints
            )
        } else {
            return null
        }
    }

    fun toAssignment(studentId: Long) = Assignment(
        id = id,
        name = name,
        description = description,
        submissionTypesRaw = submissionTypesRaw,
        dueAt = dueAt,
        pointsPossible = pointsPossible,
        courseId = courseId,
        isGradeGroupsIndividually = isGradeGroupsIndividually,
        gradingType = gradingType,
        needsGradingCount = needsGradingCount,
        htmlUrl = htmlUrl,
        url = url,
        quizId = quizId,
        rubric = rubric,
        isUseRubricForGrading = isUseRubricForGrading,
        rubricSettings = rubricSettings,
        allowedExtensions = allowedExtensions,
        submission = submissionList?.firstOrNull { submission ->
            submission.userId == studentId
        },
        assignmentGroupId = assignmentGroupId,
        position = position,
        isPeerReviews = isPeerReviews,
        lockInfo = lockInfo,
        lockedForUser = lockedForUser,
        lockAt = lockAt,
        unlockAt = unlockAt,
        lockExplanation = lockExplanation,
        discussionTopicHeader = discussionTopicHeader,
        needsGradingCountBySection = needsGradingCountBySection,
        freeFormCriterionComments = freeFormCriterionComments,
        published = published,
        groupCategoryId = groupCategoryId,
        allDates = allDates,
        userSubmitted = userSubmitted,
        unpublishable = unpublishable,
        overrides = overrides,
        onlyVisibleToOverrides = onlyVisibleToOverrides,
        anonymousPeerReviews = anonymousPeerReviews,
        moderatedGrading = moderatedGrading,
        anonymousGrading = anonymousGrading,
        allowedAttempts = allowedAttempts,
        externalToolAttributes = externalToolAttributes,
        isStudioEnabled = isStudioEnabled,
        isHiddenInGradeBook = isHiddenInGradeBook,
        checkpoints = checkpoints
    )
}
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

import android.content.Context
import android.content.res.Resources
import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.R
import com.instructure.canvasapi2.utils.toDate
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.Locale

@Parcelize
data class Assignment(
        override var id: Long = 0,
        var name: String? = null,
        var description: String? = null,
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
        var submission: Submission? = null,
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
        @SerializedName("score_statistics")
        val scoreStatistics: AssignmentScoreStatistics? = null,
        @SerializedName("allowed_attempts")
        val allowedAttempts: Long = -1, // API gives -1 for unlimited submissions
        @SerializedName("external_tool_tag_attributes")
        val externalToolAttributes: ExternalToolAttributes? = null,
        @SerializedName("planner_override")
        val plannerOverride: PlannerOverride? = null,
        var isStudioEnabled: Boolean = false,
        @SerializedName("in_closed_grading_period")
        val inClosedGradingPeriod: Boolean = false,
        @SerializedName("annotatable_attachment_id")
        val annotatableAttachmentId: Long = 0,
        // For quizzes we need to use this field instead of anonymous_grading to determine if it's anonymous
        @SerializedName("anonymous_submissions")
        val anonymousSubmissions: Boolean = false,
        @SerializedName("omit_from_final_grade")
        val omitFromFinalGrade: Boolean = false,
        @SerializedName("hide_in_gradebook")
        val isHiddenInGradeBook: Boolean = false
) : CanvasModel<Assignment>() {
    override val comparisonDate get() = dueDate
    override val comparisonString get() = dueAt

    val lastActualSubmission: Submission? get() = submission?.takeIf { it.workflowState == "submitted" }

    /**
     * Whether or not the user has submitted this assignment. If the user has not submitted anything, Canvas generates
     * an empty submission with a null value for "submittedAt". For very old assignments, canvas might not
     * return a submission at all.
     */
    val isSubmitted: Boolean get() {
        return if (turnInType == TurnInType.NONE || turnInType == TurnInType.ON_PAPER) {
            !(submission?.missing ?: false)
        } else {
            submission?.submittedAt != null
        }
    }

    val isAllowedToSubmit: Boolean
        get() {
            val submissionTypes = getSubmissionTypes()
            return expectsSubmissions() && !lockedForUser && !submissionTypes.contains(SubmissionType.ONLINE_QUIZ) && !submissionTypes.contains(SubmissionType.ATTENDANCE)
        }

    val isWithoutGradedSubmission: Boolean
        get() {
            val submission = lastActualSubmission
            return submission == null || submission.isWithoutGradedSubmission
        }

    val isOnlineSubmissionType: Boolean get() = submissionTypesRaw.any { it in onlineSubmissionTypes }

    val turnInType: TurnInType
        get() = turnInTypeFromSubmissionType(getSubmissionTypes())

    val isLocked: Boolean
        get() {
            val currentDate = Date()
            return when {
                lockInfo == null || lockInfo.isEmpty -> false
                lockInfo.lockedModuleName != null && lockInfo.lockedModuleName!!.isNotEmpty() && lockInfo.lockedModuleName != "null" -> true
                lockInfo.unlockDate != null && lockInfo.unlockDate!!.after(currentDate) -> true
                else -> false
            }

        }

    val isGradingTypeQuantitative: Boolean
        get() {
            val gradingType = getGradingTypeFromAPIString(this.gradingType ?: "")
            return gradingType == GradingType.PERCENT || gradingType == GradingType.POINTS
        }

    enum class SubmissionType(val apiString: String) {
        ONLINE_QUIZ("online_quiz"),
        NONE("none"),
        ON_PAPER("on_paper"),
        DISCUSSION_TOPIC("discussion_topic"),
        EXTERNAL_TOOL("external_tool"), // External tool and Basic LTI launch use the same api string
        BASIC_LTI_LAUNCH("basic_lti_launch"),
        ONLINE_UPLOAD("online_upload"),
        ONLINE_TEXT_ENTRY("online_text_entry"),
        ONLINE_URL("online_url"),
        MEDIA_RECORDING("media_recording"),
        ATTENDANCE("attendance"),
        NOT_GRADED("not_graded"),
        STUDENT_ANNOTATION("student_annotation")
    }

    enum class GradingType {
        PASS_FAIL, PERCENT, LETTER_GRADE, POINTS, GPA_SCALE, NOT_GRADED
    }

    enum class TurnInType {
        ONLINE, ON_PAPER, NONE, DISCUSSION, QUIZ, EXTERNAL_TOOL
    }

    private fun expectsSubmissions(): Boolean {
        val submissionTypes = getSubmissionTypes()
        return submissionTypes.isNotEmpty() && !submissionTypes.contains(SubmissionType.NONE) && !submissionTypes.contains(SubmissionType.NOT_GRADED) && !submissionTypes.contains(SubmissionType.ON_PAPER)
    }

    private fun turnInTypeFromSubmissionType(submissionTypes: List<SubmissionType>?): TurnInType {
        if (submissionTypes == null || submissionTypes.isEmpty()) {
            return TurnInType.NONE
        }

        val submissionType = submissionTypes[0]

        return when (submissionType) {
            SubmissionType.MEDIA_RECORDING, SubmissionType.ONLINE_TEXT_ENTRY, SubmissionType.ONLINE_URL, SubmissionType.ONLINE_UPLOAD, SubmissionType.STUDENT_ANNOTATION -> TurnInType.ONLINE
            SubmissionType.ONLINE_QUIZ -> TurnInType.QUIZ
            SubmissionType.DISCUSSION_TOPIC -> TurnInType.DISCUSSION
            SubmissionType.ON_PAPER -> TurnInType.ON_PAPER
            SubmissionType.EXTERNAL_TOOL -> TurnInType.EXTERNAL_TOOL
            SubmissionType.BASIC_LTI_LAUNCH -> TurnInType.EXTERNAL_TOOL
            else -> TurnInType.NONE
        }

    }

    fun hasRubric(): Boolean = rubric?.isNotEmpty() ?: false

    fun getSubmissionTypes(): List<SubmissionType> =
        submissionTypesRaw.map {
            getSubmissionTypeFromAPIString(it)
        }

    val dueDate: Date? get() = dueAt.toDate()
    val lockDate: Date? get() = lockAt.toDate()
    val unlockDate: Date? get() = unlockAt.toDate()

    override fun describeContents(): Int = 0

    fun isMissing(): Boolean {
        return submission?.missing == true || (!isSubmitted && dueDate?.before(Date()) ?: false && submission?.grade == null)
    }

    fun isGraded(): Boolean {
        return (submission?.grade != null && submission?.workflowState != "pending_review" && submission?.postedAt != null)
    }

    fun isNewQuizLti(): Boolean {
        return submissionTypesRaw.contains(SubmissionType.EXTERNAL_TOOL.apiString) && externalToolAttributes?.url?.contains("quiz-lti") == true
    }

    companion object {

        const val PASS_FAIL_TYPE = "pass_fail"
        const val PERCENT_TYPE = "percent"
        const val LETTER_GRADE_TYPE = "letter_grade"
        const val POINTS_TYPE = "points"
        const val GPA_SCALE_TYPE = "gpa_scale"
        const val NOT_GRADED_TYPE = "not_graded"

        val onlineSubmissionTypes = listOf(
            SubmissionType.ONLINE_TEXT_ENTRY.apiString,
            SubmissionType.ONLINE_URL.apiString,
            SubmissionType.MEDIA_RECORDING.apiString,
            SubmissionType.ONLINE_UPLOAD.apiString,
            SubmissionType.ONLINE_QUIZ.apiString,
            SubmissionType.EXTERNAL_TOOL.apiString,
            SubmissionType.BASIC_LTI_LAUNCH.apiString,
            SubmissionType.DISCUSSION_TOPIC.apiString
        )

        fun turnInTypeToPrettyPrintString(turnInType: TurnInType?, context: Context): String? {
            turnInType ?: return null

            return when (turnInType) {
                TurnInType.ONLINE -> context.getString(R.string.canvasAPI_online)
                TurnInType.ON_PAPER -> context.getString(R.string.canvasAPI_onPaper)
                TurnInType.NONE -> context.getString(R.string.canvasAPI_none)
                TurnInType.DISCUSSION -> context.getString(R.string.canvasAPI_discussion)
                TurnInType.QUIZ -> context.getString(R.string.canvasAPI_quiz)
                TurnInType.EXTERNAL_TOOL -> context.getString(R.string.canvasAPI_externalTool)
            }
        }

        fun getSubmissionTypeFromAPIString(submissionType: String?): SubmissionType = when (submissionType) {
                    "online_quiz" -> SubmissionType.ONLINE_QUIZ
                    "none" -> SubmissionType.NONE
                    "on_paper" -> SubmissionType.ON_PAPER
                    "discussion_topic" -> SubmissionType.DISCUSSION_TOPIC
                    "external_tool" -> SubmissionType.EXTERNAL_TOOL
                    "basic_lti_launch" -> SubmissionType.BASIC_LTI_LAUNCH
                    "online_upload" -> SubmissionType.ONLINE_UPLOAD
                    "online_text_entry" -> SubmissionType.ONLINE_TEXT_ENTRY
                    "online_url" -> SubmissionType.ONLINE_URL
                    "media_recording" -> SubmissionType.MEDIA_RECORDING
                    "attendance" -> SubmissionType.ATTENDANCE
                    "not_graded" -> SubmissionType.NOT_GRADED
                    "student_annotation" -> SubmissionType.STUDENT_ANNOTATION
                    else -> SubmissionType.NONE
                }

        fun submissionTypeToAPIString(submissionType: SubmissionType?): String = submissionType?.name?.lowercase(
            Locale.getDefault()
        )
            ?: ""

        fun submissionTypeStringToPrettyPrintString(submissionType: String?, context: Context): String? =
                submissionTypeToPrettyPrintString(getSubmissionTypeFromAPIString(submissionType), context)

        fun submissionTypeToPrettyPrintString(submissionType: SubmissionType?, context: Context): String? {
            return submissionTypeToPrettyPrintString(submissionType, context.resources)
        }

        fun submissionTypeToPrettyPrintString(submissionType: SubmissionType?, resources: Resources): String? {
            submissionType ?: return null

            return when (submissionType) {
                SubmissionType.ONLINE_QUIZ -> resources.getString(R.string.canvasAPI_onlineQuiz)
                SubmissionType.NONE -> resources.getString(R.string.canvasAPI_none)
                SubmissionType.ON_PAPER -> resources.getString(R.string.canvasAPI_onPaper)
                SubmissionType.DISCUSSION_TOPIC -> resources.getString(R.string.canvasAPI_discussionTopic)
                SubmissionType.EXTERNAL_TOOL, SubmissionType.BASIC_LTI_LAUNCH -> resources.getString(R.string.canvasAPI_externalTool)
                SubmissionType.ONLINE_UPLOAD -> resources.getString(R.string.canvasAPI_onlineUpload)
                SubmissionType.ONLINE_TEXT_ENTRY -> resources.getString(R.string.canvasAPI_onlineTextEntry)
                SubmissionType.ONLINE_URL -> resources.getString(R.string.canvasAPI_onlineURL)
                SubmissionType.MEDIA_RECORDING -> resources.getString(R.string.canvasAPI_mediaRecording)
                SubmissionType.ATTENDANCE -> resources.getString(R.string.canvasAPI_attendance)
                SubmissionType.NOT_GRADED -> resources.getString(R.string.canvasAPI_notGraded)
                SubmissionType.STUDENT_ANNOTATION -> resources.getString(R.string.canvasAPI_studentAnnotation)
            }
        }

        fun getGradingTypeFromString(gradingType: String, context: Context): GradingType? =
                when (gradingType) {
                    "pass_fail", context.getString(R.string.canvasAPI_passFail) -> GradingType.PASS_FAIL
                    "percent", context.getString(R.string.canvasAPI_percent) -> GradingType.PERCENT
                    "letter_grade", context.getString(R.string.canvasAPI_letterGrade) -> GradingType.LETTER_GRADE
                    "points", context.getString(R.string.canvasAPI_points) -> GradingType.POINTS
                    "gpa_scale", context.getString(R.string.canvasAPI_gpaScale) -> GradingType.GPA_SCALE
                    "not_graded", context.getString(R.string.canvasAPI_notGraded) -> GradingType.NOT_GRADED
                    else -> null
                }

        fun getGradingTypeFromAPIString(gradingType: String): GradingType? = when (gradingType) {
            "pass_fail" -> GradingType.PASS_FAIL
            "percent" -> GradingType.PERCENT
            "letter_grade" -> GradingType.LETTER_GRADE
            "points" -> GradingType.POINTS
            "gpa_scale" -> GradingType.GPA_SCALE
            "not_graded" -> GradingType.NOT_GRADED
            else -> null
        }

        fun gradingTypeToAPIString(gradingType: GradingType?): String? = gradingType?.name?.lowercase(
            Locale.getDefault()
        )

        fun gradingTypeToPrettyPrintString(gradingType: String, context: Context): String? =
                gradingTypeToPrettyPrintString(getGradingTypeFromAPIString(gradingType), context)

        @Suppress("MemberVisibilityCanBePrivate")
        fun gradingTypeToPrettyPrintString(gradingType: GradingType?, context: Context): String? {
            gradingType ?: return null

            return when (gradingType) {
                GradingType.PASS_FAIL -> context.getString(R.string.canvasAPI_passFail)
                GradingType.PERCENT -> context.getString(R.string.canvasAPI_percent)
                GradingType.LETTER_GRADE -> context.getString(R.string.canvasAPI_letterGrade)
                GradingType.POINTS -> context.getString(R.string.canvasAPI_points)
                GradingType.GPA_SCALE -> context.getString(R.string.canvasAPI_gpaScale)
                GradingType.NOT_GRADED -> context.getString(R.string.canvasAPI_notGraded)
            }
        }
    }
}

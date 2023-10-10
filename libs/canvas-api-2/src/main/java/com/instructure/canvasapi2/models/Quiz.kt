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
import com.instructure.canvasapi2.R
import com.instructure.canvasapi2.utils.NaturalOrderComparator
import com.instructure.canvasapi2.utils.toDate
import kotlinx.parcelize.Parcelize
import java.util.*


@Parcelize
data class Quiz(
        override val id: Long = 0,
        val title: String? = null,
        @SerializedName("mobile_url")
        val mobileUrl: String? = null,
        @SerializedName("html_url")
        val htmlUrl: String? = null,
        var description: String? = "",
        @SerializedName("quiz_type")
        val quizType: String? = null,
        @SerializedName("assignment_group_id")
        val assignmentGroupId: Long = 0,
        @SerializedName("lock_info")
        val lockInfo: LockInfo? = null,
        val permissions: QuizPermission? = null,
        @SerializedName("allowed_attempts")
        val allowedAttempts: Int = 0,
        @SerializedName("question_count")
        val questionCount: Int = 0,
        @SerializedName("points_possible")
        val pointsPossible: String? = null,
        @SerializedName("cant_go_back")
        val isLockQuestionsAfterAnswering: Boolean = false,
        @SerializedName("due_at")
        val dueAt: String? = null,
        @SerializedName("time_limit")
        val timeLimit: Int = 0,
        @SerializedName("shuffle_answers")
        val shuffleAnswers: Boolean = false,
        @SerializedName("show_correct_answers")
        val showCorrectAnswers: Boolean = false,
        @SerializedName("scoring_policy")
        val scoringPolicy: String? = null,
        @SerializedName("access_code")
        val accessCode: String? = null,
        @SerializedName("ip_filter")
        val ipFilter: String? = null,
        @SerializedName("locked_for_user")
        val lockedForUser: Boolean = false,
        @SerializedName("lock_explanation")
        val lockExplanation: String? = null,
        @SerializedName("hide_results")
        val hideResults: String? = null,
        @SerializedName("show_correct_answers_at")
        val showCorrectAnswersAt: String? = null,
        @SerializedName("hide_correct_answers_at")
        val hideCorrectAnswersAt: String? = null,
        @SerializedName("unlock_at")
        val unlockAt: String? = null,
        @SerializedName("one_time_results")
        val oneTimeResults: Boolean = false,
        @SerializedName("lock_at")
        val lockAt: String? = null,
        @SerializedName("question_types")
        val questionTypes: List<String> = ArrayList(),
        @SerializedName("has_access_code")
        val hasAccessCode: Boolean = false,
        @SerializedName("one_question_at_a_time")
        val oneQuestionAtATime: Boolean = false,
        @SerializedName("require_lockdown_broswer")
        val requireLockdownBrowser: Boolean = false,
        @SerializedName("require_lockdown_browser_for_results")
        val requireLockdownBrowserForResults: Boolean = false,
        @SerializedName("anonymous_submissions")
        val allowAnonymousSubmissions: Boolean = false,
        val published: Boolean = false,
        @SerializedName("assignment_id")
        val assignmentId: Long = 0,
        @SerializedName("all_dates")
        val allDates: List<AssignmentDueDate> = ArrayList(),
        @SerializedName("only_visible_to_overrides")
        val isOnlyVisibleToOverrides: Boolean = false,
        val unpublishable: Boolean = false,

        // Non-API properties
        var _assignment: Assignment? = null,
        var _assignmentGroup: AssignmentGroup? = null,
        var _overrides: List<QuizOverride>? = null
) : CanvasModel<Quiz>() {
    override val comparisonDate: Date? get() = dueAt.toDate()
    override val comparisonString get() = _assignment?.name ?: title

    override fun compareTo(other: Quiz) = compareQuizzes(this, other)

    private fun compareQuizzes(quiz1: Quiz, quiz2: Quiz): Int {
        //quizzes sort by due date first, then by title alphabetically
        if(quiz1.dueAt != null && quiz2.dueAt != null) {
            val result = quiz1.dueAt.toDate()!!.compareTo(quiz2.dueAt.toDate())
            return if (result == 0) {
                NaturalOrderComparator.compare(quiz1.title?.lowercase(Locale.getDefault()).orEmpty(), quiz2.title?.lowercase(Locale.getDefault()).orEmpty())
            } else {
                result
            }
        } else if(quiz1.dueAt == null && quiz2.dueAt != null) {
            return 1
        } else if(quiz1.dueAt != null && quiz2.dueAt == null) {
            return -1
        }

        return NaturalOrderComparator.compare(quiz1.title?.lowercase(Locale.getDefault()).orEmpty(), quiz2.title?.lowercase(Locale.getDefault()).orEmpty())
    }

    val url: String?
        get() = if (mobileUrl != null && mobileUrl != "") {
            mobileUrl
        } else htmlUrl

    val hideResultsStringResource: Int
        get() = if ("always" == hideResults) R.string.no else R.string.always

    val isGradeable: Boolean
        get() = TYPE_ASSIGNMENT == this.quizType || TYPE_GRADED_SURVEY == this.quizType

    val parsedQuestionTypes: ArrayList<QuizQuestion.QuestionType>
        get() = parseQuestionTypes(questionTypes)

    val lockDate: Date? get() = lockAt.toDate()
    val unlockDate: Date? get() = unlockAt.toDate()
    val dueDate: Date? get() = dueAt.toDate()

    enum class HideResultsType(val apiString: String) {
        NULL("null"),
        ALWAYS("always"),
        AFTER_LAST_ATTEMPT("after_last_attempt")
    }

    enum class SettingTypes {
        QUIZ_TYPE, POINTS, ASSIGNMENT_GROUP, SHUFFLE_ANSWERS, TIME_LIMIT,
        MULTIPLE_ATTEMPTS, SCORE_TO_KEEP, ATTEMPTS, VIEW_RESPONSES,
        SHOW_CORRECT_ANSWERS, ACCESS_CODE, IP_FILTER, ONE_QUESTION_AT_A_TIME,
        LOCK_QUESTIONS_AFTER_ANSWERING, ANONYMOUS_SUBMISSIONS
    }

    private fun parseQuestionTypes(question_types: List<String>): ArrayList<QuizQuestion.QuestionType> {
        val questionTypesList = ArrayList<QuizQuestion.QuestionType>()
        for (question_type in question_types) {
            if (question_type != null) {
                questionTypesList.add(QuizQuestion.parseQuestionType(question_type))
            }
        }

        return questionTypesList
    }

    val scoringPolicyString get() = when {
        KEEP_HIGHEST == scoringPolicy -> R.string.quiz_scoring_policy_highest
        KEEP_AVERAGE == scoringPolicy -> R.string.quiz_scoring_policy_average
        else -> R.string.quiz_scoring_policy_latest
    }

    companion object {
        const val TYPE_PRACTICE = "practice_quiz"
        const val TYPE_ASSIGNMENT = "assignment"
        const val TYPE_GRADED_SURVEY = "graded_survey"
        const val TYPE_SURVEY = "survey"
        const val TYPE_NEW_QUIZZES = "quizzes.next"

        const val KEEP_AVERAGE = "keep_average"
        const val KEEP_HIGHEST = "keep_highest"
    }
}

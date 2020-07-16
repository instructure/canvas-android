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
 *
 */
package com.instructure.canvasapi2.pact.canvas.logic

import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue
import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.models.AssignmentDueDate
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.QuizPermission
import com.instructure.canvasapi2.models.QuizQuestion
import com.instructure.canvasapi2.models.QuizSubmission
import com.instructure.canvasapi2.models.QuizSubmissionAnswer
import com.instructure.canvasapi2.models.QuizSubmissionMatch
import com.instructure.canvasapi2.models.QuizSubmissionQuestion
import io.pactfoundation.consumer.dsl.LambdaDslObject
import kotlinx.android.parcel.RawValue
import org.junit.Assert.assertNotNull

val questionTypeRegex = QuizQuestion.QuestionType.values().map({v -> v.stringVal}).joinToString("|")

//
// region logic for allDates object within Quiz
//

// There are many flavors of AllDates fields, so make this method private to this module
private fun LambdaDslObject.populateAllDatesFields() : LambdaDslObject {
    this
            .stringMatcher("due_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .stringMatcher("unlock_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .stringMatcher("lock_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .booleanType("base")

    return this
}

private fun verifyAllDatesPopulated(description: String, dates: AssignmentDueDate) {
    assertNotNull("$description + dueAt", dates.dueAt)
    assertNotNull("$description + unlockAt", dates.unlockAt)
    assertNotNull("$description + lockAt", dates.lockAt)
    assertNotNull("$description + isBase", dates.isBase)
}
//endregion

//
// region logic for permissions object within quiz
//

fun LambdaDslObject.populateQuizPermissionsFields() : LambdaDslObject {
    this
            .booleanType("read")
            .booleanType("submit")
            .booleanType("create")
            .booleanType("manage")
            .booleanType("read_statistics")
            .booleanType("review_grades")
            .booleanType("update")
            .booleanType("delete")
            .booleanType("grade")
            .booleanType("view_answer_audits")

    return this
}

fun verifyQuizPermissionsPopulated(description: String, permissions: QuizPermission) {
    assertNotNull("$description + read", permissions.read)
    assertNotNull("$description + submit", permissions.submit)
    assertNotNull("$description + create", permissions.create)
    assertNotNull("$description + manage", permissions.manage)
    assertNotNull("$description + readStatistics", permissions.readStatistics)
    assertNotNull("$description + reviewGrades", permissions.reviewGrades)
    assertNotNull("$description + update", permissions.update)
    assertNotNull("$description + delete", permissions.delete)
    assertNotNull("$description + grade", permissions.grade)
    assertNotNull("$description + viewAnswerAudits", permissions.viewAnswerAudits)
}
//endregion

//
// region Logic for quiz object fields
//

fun LambdaDslObject.populateQuizFields(role: String = "student", singleQuiz: Boolean = true): LambdaDslObject {

    this
            .id("id")
            .stringType("mobile_url")
            .stringType("html_url")
            .stringType("description")
            .stringMatcher("quiz_type", "practice_quiz|assignment|graded_survey|survey", "assignment")
            .id("assignment_group_id")
    // lock_info?
            .`object`("permissions") { obj ->
                obj.populateQuizPermissionsFields()
            }
            .id("allowed_attempts") // "id" -> integer number
            .id("question_count")
            .numberType("points_possible") // String in Quiz object, but float from API
            .booleanType("cant_go_back")
            .stringMatcher("due_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z" )
            .id("time_limit")
            .booleanType("shuffle_answers")
            .booleanType("show_correct_answers")
            .stringMatcher("scoring_policy", "keep_highest|keep_latest", "keep_highest")
            .stringType("ip_filter")
            .booleanType("locked_for_user")
            .stringType("lock_explanation")
            // TODO: Test quiz with hide_results != null
            //.stringMatcher("hide_results", "always|until_after_last_attempt", "always")
            .stringMatcher("show_correct_answers_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .stringMatcher("hide_correct_answers_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .stringMatcher("unlock_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .booleanType("one_time_results")
            .stringMatcher("lock_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .booleanType("has_access_code")
            .booleanType("one_question_at_a_time")
            .booleanType("require_lockdown_browser")
            .booleanType("require_lockdown_browser_for_results")
            //.booleanType("allow_anonymous_submissions") // only applies to survey/graded_survey
            .booleanType("published")
            .id("assignment_id")
            .minArrayLike("all_dates", 1) { obj ->
                obj.populateAllDatesFields()
            }

    if(role == "teacher") {
        this
                .booleanType("only_visible_to_overrides")
                .booleanType("unpublishable")
                .stringType("access_code")
    }

    if(singleQuiz) {
        this.minArrayLike(
                "question_types",
                1,
                PactDslJsonRootValue.stringMatcher(questionTypeRegex,"true_false_question"),
                1)
    }

    return this
}

fun assertQuizPopulated(description: String, quiz: Quiz, role: String = "student", singleQuiz: Boolean = true) {
    assertNotNull("$description + id", quiz.id)
    assertNotNull("$description + mobileUrl", quiz.mobileUrl)
    assertNotNull("$description + htmlUrl", quiz.htmlUrl)
    assertNotNull("$description + description", quiz.description)
    assertNotNull("$description + quizType", quiz.quizType)
    assertNotNull("$description + assignmentGroupId", quiz.assignmentGroupId)
    assertNotNull("$description + permissions", quiz.permissions)
    verifyQuizPermissionsPopulated("$description + permissions", quiz.permissions!!)
    assertNotNull("$description + allowedAttempts", quiz.allowedAttempts)
    assertNotNull("$description + questionCount", quiz.questionCount)
    assertNotNull("$description + pointsPossible", quiz.pointsPossible)
    assertNotNull("$description + isLockQuestionsAfterAnswering", quiz.isLockQuestionsAfterAnswering)
    assertNotNull("$description + dueAt", quiz.dueAt)
    assertNotNull("$description + timeLimit", quiz.timeLimit)
    assertNotNull("$description + shuffleAnswers", quiz.shuffleAnswers)
    assertNotNull("$description + showCorrectAnswers", quiz.showCorrectAnswers)
    assertNotNull("$description + scoringPolicy", quiz.scoringPolicy)
    assertNotNull("$description + ipFilter", quiz.ipFilter)
    assertNotNull("$description + lockedForUser", quiz.lockedForUser)
    assertNotNull("$description + lockExplanation", quiz.lockExplanation)
    // TODO: Test quiz with hide_results != null
    //assertNotNull("$description + hideResults", quiz.hideResults)
    assertNotNull("$description + showCorrectAnswersAt", quiz.showCorrectAnswersAt)
    assertNotNull("$description + hideCorrectAnswersAt", quiz.hideCorrectAnswersAt)
    assertNotNull("$description + unlockAt", quiz.unlockAt)
    assertNotNull("$description + oneTimeResults", quiz.oneTimeResults)
    assertNotNull("$description + lockAt", quiz.lockAt)
    assertNotNull("$description + hasAccessCode", quiz.hasAccessCode)
    assertNotNull("$description + oneQuestionAtATime", quiz.oneQuestionAtATime)
    assertNotNull("$description + requireLockdownBrowser", quiz.requireLockdownBrowser)
    assertNotNull("$description + requireLockdownBrowserForResults", quiz.requireLockdownBrowserForResults)
    // Later.  Only used in surveys at the moment.
    //assertNotNull("$description + allowAnonymousSubmissions", quiz.allowAnonymousSubmissions)
    assertNotNull("$description + published", quiz.published)
    assertNotNull("$description + assignmentId", quiz.assignmentId)
    assertNotNull("$description + allDates", quiz.allDates)
    quiz.allDates.forEach {ad ->
        verifyAllDatesPopulated("$description + allDates[*]", ad)
    }

    if(role == "teacher") {
        assertNotNull("$description + accessCode", quiz.accessCode)
        assertNotNull("$description + isOnlyVisibleToOverrides", quiz.isOnlyVisibleToOverrides)
        assertNotNull("$description + unpublishable", quiz.unpublishable)
    }

    if(singleQuiz) {
        assertNotNull("$description + questionTypes", quiz.questionTypes)
    }
}
//endregion

//
// region Logic for QuizSubmission object fields
//

fun LambdaDslObject.populateQuizSubmissionFields() : LambdaDslObject {
    this
            .id("id")
            .id("quiz_id")
            .id("user_id")
            .id("submission_id")
            .stringMatcher("started_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z" )
            .stringMatcher("finished_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z" )
            .stringMatcher("end_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z" )
            .id("attempt")
            .id("extra_attempts")
            .id("attempts_left")
            .id("extra_time")
            .booleanType("manually_unlocked")
            .id("time_spent")
            .numberType("score")
            .numberType("score_before_regrade")
            .numberType("kept_score")
            .numberType("fudge_points")
            .booleanType("has_seen_results")
            .stringMatcher("workflow_state", "untaken|pending_review|complete|settings_only|preview", "untaken")
            .numberType("quiz_points_possible")
            .stringType("validation_token")
            .booleanType("overdue_and_needs_submission")

    return this
}

fun assertQuizSubmissionPopulated(description: String, quizSubmission: QuizSubmission) {
    assertNotNull("$description + id", quizSubmission.id)
    assertNotNull("$description + quizId", quizSubmission.quizId)
    assertNotNull("$description + userId", quizSubmission.userId)
    assertNotNull("$description + submissionId", quizSubmission.submissionId)
    assertNotNull("$description + startedAt", quizSubmission.startedAt)
    assertNotNull("$description + finishedAt", quizSubmission.finishedAt)
    assertNotNull("$description + endAt", quizSubmission.endAt)
    assertNotNull("$description + attempt", quizSubmission.attempt)
    assertNotNull("$description + extraAttempts", quizSubmission.extraAttempts)
    assertNotNull("$description + attemptsLeft", quizSubmission.attemptsLeft)
    assertNotNull("$description + extraTime", quizSubmission.extraTime)
    assertNotNull("$description + manuallyUnlocked", quizSubmission.manuallyUnlocked)
    assertNotNull("$description + timeSpent", quizSubmission.timeSpent)
    assertNotNull("$description + score", quizSubmission.score)
    assertNotNull("$description + scoreBeforeRegrade", quizSubmission.scoreBeforeRegrade)
    assertNotNull("$description + keptScore", quizSubmission.keptScore)
    assertNotNull("$description + fudgePoints", quizSubmission.fudgePoints)
    assertNotNull("$description + hasSeenResults", quizSubmission.hasSeenResults)
    assertNotNull("$description + workflowState", quizSubmission.workflowState)
    assertNotNull("$description + quizPointsPossible", quizSubmission.quizPointsPossible)
    assertNotNull("$description + validationToken", quizSubmission.validationToken)
    assertNotNull("$description + overDueAndNeedsSubmission", quizSubmission.overDueAndNeedsSubmission)
}
//endregion



package com.instructure.canvasapi2.pact.canvas.logic

import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue
import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.QuizQuestion
import com.instructure.canvasapi2.models.QuizSubmission
import com.instructure.canvasapi2.models.QuizSubmissionAnswer
import com.instructure.canvasapi2.models.QuizSubmissionMatch
import com.instructure.canvasapi2.models.QuizSubmissionQuestion
import io.pactfoundation.consumer.dsl.LambdaDslObject
import kotlinx.android.parcel.RawValue
import org.junit.Assert.assertNotNull

val questionTypeRegex = QuizQuestion.QuestionType.values().map({v -> v.stringVal}).joinToString("|")

fun LambdaDslObject.populateQuizFields(): LambdaDslObject {

    this
            .id("id")
            .stringType("mobile_url")
            .stringType("html_url")
            .stringType("description")
            .stringMatcher("quiz_type", "practice_quiz|assignment|graded_survey|survey", "assignment")
            .id("assignment_group_id")
    // lock_info?
    // permissions?
            .id("allowed_attempts") // "id" -> integer number
            .id("question_count")
            .stringType("points_possible")
            .booleanType("cant_go_back")
            .stringMatcher("due_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z" )
            .id("time_limit")
            .booleanType("shuffle_answers")
            .booleanType("show_correct_answers")
            .stringMatcher("scoring_policy", "keep_highest|keep_latest", "keep_highest")
            .stringType("access_code")
            .stringType("ip_filter")
            .booleanType("locked_for_user")
            .stringType("lock_explanation")
            .stringMatcher("hide_results", "always|until_after_last_attempt", "always") // ??
            .stringMatcher("show_correct_answers_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .stringMatcher("hide_correct_answers_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .stringMatcher("unlock_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .booleanType("one_time_results")
            .stringMatcher("lock_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .minArrayLike(
                    "question_types",
                    1,
                     PactDslJsonRootValue.stringMatcher(questionTypeRegex,"true_false_question"),
                     1)
            .booleanType("has_access_code")
            .booleanType("one_question_at_a_time")
            .booleanType("require_lockdown_browser")
            .booleanType("require_lockdown_browser_for_results")
            .booleanType("allow_anonymous_submissions")
            .booleanType("published")
            .id("assignment_id")
    // all_dates?
            .booleanType("only_visible_to_overrides")
            .booleanType("unpublishable")

    return this
}


fun assertQuizPopulated(description: String, quiz: Quiz) {
    assertNotNull("$description + id", quiz.id)
    assertNotNull("$description + mobileUrl", quiz.mobileUrl)
    assertNotNull("$description + htmlUrl", quiz.htmlUrl)
    assertNotNull("$description + description", quiz.description)
    assertNotNull("$description + quizType", quiz.quizType)
    assertNotNull("$description + assignmentGroupId", quiz.assignmentGroupId)
    assertNotNull("$description + allowedAttempts", quiz.allowedAttempts)
    assertNotNull("$description + questionCount", quiz.questionCount)
    assertNotNull("$description + pointsPossible", quiz.pointsPossible)
    assertNotNull("$description + isLockQuestionsAfterAnswering", quiz.isLockQuestionsAfterAnswering)
    assertNotNull("$description + dueAt", quiz.dueAt)
    assertNotNull("$description + timeLimit", quiz.timeLimit)
    assertNotNull("$description + shuffleAnswers", quiz.shuffleAnswers)
    assertNotNull("$description + showCorrectAnswers", quiz.showCorrectAnswers)
    assertNotNull("$description + scoringPolicy", quiz.scoringPolicy)
    assertNotNull("$description + accessCode", quiz.accessCode)
    assertNotNull("$description + ipFilter", quiz.ipFilter)
    assertNotNull("$description + lockedForUser", quiz.lockedForUser)
    assertNotNull("$description + lockExplanation", quiz.lockExplanation)
    assertNotNull("$description + hideResults", quiz.hideResults)
    assertNotNull("$description + showCorrectAnswersAt", quiz.showCorrectAnswersAt)
    assertNotNull("$description + hideCorrectAnswersAt", quiz.hideCorrectAnswersAt)
    assertNotNull("$description + unlockAt", quiz.unlockAt)
    assertNotNull("$description + oneTimeResults", quiz.oneTimeResults)
    assertNotNull("$description + lockAt", quiz.lockAt)
    assertNotNull("$description + questionTypes", quiz.questionTypes)  // more?
    assertNotNull("$description + hasAccessCode", quiz.hasAccessCode)
    assertNotNull("$description + oneQuestionAtATime", quiz.oneQuestionAtATime)
    assertNotNull("$description + requireLockdownBrowser", quiz.requireLockdownBrowser)
    assertNotNull("$description + requireLockdownBrowserForResults", quiz.requireLockdownBrowserForResults)
    assertNotNull("$description + allowAnonymousSubmissions", quiz.allowAnonymousSubmissions)
    assertNotNull("$description + published", quiz.published)
    assertNotNull("$description + assignmentId", quiz.assignmentId)
    assertNotNull("$description + isOnlyVisibleToOverrides", quiz.isOnlyVisibleToOverrides)
    assertNotNull("$description + unpublishable", quiz.unpublishable)
}

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

fun LambdaDslObject.populateQuizSubmissionAnswerFields() : LambdaDslObject {
    this
            .id("id")
            .stringType("text")
            .stringType("html")
            .stringType("comments")
            .id("weight")
            .stringType("blank_id")

    return this
}

fun assertQuizSubmissionAnswerPopulated(description: String, answer: QuizSubmissionAnswer) {
    assertNotNull("$description + id", answer.id)
    assertNotNull("$description + text", answer.text)
    assertNotNull("$description + html", answer.html)
    assertNotNull("$description + comments", answer.comments)
    assertNotNull("$description + weight", answer.weight)
    assertNotNull("$description + blankId", answer.blankId)
}

fun LambdaDslObject.populateQuizSubmissionQuestionFields() : LambdaDslObject {
    this
            .id("id")
            .booleanType("flagged")
            .minArrayLike("answers", 1) { obj ->
                obj.populateQuizSubmissionAnswerFields()
            }
            .id("position")
            .id("quiz_id")
            .stringType("question_name")
            .stringMatcher("question_type", questionTypeRegex, "true_false_question")
            .stringType("question_text")
    // answer?
    // matches?

    return this
}

fun assertQuizSubmissionQuestionPopulated(description: String, question: QuizSubmissionQuestion) {
    assertNotNull("$description + id", question.id)
    assertNotNull("$description + isFlagged", question.isFlagged)
    assertNotNull("$description + answers", question.answers)
    for(i in 0..question.answers!!.size - 1) {
        assertQuizSubmissionAnswerPopulated("$description + answers[$i]", question.answers!![i])
    }
    assertNotNull("$description + position", question.position)
    assertNotNull("$description + quizId", question.quizId)
    assertNotNull("$description + questionName", question.questionName)
    assertNotNull("$description + questionType", question.questionType)
    assertNotNull("$description + questionText", question.questionText)
}
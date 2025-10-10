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

package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.toApiString
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class AssignmentTest {

    @Test
    fun getLastActualSubmission_TestNullSubmission() {
        val assignment = Assignment(submission = null)
        assertEquals(null, assignment.lastActualSubmission)
    }

    @Test
    fun getLastActualSubmission_TestNullWorkFlow() {
        val submission = Submission(workflowState = null)
        val assignment = Assignment(submission = submission)

        assertEquals(null, assignment.lastActualSubmission)
    }

    @Test
    fun getLastActualSubmission_TestWorkFlowSubmitted() {
        val submission = Submission(workflowState = "submitted")
        val assignment = Assignment(submission = submission)

        assertEquals(submission, assignment.lastActualSubmission)
    }

    @Test
    fun isAllowedToSubmit_TestTrue() {
        val assignment = Assignment(submissionTypesRaw = listOf(
                Assignment.submissionTypeToAPIString(Assignment.SubmissionType.DISCUSSION_TOPIC),
                Assignment.submissionTypeToAPIString(Assignment.SubmissionType.ONLINE_URL)
        ), lockedForUser = false)

        assertEquals(true, assignment.isAllowedToSubmit)
    }

    @Test
    fun isAllowedToSubmit_TestFalse() {
        val assignment = Assignment(
                submissionTypesRaw = listOf(
                        Assignment.submissionTypeToAPIString(Assignment.SubmissionType.ATTENDANCE),
                        Assignment.submissionTypeToAPIString(Assignment.SubmissionType.ONLINE_QUIZ)
                ), lockedForUser = true
        )

        assertEquals(false, assignment.isAllowedToSubmit)
    }

    @Test
    fun isWithoutGradedSubmission_TestTrueNotNull() {
        val submission = Submission(workflowState = "submitted", grade = null, submissionType = null)
        val assignment = Assignment(submission = submission)

        assertEquals(true, assignment.isWithoutGradedSubmission)
    }

    @Test
    fun isWithoutGradedSubmission_TestTrueNull() {
        val assignment = Assignment(submission = null)

        assertEquals(true, assignment.isWithoutGradedSubmission)
    }

    @Test
    fun isWithoutGradedSubmission_TestFalse() {
        val submission = Submission(workflowState = "submitted", grade = "A", submissionType = "Online_Quiz")
        val assignment = Assignment(submission = submission)

        assertEquals(false, assignment.isWithoutGradedSubmission)
    }

    //region isLocked
    @Test
    fun isLocked_TestNullLockInfo() {
        val assignment = Assignment(lockInfo = null)

        assertEquals(false, assignment.isLocked)
    }

    @Test
    fun isLocked_TestEmptyLockInfo() {
        val assignment = Assignment(lockInfo = LockInfo())

        assertEquals(false, assignment.isLocked)
    }

    @Test
    fun isLocked_TestLockedModuleName() {
        val lockedModule = LockedModule(name = "Hodor")
        val lockInfo = LockInfo(contextModule = lockedModule)
        val assignment = Assignment(lockInfo = lockInfo)

        assertEquals(true, assignment.isLocked)
    }

    @Test
    fun isLocked_TestUnlockAfterCurrentDate() {
        val time = Calendar.getInstance().timeInMillis + 100000
        val date = Date(time)

        val lockInfo = LockInfo(unlockAt = date.toApiString())
        val assignment = Assignment(lockInfo = lockInfo)

        assertEquals(true, assignment.isLocked)
    }
    //endregion

    @Test
    fun hasRubric_TestTrue() {
        val rubricCriterionList = ArrayList<RubricCriterion>()
        rubricCriterionList.add(RubricCriterion())

        val assignment = Assignment(rubric = rubricCriterionList)

        assertEquals(true, assignment.hasRubric())
    }

    @Test
    fun hasRubric_TestFalse() {
        val assignment = Assignment(rubric = null)

        assertEquals(false, assignment.hasRubric())
    }

    //region submissionTypeToAPIString
    @Test
    fun submissionTypeToAPIString_TestOnlineQuiz() {
        assertEquals("online_quiz",
                Assignment.submissionTypeToAPIString(Assignment.SubmissionType.ONLINE_QUIZ))
    }

    @Test
    fun submissionTypeToAPIString_TestNone() {
        assertEquals("none",
                Assignment.submissionTypeToAPIString(Assignment.SubmissionType.NONE))
    }

    @Test
    fun submissionTypeToAPIString_TestOnPaper() {
        assertEquals("on_paper",
                Assignment.submissionTypeToAPIString(Assignment.SubmissionType.ON_PAPER))
    }

    @Test
    fun submissionTypeToAPIString_TestDiscussionTopic() {
        assertEquals("discussion_topic",
                Assignment.submissionTypeToAPIString(Assignment.SubmissionType.DISCUSSION_TOPIC))
    }

    @Test
    fun submissionTypeToAPIString_TestExternalTool() {
        assertEquals("external_tool",
                Assignment.submissionTypeToAPIString(Assignment.SubmissionType.EXTERNAL_TOOL))
    }

    @Test
    fun submissionTypeToAPIString_TestOnlineUpload() {
        assertEquals("online_upload",
                Assignment.submissionTypeToAPIString(Assignment.SubmissionType.ONLINE_UPLOAD))
    }

    @Test
    fun submissionTypeToAPIString_TestOnlineTextEntry() {
        assertEquals("online_text_entry",
                Assignment.submissionTypeToAPIString(Assignment.SubmissionType.ONLINE_TEXT_ENTRY))
    }

    @Test
    fun submissionTypeToAPIString_TestOnlineUrl() {
        assertEquals("online_url",
                Assignment.submissionTypeToAPIString(Assignment.SubmissionType.ONLINE_URL))
    }

    @Test
    fun submissionTypeToAPIString_TestMediaRecording() {
        assertEquals("media_recording",
                Assignment.submissionTypeToAPIString(Assignment.SubmissionType.MEDIA_RECORDING))
    }

    @Test
    fun submissionTypeToAPIString_TestAttendance() {
        assertEquals("attendance",
                Assignment.submissionTypeToAPIString(Assignment.SubmissionType.ATTENDANCE))
    }

    @Test
    fun submissionTypeToAPIString_TestNotGraded() {
        assertEquals("not_graded",
                Assignment.submissionTypeToAPIString(Assignment.SubmissionType.NOT_GRADED))
    }
    //endregion

    //region getGradingTypeFromAPIString
    @Test
    fun getGradingTypeFromAPIString_TestPassFail() {
        assertEquals(Assignment.GradingType.PASS_FAIL,
                Assignment.getGradingTypeFromAPIString("pass_fail"))
    }

    @Test
    fun getGradingTypeFromAPIString_TestPercent() {
        assertEquals(Assignment.GradingType.PERCENT,
                Assignment.getGradingTypeFromAPIString("percent"))
    }

    @Test
    fun getGradingTypeFromAPIString_TestLetterGrade() {
        assertEquals(Assignment.GradingType.LETTER_GRADE,
                Assignment.getGradingTypeFromAPIString("letter_grade"))
    }

    @Test
    fun getGradingTypeFromAPIString_TestPoints() {
        assertEquals(Assignment.GradingType.POINTS,
                Assignment.getGradingTypeFromAPIString("points"))
    }

    @Test
    fun getGradingTypeFromAPIString_TestGPAScale() {
        assertEquals(Assignment.GradingType.GPA_SCALE,
                Assignment.getGradingTypeFromAPIString("gpa_scale"))
    }

    @Test
    fun getGradingTypeFromAPIString_TestNotGraded() {
        assertEquals(Assignment.GradingType.NOT_GRADED,
                Assignment.getGradingTypeFromAPIString("not_graded"))
    }
    //endregion


    //region gradingTypeToAPIString
    @Test
    fun gradingTypeToAPIString_TestPassFail() {
        assertEquals("pass_fail",
                Assignment.gradingTypeToAPIString(Assignment.GradingType.PASS_FAIL))
    }

    @Test
    fun gradingTypeToAPIString_TestPercent() {
        assertEquals("percent",
                Assignment.gradingTypeToAPIString(Assignment.GradingType.PERCENT))
    }

    @Test
    fun gradingTypeToAPIString_TestLetterGrade() {
        assertEquals("letter_grade",
                Assignment.gradingTypeToAPIString(Assignment.GradingType.LETTER_GRADE))
    }

    @Test
    fun gradingTypeToAPIString_TestPoints() {
        assertEquals("points",
                Assignment.gradingTypeToAPIString(Assignment.GradingType.POINTS))
    }

    @Test
    fun gradingTypeToAPIString_TestGPAScale() {
        assertEquals("gpa_scale",
                Assignment.gradingTypeToAPIString(Assignment.GradingType.GPA_SCALE))
    }

    @Test
    fun gradingTypeToAPIString_TestNotGraded() {
        assertEquals("not_graded",
                Assignment.gradingTypeToAPIString(Assignment.GradingType.NOT_GRADED))
    }
    //endregion

    //region isQuiz
    @Test
    fun isQuiz_TestOnlineQuiz() {
        val assignment = Assignment(submissionTypesRaw = listOf("online_quiz"))
        assertEquals(true, assignment.isQuiz())
    }

    @Test
    fun isQuiz_TestQuizLtiAssignment() {
        val externalToolAttributes = ExternalToolAttributes(url = "https://example.com/quiz-lti/launch")
        val assignment = Assignment(
            submissionTypesRaw = listOf("external_tool"),
            externalToolAttributes = externalToolAttributes
        )
        assertEquals(true, assignment.isQuiz())
    }

    @Test
    fun isQuiz_TestRegularExternalTool() {
        val externalToolAttributes = ExternalToolAttributes(url = "https://example.com/tool/launch")
        val assignment = Assignment(
            submissionTypesRaw = listOf("external_tool"),
            externalToolAttributes = externalToolAttributes
        )
        assertEquals(false, assignment.isQuiz())
    }

    @Test
    fun isQuiz_TestNonQuizSubmissionType() {
        val assignment = Assignment(submissionTypesRaw = listOf("online_upload"))
        assertEquals(false, assignment.isQuiz())
    }
    //endregion

}

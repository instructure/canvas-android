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
 */
package com.instructure.teacher.ui.interaction

import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockcanvas.addQuestionToQuiz
import com.instructure.canvas.espresso.mockcanvas.addQuizSubmission
import com.instructure.canvas.espresso.mockcanvas.addQuizToCourse
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeAssignmentDetailsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeCommentLibraryManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeCustomGradeStatusesManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeDifferentiationTagsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeInboxSettingsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakePostPolicyManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeRecentGradedSubmissionsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeStudentContextManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeSubmissionCommentsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeSubmissionContentManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeSubmissionDetailsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeSubmissionGradeManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeSubmissionRubricManager
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.di.GraphQlApiModule
import com.instructure.canvasapi2.di.graphql.CustomGradeStatusModule
import com.instructure.canvasapi2.managers.CommentLibraryManager
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.canvasapi2.managers.PostPolicyManager
import com.instructure.canvasapi2.managers.StudentContextManager
import com.instructure.canvasapi2.managers.SubmissionRubricManager
import com.instructure.canvasapi2.managers.graphql.AssignmentDetailsManager
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.managers.graphql.DifferentiationTagsManager
import com.instructure.canvasapi2.managers.graphql.RecentGradedSubmissionsManager
import com.instructure.canvasapi2.managers.graphql.SubmissionCommentsManager
import com.instructure.canvasapi2.managers.graphql.SubmissionContentManager
import com.instructure.canvasapi2.managers.graphql.SubmissionDetailsManager
import com.instructure.canvasapi2.managers.graphql.SubmissionGradeManager
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.QuizAnswer
import com.instructure.pandautils.di.DifferentiationTagsModule
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Test

@HiltAndroidTest
@UninstallModules(
    GraphQlApiModule::class,
    CustomGradeStatusModule::class,
    DifferentiationTagsModule::class)
class SpeedGraderQuizSubmissionInteractionTest : TeacherComposeTest() {

    override fun displaysPageObjects() = Unit

    @BindValue
    @JvmField
    val commentLibraryManager: CommentLibraryManager = FakeCommentLibraryManager()

    @BindValue
    @JvmField
    val postPolicyManager: PostPolicyManager = FakePostPolicyManager()

    @BindValue
    @JvmField
    val inboxSettingsManager: InboxSettingsManager = FakeInboxSettingsManager()

    @BindValue
    @JvmField
    val personContextManager: StudentContextManager = FakeStudentContextManager()

    @BindValue
    @JvmField
    val assignmentDetailsManager: AssignmentDetailsManager = FakeAssignmentDetailsManager()

    @BindValue
    @JvmField
    val submissionContentManager: SubmissionContentManager = FakeSubmissionContentManager()

    @BindValue
    @JvmField
    val submissionGradeManager: SubmissionGradeManager = FakeSubmissionGradeManager()

    @BindValue
    @JvmField
    val submissionDetailsManager: SubmissionDetailsManager = FakeSubmissionDetailsManager()

    @BindValue
    @JvmField
    val submissionRubricManager: SubmissionRubricManager = FakeSubmissionRubricManager()

    @BindValue
    @JvmField
    val submissionCommentsManager: SubmissionCommentsManager = FakeSubmissionCommentsManager()

    @BindValue
    @JvmField
    val customGradeStatusesManager: CustomGradeStatusesManager = FakeCustomGradeStatusesManager()

    @BindValue
    @JvmField
    val differentiationTagsManager: DifferentiationTagsManager = FakeDifferentiationTagsManager()

    @BindValue
    @JvmField
    val recentGradedSubmissionsManager: RecentGradedSubmissionsManager = FakeRecentGradedSubmissionsManager()

    @Test
    fun displaysNoSubmission() {
        getToQuizSubmissionPage(submitQuiz = false)
        speedGraderQuizSubmissionPage.assertShowsNoSubmissionState(R.string.noSubmissionTeacher)
    }

    @Test
    fun displaysPendingReviewState() {
        getToQuizSubmissionPage(addQuestion = true, state = "pending_review")
        speedGraderQuizSubmissionPage.assertShowsPendingReviewState()
    }

    @Test
    fun displaysViewQuizState() {
        getToQuizSubmissionPage(state = "untaken")
        speedGraderQuizSubmissionPage.assertShowsViewQuizState()
    }

    private fun getToQuizSubmissionPage(addQuestion: Boolean = false, submitQuiz: Boolean = true, state: String = "untaken") {
        val data = MockCanvas.init(teacherCount = 1, studentCount = 1, favoriteCourseCount = 1, courseCount = 1)
        val teacher = data.teachers[0]
        val student = data.students[0]
        val course = data.courses.values.first()

        data.addCoursePermissions(
                course.id,
                CanvasContextPermission()
        )

        val quiz = data.addQuizToCourse(course = course, published = true, quizType = Quiz.TYPE_ASSIGNMENT)
        val quizId = quiz.id

        if (addQuestion) {
            data.addQuestionToQuiz(
                    course = course,
                    quizId = quizId,
                    questionName = "Mock Question",
                    questionText = "What's your favorite color?",
                    answers = arrayOf(
                            QuizAnswer(id =  data.newItemId(), answerText = "Red", answerWeight = 1),
                            QuizAnswer(id =  data.newItemId(), answerText = "Yellow", answerWeight = 0),
                            QuizAnswer(id =  data.newItemId(), answerText = "Blue", answerWeight = 0),
                            QuizAnswer(id =  data.newItemId(), answerText = "Green", answerWeight = 0)
                    )
            )

        }

        if (submitQuiz) {
            data.addQuizSubmission(quiz, student, state)
        }

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)
        dashboardPage.openCourse(course)
        courseBrowserPage.openQuizzesTab()

        quizListPage.clickQuiz(quiz)
        quizDetailsPage.openSubmissionsPage()
        assignmentSubmissionListPage.clickSubmission(student)
        composeTestRule.waitForIdle()
        if (isCompactDevice()) speedGraderPage.clickExpandPanelButton()
        speedGraderPage.selectTab("Grade & Rubric")
    }
}

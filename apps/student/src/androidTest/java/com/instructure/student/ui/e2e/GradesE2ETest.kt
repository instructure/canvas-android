package com.instructure.student.ui.e2e

import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.QuizzesApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.QuizAnswer
import com.instructure.dataseeding.model.QuizQuestion
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.R
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class GradesE2ETest: StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.GRADES, TestCategory.E2E)
    fun testGradesE2E() {
        // Seed basic student/teacher/course data
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        // Create an assignment
        val assignment = AssignmentsApi.createAssignment(AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                withDescription = true,
                dueAt = 1.days.fromNow.iso8601,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token,
                gradingType = GradingType.PERCENT,
                pointsPossible = 15.0
        ))

        // Create a quiz with some questions
        val quizQuestions = listOf(
                QuizQuestion(
                        pointsPossible = 5,
                        questionType = "multiple_choice_question",
                        questionText = "Odd or even?",
                        answers = listOf(
                                QuizAnswer(id = 1, weight = 1, text = "Odd"),
                                QuizAnswer(id = 1, weight = 1, text = "Even")
                        )

                ),
                QuizQuestion(
                        pointsPossible = 5,
                        questionType = "multiple_choice_question",
                        questionText = "How many roads must a man walk down?",
                        answers = listOf(
                                QuizAnswer(id = 1, weight = 1, text = "42"),
                                QuizAnswer(id = 1, weight = 1, text = "A Gazillion"),
                                QuizAnswer(id = 1, weight = 1, text = "13")
                        )

                )
        )

        val quiz = QuizzesApi.createAndPublishQuiz(course.id, teacher.token, quizQuestions)

        // Sign the student in
        tokenLogin(student)
        dashboardPage.waitForRender()

        // Navigate to our course
        dashboardPage.selectCourse(course)

        // Let's take a look at the grades page
        courseBrowserPage.selectGrades()

        // At this point, with nothing turned in, our total grade should be "N/A"
        courseGradesPage.assertTotalGrade(withText(R.string.noGradeText))

        // We'll be using these a lot
        val assignmentMatcher = withText(assignment.name)
        val quizMatcher = withText(quiz.title)

        // Make sure our assignments/quizzes are displayed
        courseGradesPage.refresh()
        courseGradesPage.assertItemDisplayed(assignmentMatcher)
        courseGradesPage.assertGradeNotDisplayed(assignmentMatcher)
        courseGradesPage.assertItemDisplayed(quizMatcher)
        courseGradesPage.assertGradeNotDisplayed(quizMatcher)

        // Let's try a what-if
        courseGradesPage.toggleWhatIf()
        courseGradesPage.enterWhatIfGrade(assignmentMatcher, "12")
        courseGradesPage.assertTotalGrade(containsTextCaseInsensitive("80"))
        courseGradesPage.toggleWhatIf()
        courseGradesPage.assertTotalGrade(withText(R.string.noGradeText))

        // Let's submit our assignment, and grade it
        val submission = SubmissionsApi.submitCourseAssignment(
                submissionType = SubmissionType.ONLINE_TEXT_ENTRY,
                courseId = course.id,
                assignmentId = assignment.id,
                fileIds = mutableListOf(),
                studentToken = student.token
        )
        SubmissionsApi.gradeSubmission(
                teacherToken = teacher.token,
                courseId = course.id,
                assignmentId = assignment.id,
                studentId = student.id,
                postedGrade="9",
                excused = false)
        courseGradesPage.refresh()

        // 9 out of 15
        courseGradesPage.assertGradeDisplayed(
                assignmentMatcher,
                containsTextCaseInsensitive("60"))

        // Let's toggle "Base on graded assignments" and verify that we see the correct score
        courseGradesPage.toggleBaseOnGradedAssignments()
        courseGradesPage.refreshUntilAssertTotalGrade(containsTextCaseInsensitive("36")) // 9 out of 25
        courseGradesPage.toggleBaseOnGradedAssignments()
        courseGradesPage.refreshUntilAssertTotalGrade(containsTextCaseInsensitive("60")) // 9 out of 15

        /* TODO: Submit a quiz if/when we can do so via WebView
        // Let's submit our quiz
        courseGradesPage.selectItem(quizMatcher)
        assignmentDetailsPage.viewQuiz()
        quizDetailsPage.takeQuiz(quizQuestions)
        quizDetailsPage.submitQuiz()
        Espresso.pressBack() // to assignment details
        Espresso.pressBack() // to grades

        // And make sure our new quiz grade shows up in our grades
        courseGradesPage.refresh()
        courseGradesPage.assertGradeDisplayed(quizMatcher, containsTextCaseInsensitive("10/10"))
        courseGradesPage.refreshUntilAssertTotalGrade(containsTextCaseInsensitive("76"))
        */
    }

}

package com.instructure.student.ui.e2e

import android.util.Log
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.QuizzesApi
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedAssignments
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class TodoE2ETest: StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enableAndConfigureAccessibilityChecks() {
        //We don't want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.TODOS, TestCategory.E2E)
    fun testTodoE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seed an assignment for ${course.name} course with tomorrow due date.")
        val seededAssignments = seedAssignments(
                courseId = course.id,
                teacherToken = teacher.token,
                dueAt = 1.days.fromNow.iso8601
        )

        Log.d(PREPARATION_TAG,"Seed another assignment for ${course.name} course with 7 days from now due date.")
        val seededAssignments2 = seedAssignments(
            courseId = course.id,
            teacherToken = teacher.token,
            dueAt = 7.days.fromNow.iso8601
        )

        val testAssignment = seededAssignments[0]
        val borderDateAssignment = seededAssignments2[0] //We show items in the to do section which are within 7 days.

        Log.d(PREPARATION_TAG,"Seed a quiz for ${course.name} course with tomorrow due date.")
        val quiz = QuizzesApi.createQuiz(
                QuizzesApi.CreateQuizRequest(
                        courseId = course.id,
                        withDescription = true,
                        published = true,
                        token = teacher.token,
                        dueAt = 1.days.fromNow.iso8601)
        )

        Log.d(PREPARATION_TAG,"Seed another quiz for ${course.name} course with 8 days from now due date..")
        val tooFarAwayQuiz = QuizzesApi.createQuiz(
            QuizzesApi.CreateQuizRequest(
                courseId = course.id,
                withDescription = true,
                published = true,
                token = teacher.token,
                dueAt = 8.days.fromNow.iso8601)
        )

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId} , password: ${student.password}")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Navigate to 'To Do' page via bottom-menu.")
        dashboardPage.clickTodoTab()

        Log.d(STEP_TAG,"Assert that ${testAssignment.name} assignment is displayed and ${borderDateAssignment.name} is displayed because it's 7 days away from now..")
        todoPage.assertAssignmentDisplayed(testAssignment)
        todoPage.assertAssignmentDisplayed(borderDateAssignment)

        Log.d(STEP_TAG,"Assert that ${quiz.title} quiz is displayed and ${tooFarAwayQuiz.title} quiz is not displayed because it's end date is more than a week away..")
        todoPage.assertQuizDisplayed(quiz)
        todoPage.assertQuizNotDisplayed(tooFarAwayQuiz)
    }
}
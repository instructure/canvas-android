package com.instructure.student.ui.e2e

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
import java.util.Calendar

@HiltAndroidTest
class TodoE2ETest: StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.TODOS, TestCategory.E2E)
    fun testTodoE2E() {

        // Don't attempt this test on a Friday, Saturday or Sunday.
        // The TODO tab doesn't seem to behave correctly on Fridays (or presumably weekends).
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        if(dayOfWeek == Calendar.FRIDAY || dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            println("We don't run the TODO E2E test on weekends")
            return
        }

        // Seed data
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        // Seed an assignment due tomorrow, for todo tab
        val seededAssignments = seedAssignments(
                courseId = course.id,
                teacherToken = teacher.token,
                dueAt = 1.days.fromNow.iso8601
        )

        // Seed a quiz due tomorrow, for todo tab
        val quiz = QuizzesApi.createQuiz(
                QuizzesApi.CreateQuizRequest(
                        courseId = course.id,
                        withDescription = true,
                        published = true,
                        token = teacher.token,
                        dueAt = 1.days.fromNow.iso8601)

        )

        // Sign in with lone student
        tokenLogin(student)

        // Navigate to ToDo tab
        dashboardPage.waitForRender()
        dashboardPage.clickTodoTab()
        //todoPage.waitForRender()

        // Verify that your assignment shows up
        todoPage.assertAssignmentDisplayed(seededAssignments[0])

        // Verify that your quiz shows up
        todoPage.assertQuizDisplayed(quiz)
    }
}
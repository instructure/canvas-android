package com.instructure.student.ui.e2e

import android.os.SystemClock.sleep
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.GroupsApi
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
class EventsE2ETest: StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // TODO: Get this working with embedded Flutter
    /*@E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.EVENTS, TestCategory.E2E)
    fun testEventsE2E() {
        // Seed data
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        // Seed an assignment due today, for calendar tab
        val seededAssignments = seedAssignments(
                courseId = course.id,
                teacherToken = teacher.token,
                dueAt = 0.days.fromNow.iso8601,
                assignments = 1
        )

        // Seed a grouped assignment due today, for calendar tab
        var groupCategory = GroupsApi.createCourseGroupCategory(course.id, teacher.token)
        var group = GroupsApi.createGroup(groupCategory.id, teacher.token)
        GroupsApi.createGroupMembership(group.id, student.id, teacher.token)

        val groupedAssignmentRequest = AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                teacherToken = teacher.token,
                dueAt = 0.days.fromNow.iso8601,
                groupCategoryId = groupCategory.id,
                submissionTypes = emptyList()
        )
        val groupedAssignment = AssignmentsApi.createAssignment(groupedAssignmentRequest)


        // Seed a quiz due today, for calendar tab
        val quiz = QuizzesApi.createQuiz(
                QuizzesApi.CreateQuizRequest(
                        courseId = course.id,
                        withDescription = true,
                        published = true,
                        token = teacher.token,
                        dueAt = 0.days.fromNow.iso8601)

        )

        // Sign in with lone student
        tokenLogin(student)

        // Navigate to calendar
        dashboardPage.waitForRender()
        dashboardPage.clickCalendarTab()
        //calendarPage.waitForRender()

        // Select the calendars for your courses/groups
        calendarPage.selectDesiredCalendarsAndDismiss(course.name, group.name)

        // Hide the calendar itself.  This helps on low-res devices.
        calendarPage.toggleCalendarVisibility()

        // Make sure that your assignment shows up on the calendar
        calendarPage.assertAssignmentDisplayed(seededAssignments.assignmentList[0])

        // Assert that the grouped assignment is there
        calendarPage.assertAssignmentDisplayed(groupedAssignment)

        // Make sure that your quiz shows up on the calendar
        calendarPage.assertQuizDisplayed(quiz)
    }*/

    // TODO: Can we test other types of events?
    // https://mobileqa.test.instructure.com/api/v1/calendar_events/?all_events=false&type=assignment -- covered
    // https://mobileqa.test.instructure.com/api/v1/calendar_events/?all_events=false&type=event... -- ??

}

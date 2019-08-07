package com.instructure.student.ui.e2e

import android.os.SystemClock.sleep
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.Stub
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import org.junit.Before
import org.junit.Test

class AssignmentsE2ETest: StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Stub
    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.ASSIGNMENTS, TestCategory.E2E, true)
    fun testAssignmentsE2E() {
        // Seed basic student/teacher/course data
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        // Seed some assignments
        val pointsTextAssignment = AssignmentsApi.createAssignment(AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                gradingType = GradingType.POINTS,
                teacherToken = teacher.token,
                pointsPossible = 15.0,
                dueAt = 1.days.fromNow.iso8601
        ))

        val letterGradeTextAssignment = AssignmentsApi.createAssignment(AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                gradingType = GradingType.LETTER_GRADE,
                teacherToken = teacher.token,
                pointsPossible = 20.0
        ))

        val submissionList = SubmissionsApi.seedAssignmentSubmission(SubmissionsApi.SubmissionSeedRequest(
                assignmentId = letterGradeTextAssignment.id,
                courseId = course.id,
                studentToken = student.token,
                submissionSeedsList = listOf(SubmissionsApi.SubmissionSeedInfo(
                        amount = 1,
                        submissionType = SubmissionType.ONLINE_TEXT_ENTRY
                ))
        ))

        val submissionGrade = SubmissionsApi.gradeSubmission(
                teacherToken = teacher.token,
                courseId = course.id,
                assignmentId = letterGradeTextAssignment.id,
                studentId = student.id,
                postedGrade = "16",
                excused = false
        )

        val percentageTextAssignment = AssignmentsApi.createAssignment(AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                gradingType = GradingType.PERCENT,
                teacherToken = teacher.token,
                pointsPossible = 25.0
        ))

        // Sign in with lone student
        tokenLogin(student)

        // Go into our course
        dashboardPage.waitForRender()
        dashboardPage.selectCourse(course)

        // Select the assignments tab
        courseBrowserPage.selectAssignments()

        // Verify that our assignments are present, along with any grade/date info
        assignmentListPage.assertHasAssignment(pointsTextAssignment)
        //println("submissionGrade: ${submissionGrade.grade}")
        assignmentListPage.assertHasAssignment(letterGradeTextAssignment, submissionGrade.grade)
        assignmentListPage.assertHasAssignment(percentageTextAssignment)

        // Let's submit one of the assignments
        assignmentListPage.clickAssignment(pointsTextAssignment)

        assignmentDetailsPage.pressSubmitButton()

    }
}
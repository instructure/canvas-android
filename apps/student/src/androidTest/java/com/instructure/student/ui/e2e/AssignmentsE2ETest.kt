package com.instructure.student.ui.e2e

import android.os.SystemClock.sleep
import androidx.test.espresso.Espresso
import androidx.test.rule.GrantPermissionRule
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.FileUploadType
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
import com.instructure.student.ui.utils.uploadTextFile
import org.junit.Rule
import org.junit.Test

class AssignmentsE2ETest: StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.CAMERA
    )

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.ASSIGNMENTS, TestCategory.E2E)
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

        val percentageFileAssignment = AssignmentsApi.createAssignment(AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                submissionTypes = listOf(SubmissionType.ONLINE_UPLOAD),
                gradingType = GradingType.PERCENT,
                teacherToken = teacher.token,
                pointsPossible = 25.0,
                allowedExtensions = listOf("txt", "pdf", "jpg")
        ))

        // Pre-seed a submission and a grade for the letter grade assignment
        SubmissionsApi.seedAssignmentSubmission(SubmissionsApi.SubmissionSeedRequest(
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


        // Sign in with lone student
        tokenLogin(student)

        // Go into our course
        dashboardPage.waitForRender()
        dashboardPage.selectCourse(course)

        // Select the assignments tab
        courseBrowserPage.selectAssignments()

        // Verify that our assignments are present, along with any grade/date info
        assignmentListPage.assertHasAssignment(pointsTextAssignment)
        assignmentListPage.assertHasAssignment(letterGradeTextAssignment, submissionGrade.grade)
        assignmentListPage.assertHasAssignment(percentageFileAssignment)

        // Let's submit a text assignment
        assignmentListPage.clickAssignment(pointsTextAssignment)

        SubmissionsApi.submitCourseAssignment(
                submissionType = SubmissionType.ONLINE_TEXT_ENTRY,
                courseId = course.id,
                assignmentId = pointsTextAssignment.id,
                studentToken = student.token,
                fileIds = emptyList<Long>().toMutableList()
        )

        assignmentDetailsPage.refresh()
        assignmentDetailsPage.verifyAssignmentSubmitted()

        // Let's grade the assignment
        val textGrade = SubmissionsApi.gradeSubmission(
                teacherToken = teacher.token,
                courseId = course.id,
                assignmentId = pointsTextAssignment.id,
                studentId = student.id,
                postedGrade = "13",
                excused = false
        )

        assignmentDetailsPage.refresh()
        assignmentDetailsPage.verifyAssignmentGraded("13")

        Espresso.pressBack() // Back to assignment list

        // Upload a text file for submission
        assignmentListPage.clickAssignment(percentageFileAssignment)
        val uploadInfo = uploadTextFile(
                courseId = course.id,
                assignmentId = percentageFileAssignment.id,
                token = student.token,
                fileUploadType = FileUploadType.ASSIGNMENT_SUBMISSION
        )

        // Submit the assignment
        SubmissionsApi.submitCourseAssignment(
                submissionType = SubmissionType.ONLINE_UPLOAD,
                courseId = course.id,
                assignmentId = percentageFileAssignment.id,
                fileIds = listOf(uploadInfo.id).toMutableList(),
                studentToken = student.token
        )

        // Verify that assignment has been submitted
        assignmentDetailsPage.refresh()
        assignmentDetailsPage.verifyAssignmentSubmitted()

        // Grade the assignment
        val fileGrade = SubmissionsApi.gradeSubmission(
                teacherToken = teacher.token,
                courseId = course.id,
                assignmentId = percentageFileAssignment.id,
                studentId = student.id,
                postedGrade = "22",
                excused = false
        )

        // Verify that the assignment has been graded
        assignmentDetailsPage.refresh()
        assignmentDetailsPage.verifyAssignmentGraded("22")

        // Back to assignment list page
        Espresso.pressBack()

        // Let's verify that the assignments in the list all have grades now
        assignmentListPage.refresh()
        assignmentListPage.assertHasAssignment(pointsTextAssignment, textGrade.grade)
        assignmentListPage.assertHasAssignment(letterGradeTextAssignment, submissionGrade.grade)
        assignmentListPage.assertHasAssignment(percentageFileAssignment, fileGrade.grade)

        // Let's make sure that comments are working
        assignmentListPage.clickAssignment(percentageFileAssignment)
        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.openComments()
        submissionDetailsPage.assertCommentDisplayed(
                uploadInfo.fileName,
                student)

        // Add a comment, make sure it shows up in the stream
        submissionDetailsPage.addAndSendComment("My comment!!")
        sleep(2000) // Give the comment time to propagate
        submissionDetailsPage.assertCommentDisplayed(
                "My comment!!",
                student
        )
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.COMMENTS, TestCategory.E2E)
    fun testMediaCommentsE2E() {
        // Seed basic student/teacher/course data
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        // Seed an assignment and a submission
        val assignment = AssignmentsApi.createAssignment(AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                gradingType = GradingType.POINTS,
                teacherToken = teacher.token,
                pointsPossible = 15.0,
                dueAt = 1.days.fromNow.iso8601
        ))

        val submission = SubmissionsApi.seedAssignmentSubmission(SubmissionsApi.SubmissionSeedRequest(
                assignmentId = assignment.id,
                courseId = course.id,
                studentToken = student.token,
                submissionSeedsList = listOf(SubmissionsApi.SubmissionSeedInfo(
                        amount = 1,
                        submissionType = SubmissionType.ONLINE_TEXT_ENTRY
                ))
        ))

        // Sign in with lone student
        tokenLogin(student)

        // Go into our course
        dashboardPage.waitForRender()
        dashboardPage.selectCourse(course)

        // Select the assignments tab
        courseBrowserPage.selectAssignments()

        // Select our assignment
        assignmentListPage.clickAssignment(assignment)

        // Open the submission details and the comments tab
        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.openComments()

        // send video comment
        submissionDetailsPage.addAndSendVideoComment()
        sleep(3000) // wait for video comment submission to propagate
        submissionDetailsPage.assertVideoCommentDisplayed()

        // send audio comment
        submissionDetailsPage.addAndSendAudioComment()
        sleep(3000) // wait for audio comment submission to propagate
        submissionDetailsPage.assertAudioCommentDisplayed()
    }

}
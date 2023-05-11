/*
 * Copyright (C) 2018 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
@file:Suppress("unused")

package com.emeritus.student.ui.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvas.espresso.waitForMatcherWithSleeps
import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.api.*
import com.instructure.dataseeding.model.*
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import com.instructure.dataseeding.util.Randomizer
import com.instructure.interactions.router.Route
import com.emeritus.student.R
import com.instructure.student.activity.LoginActivity
import com.emeritus.student.Router.RouteMatcher
import java.io.File
import java.io.FileWriter

const val SUB_ACCOUNT_ID = 181364L

fun StudentTest.enterDomain(enrollmentType: String = EnrollmentTypes.STUDENT_ENROLLMENT): CanvasUserApiModel {
    val user = UserApi.createCanvasUser()
    val course = CoursesApi.createCourse()
    EnrollmentsApi.enrollUser(course.id, user.id, enrollmentType)
    loginFindSchoolPage.enterDomain(user.domain)
    return user
}

fun StudentTest.slowLogIn(enrollmentType: String = EnrollmentTypes.STUDENT_ENROLLMENT): CanvasUserApiModel {
    loginLandingPage.clickFindMySchoolButton()
    val user = enterDomain(enrollmentType)
    loginFindSchoolPage.clickToolbarNextMenuItem()
    loginSignInPage.loginAs(user)
    return user
}

fun StudentTest.seedDataForK5(
    teachers: Int = 0,
    tas: Int = 0,
    pastCourses: Int = 0,
    courses: Int = 0,
    students: Int = 0,
    favoriteCourses: Int = 0,
    homeroomCourses: Int = 0,
    announcements: Int = 0,
    discussions: Int = 0,
    syllabusBody: String? = null,
    gradingPeriods: Boolean = false): SeedApi.SeededDataApiModel {

    val request = SeedApi.SeedDataRequest (
        teachers = teachers,
        TAs = tas,
        students = students,
        pastCourses = pastCourses,
        courses = courses,
        favoriteCourses = favoriteCourses,
        homeroomCourses = homeroomCourses,
        accountId = SUB_ACCOUNT_ID, //K5 Sub Account accountId on mobileqa.beta domain
        gradingPeriods = gradingPeriods,
        discussions = discussions,
        announcements = announcements,
        syllabusBody = syllabusBody
    )
    return SeedApi.seedDataForSubAccount(request)
}

fun StudentTest.seedData(
    teachers: Int = 0,
    tas: Int = 0,
    pastCourses: Int = 0,
    courses: Int = 0,
    students: Int = 0,
    favoriteCourses: Int = 0,
    homeroomCourses: Int = 0,
    announcements: Int = 0,
    locked: Boolean = false,
    discussions: Int = 0,
    syllabusBody: String? = null,
    gradingPeriods: Boolean = false): SeedApi.SeededDataApiModel {

    val request = SeedApi.SeedDataRequest (
            teachers = teachers,
            TAs = tas,
            students = students,
            pastCourses = pastCourses,
            courses = courses,
            favoriteCourses = favoriteCourses,
            homeroomCourses = homeroomCourses,
            gradingPeriods = gradingPeriods,
            discussions = discussions,
            announcements = announcements,
            locked = locked,
            syllabusBody = syllabusBody
    )
    return SeedApi.seedData(request)
}

fun StudentTest.seedAssignments(
        courseId: Long,
        assignments: Int = 1,
        withDescription: Boolean = false,
        lockAt: String = "",
        unlockAt: String = "",
        dueAt: String = "",
        submissionTypes: List<SubmissionType> = emptyList(),
        teacherToken: String): List<AssignmentApiModel> {

    return AssignmentsApi.seedAssignments(AssignmentsApi.CreateAssignmentRequest(
            courseId = courseId,
            withDescription = withDescription,
            lockAt = lockAt,
            unlockAt = unlockAt,
            dueAt = dueAt,
            submissionTypes = submissionTypes,
            teacherToken = teacherToken), assignments)
}

fun StudentTest.tokenLogin(user: CanvasUserApiModel) {
    activityRule.runOnUiThread {
        (originalActivity as LoginActivity).loginWithToken(
            user.token,
            user.domain,
            User(
                id = user.id,
                name = user.name,
                shortName = user.shortName,
                avatarUrl = user.avatarUrl,
                effective_locale = "en" // Needed so we don't restart for custom languages (system.exit(0) kills the test process)
            )
        )
    }
    dashboardPage.assertPageObjects()
}

fun StudentTest.tokenLogin(domain: String, token: String, user: User) {
    activityRule.runOnUiThread {
        (originalActivity as LoginActivity).loginWithToken(
            token,
            domain,
            user
        )
    }
    // Sometimes, especially on slow FTL emulators, it can take a bit for the dashboard to show
    // up after a token login.  Add some tolerance for that.
    waitForMatcherWithSleeps(withId(R.id.dashboardPage), 20000).check(matches(isDisplayed()))
    dashboardPage.assertPageObjects()
}

fun StudentTest.tokenLoginElementary(domain: String, token: String, user: User) {
    activityRule.runOnUiThread {
        (originalActivity as LoginActivity).loginWithToken(
            token,
            domain,
            user,
            canvasForElementary = true
        )
    }
    // Sometimes, especially on slow FTL emulators, it can take a bit for the dashboard to show
    // up after a token login.  Add some tolerance for that.
    waitForMatcherWithSleeps(withId(R.id.elementaryDashboardPage), 20000).check(matches(isDisplayed()))
    elementaryDashboardPage.assertPageObjects()
}

fun StudentTest.tokenLoginElementary(user: CanvasUserApiModel) {
    activityRule.runOnUiThread {
        (originalActivity as LoginActivity).loginWithToken(
            user.token,
            user.domain,
            User(
                id = user.id,
                name = user.name,
                shortName = user.shortName,
                avatarUrl = user.avatarUrl,
                effective_locale = "en" // Needed so we don't restart for custom languages (system.exit(0) kills the test process)
            ),
            canvasForElementary = true
        )
    }
    elementaryDashboardPage.assertPageObjects()
}

fun StudentTest.routeTo(route: String) {
    val url = "canvas-student://${CanvasNetworkAdapter.canvasDomain}/$route"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    if (context !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

fun StudentTest.routeTo(route: String, domain: String) {
    val url = "canvas-student://$domain/$route"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    if (context !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

fun StudentTest.routeTo(route: Route) {
    RouteMatcher.route(InstrumentationRegistry.getInstrumentation().targetContext, route)
}

fun StudentTest.seedAssignmentSubmission(
        submissionSeeds: List<SubmissionsApi.SubmissionSeedInfo>,
        assignmentId: Long,
        courseId: Long,
        studentToken: String,
        commentSeeds: List<SubmissionsApi.CommentSeedInfo> = kotlin.collections.emptyList()
): List<SubmissionApiModel> {

    // Upload one submission file for each submission seed
    submissionSeeds.forEach {
        it.attachmentsList.add(
                when (it.submissionType) {
                    SubmissionType.ONLINE_UPLOAD -> uploadTextFile(courseId, assignmentId, studentToken,
                        FileUploadType.ASSIGNMENT_SUBMISSION
                    )
                    else -> AttachmentApiModel(displayName="", fileName="", id=0L) // Not handled right now
                }
            );
    }

    // Add attachments to comment seeds
    commentSeeds.forEach {
        val fileAttachments: MutableList<AttachmentApiModel> = kotlin.collections.mutableListOf()

        for (i in 0..it.amount) {
            if (it.fileType != FileType.NONE) {
                fileAttachments.add(when (it.fileType) {
                    FileType.PDF -> kotlin.TODO()
                    FileType.TEXT -> uploadTextFile(courseId, assignmentId, studentToken,
                        FileUploadType.COMMENT_ATTACHMENT
                    )
                    else -> throw RuntimeException("Unknown file type passed into StudentTest.seedAssignmentSubmission") // Unknown type
                })
            }
        }

        it.attachmentsList.addAll(fileAttachments)
    }

    // Seed the submissions
    val submissionRequest = SubmissionsApi.SubmissionSeedRequest(
            assignmentId = assignmentId,
            courseId = courseId,
            studentToken = studentToken,
            commentSeedsList = commentSeeds,
            submissionSeedsList = submissionSeeds
    )

    return SubmissionsApi.seedAssignmentSubmission(submissionRequest)
}

fun StudentTest.uploadTextFile(courseId: Long, assignmentId: Long, token: String, fileUploadType: FileUploadType): AttachmentApiModel {

    // Create the file
    val file = File(
            Randomizer.randomTextFileName(Environment.getExternalStorageDirectory().absolutePath))
            .apply { createNewFile() }

    // Add contents to file
    FileWriter(file, true).apply {
        write(Randomizer.randomTextFileContents())
        flush()
        close()
    }

    // Start the Canvas file upload process
    return FileUploadsApi.uploadFile(
            courseId,
            assignmentId,
            file.readBytes(),
            file.name,
            token,
            fileUploadType)
}

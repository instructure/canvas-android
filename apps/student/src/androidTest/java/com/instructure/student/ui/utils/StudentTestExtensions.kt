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

package com.instructure.student.ui.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.fragment.app.FragmentActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvas.espresso.CanvasTest
import com.instructure.canvas.espresso.waitForMatcherWithSleeps
import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.api.EnrollmentsApi
import com.instructure.dataseeding.api.FileUploadsApi
import com.instructure.dataseeding.api.SeedApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.api.UserApi
import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.dataseeding.model.AttachmentApiModel
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.EnrollmentTypes
import com.instructure.dataseeding.model.FileType
import com.instructure.dataseeding.model.FileUploadType
import com.instructure.dataseeding.model.SubmissionApiModel
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import com.instructure.dataseeding.util.Randomizer
import com.instructure.interactions.router.Route
import com.instructure.student.R
import com.instructure.student.activity.LoginActivity
import com.instructure.student.router.RouteMatcher
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

fun StudentTest.openOverflowMenu() {
    Espresso.openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
}

fun seedDataForK5(
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
    gradingPeriods: Boolean = false
): SeedApi.SeededDataApiModel {

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

fun seedData(
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
    gradingPeriods: Boolean = false,
    modules: Int = 0
): SeedApi.SeededDataApiModel {

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
            syllabusBody = syllabusBody,
            modules = modules
    )
    return SeedApi.seedData(request)
}

fun seedAssignments(
    courseId: Long,
    assignments: Int = 1,
    withDescription: Boolean = false,
    lockAt: String = "",
    unlockAt: String = "",
    dueAt: String = "",
    submissionTypes: List<SubmissionType> = emptyList(),
    teacherToken: String
): List<AssignmentApiModel> {

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

fun CanvasTest.tokenLogin(domain: String, token: String, user: User) {
    activityRule.runOnUiThread {
        (originalActivity as LoginActivity).loginWithToken(
            token,
            domain,
            user
        )
    }
    // Sometimes, especially on slow FTL emulators, it can take a bit for the dashboard to show
    // up after a token login.  Add some tolerance for that.
    waitForMatcherWithSleeps(ViewMatchers.withId(R.id.dashboardPage), 20000).check(
        ViewAssertions.matches(
            ViewMatchers.isDisplayed()
        )
    )
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

fun routeTo(route: String) {
    val url = "canvas-student://${CanvasNetworkAdapter.canvasDomain}/$route"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    if (context !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

fun routeTo(route: String, domain: String) {
    val url = "canvas-student://$domain/$route"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    if (context !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

fun routeTo(route: Route, activity: FragmentActivity) {
    RouteMatcher.route(activity, route)
}

fun seedAssignmentSubmission(
    submissionSeeds: List<SubmissionsApi.SubmissionSeedInfo>,
    assignmentId: Long,
    courseId: Long,
    studentToken: String,
    commentSeeds: List<SubmissionsApi.CommentSeedInfo> = emptyList()
): List<SubmissionApiModel> {

    // Upload one submission file for each submission seed
    submissionSeeds.forEach {
        it.attachmentsList.add(
                when (it.submissionType) {
                    SubmissionType.ONLINE_UPLOAD -> uploadTextFile(
                        courseId, assignmentId, studentToken,
                        FileUploadType.ASSIGNMENT_SUBMISSION
                    )
                    else -> AttachmentApiModel(displayName="", fileName="", id=0L) // Not handled right now
                }
            )
    }

    // Add attachments to comment seeds
    commentSeeds.forEach {
        val fileAttachments: MutableList<AttachmentApiModel> = mutableListOf()

        for (i in 0..it.amount) {
            if (it.fileType != FileType.NONE) {
                fileAttachments.add(when (it.fileType) {
                    FileType.PDF -> TODO()
                    FileType.TEXT -> uploadTextFile(
                        courseId, assignmentId, studentToken,
                        FileUploadType.COMMENT_ATTACHMENT
                    )
                    else -> throw RuntimeException("Unknown file type passed into StudentTest.seedAssignmentSubmission") // Unknown type
                })
            }
        }

        it.attachmentsList.addAll(fileAttachments)
    }

    return SubmissionsApi.seedAssignmentSubmission(courseId, studentToken, assignmentId, commentSeeds, submissionSeeds)
}

fun uploadTextFile(
    courseId: Long,
    assignmentId: Long? = null,
    token: String,
    fileUploadType: FileUploadType
): AttachmentApiModel {

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
        fileUploadType
    )
}
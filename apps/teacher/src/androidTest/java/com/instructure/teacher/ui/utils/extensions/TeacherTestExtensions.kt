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

package com.instructure.teacher.ui.utils.extensions

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.fragment.app.FragmentActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvas.espresso.CanvasTest
import com.instructure.canvas.espresso.waitForMatcherWithSleeps
import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.ConversationsApi
import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.api.EnrollmentsApi
import com.instructure.dataseeding.api.FileUploadsApi
import com.instructure.dataseeding.api.PagesApi
import com.instructure.dataseeding.api.QuizzesApi
import com.instructure.dataseeding.api.SeedApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.api.UserApi
import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.dataseeding.model.AttachmentApiModel
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.ConversationListApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.dataseeding.model.EnrollmentTypes
import com.instructure.dataseeding.model.FileType
import com.instructure.dataseeding.model.FileUploadType
import com.instructure.dataseeding.model.PageApiModel
import com.instructure.dataseeding.model.QuizListApiModel
import com.instructure.dataseeding.model.QuizSubmissionApiModel
import com.instructure.dataseeding.model.SubmissionApiModel
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import com.instructure.dataseeding.util.DataSeedingException
import com.instructure.dataseeding.util.Randomizer
import com.instructure.interactions.router.Route
import com.instructure.teacher.R
import com.instructure.teacher.activities.LoginActivity
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.ui.utils.TeacherTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matchers.anyOf
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter


fun TeacherTest.enterDomain(enrollmentType: String = EnrollmentTypes.TEACHER_ENROLLMENT): CanvasUserApiModel {
    val user = UserApi.createCanvasUser()
    val course = CoursesApi.createCourse()
    EnrollmentsApi.enrollUser(course.id, user.id, enrollmentType)
    loginFindSchoolPage.enterDomain(user.domain)
    return user
}

fun TeacherTest.enterStudentDomain(): CanvasUserApiModel {
    val user = UserApi.createCanvasUser()
    val course = CoursesApi.createCourse()
    // TODO: Enroll user as student
    val enrollment = EnrollmentsApi.enrollUserAsStudent( course.id, user.id )
    loginFindSchoolPage.enterDomain(user.domain)
    return user
}

fun TeacherTest.slowLogIn(enrollmentType: String = EnrollmentTypes.TEACHER_ENROLLMENT): CanvasUserApiModel {
    loginLandingPage.clickFindMySchoolButton()
    val user = enterDomain(enrollmentType)
    loginFindSchoolPage.clickToolbarNextMenuItem()
    loginSignInPage.loginAs(user)
    return user
}

fun TeacherTest.slowLogInAsStudent(): CanvasUserApiModel = slowLogIn(EnrollmentTypes.STUDENT_ENROLLMENT)

fun TeacherTest.logIn(
    skipSplash: Boolean = true,
    enrollmentType: String = EnrollmentTypes.TEACHER_ENROLLMENT,
    noCourses: Boolean = false
): CanvasUserApiModel {
    val teacher = UserApi.createCanvasUser()
    if(!noCourses) {
        val course = CoursesApi.createCourse()
        EnrollmentsApi.enrollUser(course.id, teacher.id, enrollmentType)
    }

    activityRule.runOnUiThread {
        (activityRule.activity as LoginActivity).loginWithToken(
                teacher.token,
                teacher.domain,
                User(
                        id = teacher.id,
                        name = teacher.name,
                        shortName = teacher.shortName,
                        avatarUrl = teacher.avatarUrl,
                        locale = "en" // Must specify 'en' here to avoid restarting app due to locale change
                ),
                skipSplash
        )
    }
    return teacher
}

fun TeacherTest.seedData(
        teachers: Int = 0,
        courses: Int = 0,
        students: Int = 0,
        parents: Int = 0,
        favoriteCourses: Int = 0,
        announcements: Int = 0,
        discussions: Int = 0,
        gradingPeriods: Boolean = false,
        pastCourses: Int = 0,
        publishCourses: Boolean = true): SeedApi.SeededDataApiModel {

    val request = SeedApi.SeedDataRequest(
            teachers = teachers,
            courses = courses,
            students = students,
            parents = parents,
            favoriteCourses = favoriteCourses,
            announcements = announcements,
            discussions = discussions,
            gradingPeriods = gradingPeriods,
            pastCourses = pastCourses
    )

    return SeedApi.seedData(request)
}

fun TeacherTest.seedAssignments(
        courseId: Long,
        assignments: Int = 1,
        withDescription: Boolean = false,
        lockAt: String = "",
        unlockAt: String = "",
        dueAt: String = "",
        pointsPossible: Double? = null,
        submissionTypes: List<SubmissionType> = emptyList(),
        teacherToken: String): List<AssignmentApiModel> {

    val request = AssignmentsApi.CreateAssignmentRequest(
            courseId = courseId,
            withDescription = withDescription,
            lockAt = lockAt,
            unlockAt = unlockAt,
            dueAt = dueAt,
            submissionTypes = submissionTypes,
            teacherToken = teacherToken,
            pointsPossible = pointsPossible
    )

    return AssignmentsApi.seedAssignments(request, assignments)
}

// Must publish quiz after creating a question for that question to appear.
fun TeacherTest.seedQuizQuestion(
        courseId: Long,
        quizId: Long,
        teacherToken: String
) {
    QuizzesApi.createQuizQuestion(
            courseId = courseId,
            quizId = quizId,
            teacherToken = teacherToken
    )
}

fun TeacherTest.publishQuiz(courseId: Long,
                            quizId: Long,
                            teacherToken: String) {
    QuizzesApi.publishQuiz(
            courseId = courseId,
            quizId = quizId,
            teacherToken = teacherToken,
            published = true
    )
}

fun TeacherTest.seedQuizzes(
        courseId: Long,
        quizzes: Int = 1,
        withDescription: Boolean = false,
        lockAt: String = "",
        unlockAt: String = "",
        dueAt: String = "",
        published: Boolean = true,
        teacherToken: String): QuizListApiModel {

    return QuizzesApi.seedQuizzes(
            request = QuizzesApi.CreateQuizRequest(
                    courseId = courseId,
                    withDescription = withDescription,
                    published = published,
                    token = teacherToken,
                    lockAt = lockAt,
                    unlockAt = unlockAt,
                    dueAt = dueAt
            ),
            numQuizzes = quizzes
    )
}

// "you are not allowed to participate in this quiz" = make sure the quiz isn't Locked
fun TeacherTest.seedQuizSubmission(
        courseId: Long,
        quizId: Long,
        studentToken: String,
        complete: Boolean = true): QuizSubmissionApiModel {

    return QuizzesApi.seedQuizSubmission(
            request = QuizzesApi.CreateQuizSubmissionRequest(
                    courseId = courseId,
                    quizId = quizId,
                    studentToken = studentToken
            ),
            complete = complete
    )
}

fun TeacherTest.seedAssignmentSubmission(
        submissionSeeds: List<SubmissionsApi.SubmissionSeedInfo>,
        assignmentId: Long,
        courseId: Long,
        studentToken: String,
        commentSeeds: List<SubmissionsApi.CommentSeedInfo> = emptyList()): List<SubmissionApiModel> {

    // Upload one submission file for each submission seed
    // TODO: Add ability to upload more than one submission
    submissionSeeds.forEach {
        it.attachmentsList.add(
                when (it.submissionType) {
                    SubmissionType.ONLINE_UPLOAD -> uploadTextFile(courseId, assignmentId, studentToken, FileUploadType.ASSIGNMENT_SUBMISSION)
                    else -> AttachmentApiModel(displayName = "", fileName = "", id = 0L)  // Not handled right now
                }
        )
    }

    // Upload comment files
    // TODO: We could make this more granular, allowing multiple comments per seed with different file upload types
    commentSeeds.forEach {
        val fileAttachments: MutableList<AttachmentApiModel> = mutableListOf()

        for (i in 0..it.amount) {
            if (it.fileType != FileType.NONE) {
                fileAttachments.add(when (it.fileType) {
                    FileType.PDF -> TODO()
                    FileType.TEXT -> uploadTextFile(courseId, assignmentId, studentToken, FileUploadType.COMMENT_ATTACHMENT)
                    else -> throw RuntimeException("Unknown file type passed into TeacherTest.seedAssignmentSubmission") // Unknown type
                })
            }
        }

        it.attachmentsList.addAll(fileAttachments)
    }

    return SubmissionsApi.seedAssignmentSubmission(courseId, studentToken, assignmentId, commentSeeds, submissionSeeds)
}

fun TeacherTest.uploadTextFile(courseId: Long, assignmentId: Long? = null, token: String, fileUploadType: FileUploadType): AttachmentApiModel {

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

    return FileUploadsApi.uploadFile(
            courseId = courseId,
            assignmentId = assignmentId,
            token = token,
            fileName = file.name,
            file = file.toByteArray(),
            fileUploadType = fileUploadType
    )
}

fun TeacherTest.seedConversation(sender: CanvasUserApiModel, recipients: List<CanvasUserApiModel>): ConversationListApiModel {

    val returnedList = ConversationsApi.createConversation(
            token = sender.token,
            recipients = recipients.map { r -> r.id.toString() }
    )

    // Legacy: We need to convert the returned list of conversations to a ConversationListApiModel because
    // mockableSeed doesn't process List<> objects correctly.
    return ConversationListApiModel(conversations = returnedList)
}

fun TeacherTest.seedCoursePage(course: CourseApiModel, published: Boolean = true, frontPage: Boolean = false, teacher: CanvasUserApiModel): PageApiModel {
    if (frontPage && !published) {
        throw DataSeedingException("Front Page must be Published")
    }

    return PagesApi.createCoursePage(
            courseId = course.id,
            published = published,
            frontPage = frontPage,
            token = teacher.token
    )

}

val SeedApi.SeededDataApiModel.favoriteCourses: List<CourseApiModel>
    get() =
        this.favoriteCoursesList.map { fav ->
            this.coursesList.first { course ->
                course.id == fav.contextId
            }
        }

fun TeacherTest.tokenLogin(teacher: CanvasUserApiModel, skipSplash: Boolean = true) {
    activityRule.runOnUiThread {
        (activityRule.activity as LoginActivity).loginWithToken(
                teacher.token,
                teacher.domain,
                User(
                    id = teacher.id,
                    name = teacher.name,
                    shortName = teacher.shortName,
                    avatarUrl = teacher.avatarUrl,
                    locale = "en" // Must specify 'en' here to avoid restarting app due to locale change
                    ),
                skipSplash
        )
    }
    dashboardPage.assertPageObjects()
}

fun TeacherTest.tokenLogin(domain: String, token: String, user: User) {
    activityRule.runOnUiThread {
        (activityRule.activity as LoginActivity).loginWithToken(
                token,
                domain,
                user,
                true
        )
    }
    // Sometimes, especially on slow FTL emulators, it can take a bit for the dashboard to show
    // up after a token login.  Add some tolerance for that.
    waitForMatcherWithSleeps(anyOf(
                allOf(withId(R.id.courseRecyclerView), isDisplayed()),
                allOf(withId(R.id.emptyCoursesView), isDisplayed())),
            20000)
            .check(matches(isDisplayed()))
    dashboardPage.assertPageObjects()
}

fun CanvasTest.tokenLogin(domain: String, token: String, user: User) {
    activityRule.runOnUiThread {
        (activityRule.activity as LoginActivity).loginWithToken(
            token,
            domain,
            user,
            true
        )
    }
    // Sometimes, especially on slow FTL emulators, it can take a bit for the dashboard to show
    // up after a token login.  Add some tolerance for that.
    waitForMatcherWithSleeps(anyOf(
        allOf(withId(R.id.courseRecyclerView), isDisplayed()),
        allOf(withId(R.id.emptyCoursesView), isDisplayed())),
        20000)
        .check(matches(isDisplayed()))
}

fun TeacherTest.openOverflowMenu() {
    Espresso.openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
}

fun TeacherTest.logInAsStudent(): CanvasUserApiModel {
    loginLandingPage.clickFindMySchoolButton()
    val student = enterStudentDomain()
    loginFindSchoolPage.clickToolbarNextMenuItem()
    loginSignInPage.loginAs(student)
    return student
}

fun File.toByteArray(): ByteArray {
    val size = this.length().toInt()
    val bytes: ByteArray = ByteArray(size)

    val bufferInputStream = BufferedInputStream(FileInputStream(this))
    val dataInputStream = DataInputStream(bufferInputStream)
    dataInputStream.readFully(bytes)

    return bytes
}

fun TeacherTest.routeTo(route: String) {
    val url = "canvas-teacher://${CanvasNetworkAdapter.canvasDomain}/$route"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    if (context !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

fun TeacherTest.routeTo(route: Route, activity: FragmentActivity) {
    RouteMatcher.route(activity, route)
}

/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.ui.e2e

import android.util.Log
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.api.EnrollmentsApi
import com.instructure.dataseeding.api.SeedApi
import com.instructure.dataseeding.api.UserApi
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.dataseeding.model.EnrollmentTypes.STUDENT_ENROLLMENT
import com.instructure.dataseeding.model.EnrollmentTypes.TEACHER_ENROLLMENT
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.ViewUtils
import com.instructure.student.ui.utils.seedData
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class LoginE2ETest : StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enableAndConfigureAccessibilityChecks() {
        //We don't want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.LOGIN, TestCategory.E2E)
    fun testLoginE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, teachers = 1, courses = 1)
        val student1 = data.studentsList[0]
        val student2 = data.studentsList[1]

        Log.d(STEP_TAG,"Login with user: ${student1.name}, login id: ${student1.loginId}.")
        loginWithUser(student1)

        Log.d(STEP_TAG,"Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        assertDashboardPageDisplayed(student1)

        Log.d(STEP_TAG,"Log out with ${student1.name} student.")
        dashboardPage.logOut()

        Log.d(STEP_TAG,"Login with user: ${student2.name}, login id: ${student2.loginId}.")
        loginWithUser(student2)

        Log.d(STEP_TAG,"Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        assertDashboardPageDisplayed(student2)

        Log.d(STEP_TAG,"Click on 'Change User' button on the left-side menu.")
        dashboardPage.pressChangeUser()

        Log.d(STEP_TAG,"Assert that the previously logins has been displayed.")
        loginLandingPage.assertDisplaysPreviousLogins()

        Log.d(STEP_TAG,"Login with user: ${student1.name}, login id: ${student1.loginId}.")
        loginWithUser(student1)

        Log.d(STEP_TAG,"Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        assertDashboardPageDisplayed(student1)

        Log.d(STEP_TAG,"Click on 'Change User' button on the left-side menu.")
        dashboardPage.pressChangeUser()

        Log.d(STEP_TAG,"Assert that the previously logins has been displayed. Assert that ${student1.name} and ${student2.name} students are displayed within the previous login section.")
        loginLandingPage.assertDisplaysPreviousLogins()
        loginLandingPage.assertPreviousLoginUserDisplayed(student1.name)
        loginLandingPage.assertPreviousLoginUserDisplayed(student2.name)

        Log.d(STEP_TAG,"Remove ${student1.name} student from the previous login section.")
        loginLandingPage.removeUserFromPreviousLogins(student1.name)

        Log.d(STEP_TAG,"Login with the previous user, ${student2.name}, with one click, by clicking on the user's name on the bottom.")
        loginLandingPage.loginWithPreviousUser(student2)

        Log.d(STEP_TAG,"Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        assertDashboardPageDisplayed(student2)

        Log.d(STEP_TAG,"Click on 'Change User' button on the left-side menu.")
        dashboardPage.pressChangeUser()

        Log.d(STEP_TAG,"Assert that the previously logins has been displayed. Assert that ${student1.name} and ${student2.name} students are displayed within the previous login section.")
        loginLandingPage.assertDisplaysPreviousLogins()
        loginLandingPage.assertPreviousLoginUserDisplayed(student2.name)

        Log.d(STEP_TAG,"Remove ${student2.name} student from the previous login section.")
        loginLandingPage.removeUserFromPreviousLogins(student2.name)

        Log.d(STEP_TAG,"Assert that none of the students, ${student1.name} and ${student2.name} are displayed and not even the 'Previous Logins' label is displayed.")
        loginLandingPage.assertPreviousLoginUserNotExist(student1.name)
        loginLandingPage.assertPreviousLoginUserNotExist(student2.name)
        loginLandingPage.assertNotDisplaysPreviousLogins()

    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.LOGIN, TestCategory.E2E)
    fun testUserRolesLoginE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, tas = 1, courses = 1)
        val parentData = SeedApi.seedParentData(
                SeedApi.SeedParentDataRequest(
                        courses=1, students=1, parents=1
                )
        )
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val ta = data.taList[0]
        val course = data.coursesList[0]
        val parent = parentData.parentsList[0]  //Test with Parent user. parents don't show up in the "People" page so we can't verify their role.

        Log.d(STEP_TAG,"Login with user: ${student.name}, login id: ${student.loginId}.")
        loginWithUser(student)

        Log.d(STEP_TAG,"Validate ${student.name} user's role as a Student.")
        validateUserAndRole(student, course, "Student")

        Log.d(STEP_TAG,"Navigate back to Dashboard Page.")
        ViewUtils.pressBackButton(2)

        Log.d(STEP_TAG,"Log out with ${student.name} student.")
        dashboardPage.logOut()

        Log.d(STEP_TAG,"Login with user: ${teacher.name}, login id: ${teacher.loginId}.")
        loginWithUser(teacher)

        Log.d(STEP_TAG,"Validate ${teacher.name} user's role as a Teacher.")
        validateUserAndRole(teacher, course, "Teacher")

        Log.d(STEP_TAG,"Navigate back to Dashboard Page.")
        ViewUtils.pressBackButton(2)

        Log.d(STEP_TAG,"Log out with ${teacher.name} teacher.")
        dashboardPage.logOut()

        Log.d(STEP_TAG,"Login with user: ${ta.name}, login id: ${ta.loginId}.")
        loginWithUser(ta)

        Log.d(STEP_TAG,"Validate ${ta.name} user's role as a TA.")
        validateUserAndRole(ta, course, "TA")

        Log.d(STEP_TAG,"Navigate back to Dashboard Page.")
        ViewUtils.pressBackButton(2)

        Log.d(STEP_TAG,"Log out with ${ta.name} teacher assistant.")
        dashboardPage.logOut()

        Log.d(STEP_TAG,"Login with user: ${parent.name}, login id: ${parent.loginId}.")
        loginWithUser(parent)

        Log.d(STEP_TAG,"Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        assertDashboardPageDisplayed(parent)

        Log.d(STEP_TAG,"Log out with ${parent.name} parent.")
        dashboardPage.logOut()
    }

    // Verify that students can sign into vanity domain
    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.LOGIN, TestCategory.E2E)
    fun testVanityDomainLoginE2E() {
        // Create a Retrofit client for our vanity domain
        val domain = "canvas.beta.jitops.computer" // Our test vanity domain
        val retrofitClient = CanvasNetworkAdapter.createAdminRetrofitClient(domain)

        Log.d(PREPARATION_TAG,"Create services off of that Retrofit client.")
        val userService = retrofitClient.create(UserApi.UserService::class.java)
        val coursesService = retrofitClient.create(CoursesApi.CoursesService::class.java)
        val enrollmentsService = retrofitClient.create(EnrollmentsApi.EnrollmentsService::class.java)

        Log.d(PREPARATION_TAG,"Create student, teacher, and a course via API.")
        val student = UserApi.createCanvasUser(userService = userService, userDomain = domain)
        val teacher = UserApi.createCanvasUser(userService = userService, userDomain = domain)
        val course = CoursesApi.createCourse(coursesService = coursesService)

        Log.d(PREPARATION_TAG,"Enroll ${student.name} student to ${course.name} course.")
        EnrollmentsApi.enrollUser(
                courseId = course.id,
                userId = student.id,
                enrollmentType = STUDENT_ENROLLMENT,
                enrollmentService = enrollmentsService
        )

        Log.d(PREPARATION_TAG,"Enroll ${teacher.name} teacher to ${course.name} course.")
        EnrollmentsApi.enrollUser(
                courseId = course.id,
                userId = teacher.id,
                enrollmentType = TEACHER_ENROLLMENT,
                enrollmentService = enrollmentsService
        )

        Log.d(STEP_TAG,"Login with user: ${student.name}, login id: ${student.loginId}.")
        loginWithUser(student)

        Log.d(STEP_TAG,"Attempt to sign into our vanity domain, and validate ${student.name} user's role as a Student.")
        validateUserAndRole(student, course,"Student" )

        Log.d(STEP_TAG,"Navigate back to Dashboard Page.")
        ViewUtils.pressBackButton(2)

        Log.d(STEP_TAG,"Log out with ${student.name} student.")
        dashboardPage.logOut()
    }

    private fun loginWithUser(user: CanvasUserApiModel) {
        Log.d(STEP_TAG,"Click 'Find My School' button.")
        loginLandingPage.clickFindMySchoolButton()

        Log.d(STEP_TAG,"Enter domain: ${user.domain}.")
        loginFindSchoolPage.enterDomain(user.domain)

        Log.d(STEP_TAG,"Click on 'Next' button on the Toolbar.")
        loginFindSchoolPage.clickToolbarNextMenuItem()
        loginSignInPage.loginAs(user)
    }

    private fun validateUserAndRole(user: CanvasUserApiModel, course: CourseApiModel, role: String) {

        Log.d(STEP_TAG,"Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        assertDashboardPageDisplayed(user)

        Log.d(STEP_TAG,"Navigate to 'People' Page of ${course.name} course.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectPeople()

        Log.d(STEP_TAG,"Assert that ${user.name} user's role is: $role.")
        peopleListPage.assertPersonListed(user, role)

    }

    private fun assertDashboardPageDisplayed(user: CanvasUserApiModel)
    {
        dashboardPage.waitForRender()
        dashboardPage.assertUserLoggedIn(user)
        dashboardPage.assertDisplaysCourses()
        dashboardPage.assertPageObjects()
    }
}
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
package com.instructure.student.ui.e2e.classic

import android.util.Log
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.canvas.espresso.annotations.Stub
import com.instructure.canvas.espresso.pressBackButton
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.api.EnrollmentsApi
import com.instructure.dataseeding.api.SeedApi
import com.instructure.dataseeding.api.UserApi
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.dataseeding.model.EnrollmentTypes.STUDENT_ENROLLMENT
import com.instructure.dataseeding.model.EnrollmentTypes.TEACHER_ENROLLMENT
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import com.instructure.espresso.withIdlingResourceDisabled
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.extensions.enterDomain
import com.instructure.student.ui.utils.extensions.seedData
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class LoginE2ETest : StudentTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.LOGIN, TestCategory.E2E)
    fun testLoginE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, teachers = 1, courses = 1)
        val student1 = data.studentsList[0]
        val student2 = data.studentsList[1]

        Log.d(STEP_TAG, "Login with user: '${student1.name}', login id: '${student1.loginId}'.")
        loginWithUser(student1)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        assertDashboardPageDisplayed(student1)

        Log.d(STEP_TAG, "Log out with '${student1.name}' student.")
        leftSideNavigationDrawerPage.logout()

        Log.d(STEP_TAG, "Login with user: '${student2.name}', login id: '${student2.loginId}'.")
        loginWithUser(student2, true)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        assertDashboardPageDisplayed(student2)

        Log.d(STEP_TAG, "Click on 'Change User' button on the left-side menu.")
        leftSideNavigationDrawerPage.clickChangeUserMenu()

        Log.d(ASSERTION_TAG, "Assert that the previously logins has been displayed.")
        loginLandingPage.assertDisplaysPreviousLogins()

        Log.d(STEP_TAG, "Login with user: '${student1.name}', login id: '${student1.loginId}'.")
        loginWithUser(student1, true)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        assertDashboardPageDisplayed(student1)

        Log.d(STEP_TAG, "Click on 'Change User' button on the left-side menu.")
        leftSideNavigationDrawerPage.clickChangeUserMenu()

        Log.d(ASSERTION_TAG, "Assert that the previously logins has been displayed. Assert that '${student1.name}' and '${student2.name}' students are displayed within the previous login section.")
        loginLandingPage.assertDisplaysPreviousLogins()
        loginLandingPage.assertPreviousLoginUserDisplayed(student1.name)
        loginLandingPage.assertPreviousLoginUserDisplayed(student2.name)

        Log.d(STEP_TAG, "Remove '${student1.name}' student from the previous login section.")
        loginLandingPage.removeUserFromPreviousLogins(student1.name)

        Log.d(STEP_TAG, "Login with the previous user, '${student2.name}', with one click, by clicking on the user's name on the bottom.")
        loginLandingPage.loginWithPreviousUser(student2)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        assertDashboardPageDisplayed(student2)

        Log.d(STEP_TAG, "Click on 'Change User' button on the left-side menu.")
        leftSideNavigationDrawerPage.clickChangeUserMenu()

        Log.d(ASSERTION_TAG, "Assert that the previously logins has been displayed. Assert that '${student1.name}' and '${student2.name}' students are displayed within the previous login section.")
        loginLandingPage.assertDisplaysPreviousLogins()
        loginLandingPage.assertPreviousLoginUserDisplayed(student2.name)

        Log.d(STEP_TAG, "Remove '${student2.name}' student from the previous login section.")
        loginLandingPage.removeUserFromPreviousLogins(student2.name)

        Log.d(ASSERTION_TAG, "Assert that none of the students, '${student1.name}' and '${student2.name}' are displayed and not even the 'Previous Logins' label is displayed.")
        loginLandingPage.assertPreviousLoginUserNotExist(student1.name)
        loginLandingPage.assertPreviousLoginUserNotExist(student2.name)
        loginLandingPage.assertNotDisplaysPreviousLogins()
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.LOGIN, TestCategory.E2E)
    fun testLoginE2EWithLastSavedSchool() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, teachers = 1, courses = 1)
        val student1 = data.studentsList[0]
        val student2 = data.studentsList[1]

        Log.d(STEP_TAG, "Login with user: '${student1.name}', login id: '${student1.loginId}'.")
        loginWithUser(student1)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        assertDashboardPageDisplayed(student1)

        Log.d(STEP_TAG, "Log out with '${student1.name}' student.")
        leftSideNavigationDrawerPage.logout()

        Log.d(STEP_TAG, "Login with user: '${student2.name}', login id: '${student2.loginId}'.")
        loginWithLastSavedSchool(student2)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        assertDashboardPageDisplayed(student2)
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

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        loginWithUser(student)

        Log.d(STEP_TAG, "Validate '${student.name}' user's role as a Student.")
        validateUserAndRole(student, course, "Student")

        Log.d(STEP_TAG, "Navigate back to Dashboard Page.")
        pressBackButton(2)

        Log.d(STEP_TAG, "Log out with '${student.name}' student.")
        leftSideNavigationDrawerPage.logout()

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        loginWithUser(teacher, true)

        Log.d(STEP_TAG, "Validate '${teacher.name}' user's role as a Teacher.")
        validateUserAndRole(teacher, course, "Teacher")

        Log.d(STEP_TAG, "Navigate back to Dashboard Page.")
        pressBackButton(2)

        Log.d(STEP_TAG, "Log out with '${teacher.name}' teacher.")
        leftSideNavigationDrawerPage.logout()

        Log.d(STEP_TAG, "Login with user: '${ta.name}', login id: '${ta.loginId}'.")
        loginWithUser(ta, true)

        Log.d(STEP_TAG, "Validate '${ta.name}' user's role as a TA (Teacher Assistant).")
        validateUserAndRole(ta, course, "TA")

        Log.d(STEP_TAG, "Navigate back to Dashboard Page.")
        pressBackButton(2)

        Log.d(STEP_TAG, "Log out with '${ta.name}' teacher assistant.")
        leftSideNavigationDrawerPage.logout()

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        loginWithUser(parent, true)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        assertDashboardPageDisplayed(parent)

        Log.d(STEP_TAG, "Log out with '${parent.name}' parent.")
        leftSideNavigationDrawerPage.logout()
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.LOGIN, TestCategory.E2E, SecondaryFeatureCategory.ACCESS_TOKEN_EXPIRATION)
    fun testTokenExpirationForcedLogoutThenLogBackE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, teachers = 1, courses = 1)
        val student1 = data.studentsList[0]
        val course = data.coursesList[0]

        Log.d(STEP_TAG, "Login with user: '${student1.name}', login id: '${student1.loginId}'.")
        loginWithUser(student1)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        dashboardPage.waitForRender()

        Log.d(PREPARATION_TAG, "Delete the '${student1.name}' student's (valid) access token which has been created by the login mechanism.")
        UserApi.deleteToken(ApiPrefs.accessToken)

        withIdlingResourceDisabled {

            Log.d(STEP_TAG, "Try to make some action with expired (deleted) token, for example, select '${course.name}' course on the Dashboard Page.")
            dashboardPage.selectCourse(course)

            Log.d(ASSERTION_TAG, "Assert that the 'Login Required' dialog is displayed because the session (aka. access token) is expired/deleted, so we drop out the user when any action has been made.")
            dashboardPage.assertLoginRequiredDialog()

            Log.d(STEP_TAG, "Click on the 'LOG IN' button on the 'Login Required' dialog.")
            dashboardPage.clickLogInOnLoginRequiredDialog()

            Log.d(STEP_TAG, "Log back with the SAME user: '${student1.name}', login id: '${student1.loginId}'.")
            loginSignInPage.loginAs(student1)

            Log.d(ASSERTION_TAG, "Assert that the Course Browser Page will be displayed and loaded correctly because that was the 'target' page of the last action which before dropping the user out.")
            courseBrowserPage.assertPageObjects()
            courseBrowserPage.assertInitialBrowserTitle(course)
        }
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.LOGIN, TestCategory.E2E)
    fun testInvalidAndEmptyLoginCredentialsE2E() {

        val INVALID_USERNAME = "invalidusercred@test.com"
        val INVALID_PASSWORD = "invalidpw"
        val INVALID_CREDENTIALS_ERROR_MESSAGE = "Please verify your username or password and try again. Trouble logging in? Check out our Login FAQs."
        val NO_PASSWORD_GIVEN_ERROR_MESSAGE = "No password was given"
        val DOMAIN = "mobileqa.beta"

        Log.d(STEP_TAG, "Click 'Find My School' button.")
        loginLandingPage.clickFindMySchoolButton()

        Log.d(STEP_TAG, "Enter domain: $DOMAIN.instructure.com.")
        loginFindSchoolPage.enterDomain(DOMAIN)

        Log.d(STEP_TAG, "Click on 'Next' button on the Toolbar.")
        loginFindSchoolPage.clickToolbarNextMenuItem()

        Log.d(STEP_TAG, "Try to login with invalid, non-existing credentials ('$INVALID_USERNAME', '$INVALID_PASSWORD').")
        loginSignInPage.loginAs(INVALID_USERNAME, INVALID_PASSWORD)

        Log.d(ASSERTION_TAG, "Assert that the invalid credentials error message is displayed.")
        loginSignInPage.assertLoginErrorMessage(INVALID_CREDENTIALS_ERROR_MESSAGE)

        Log.d(STEP_TAG, "Try to login with no credentials typed in either of the username and password field.")
        loginSignInPage.loginAs(EMPTY_STRING, EMPTY_STRING)

        Log.d(ASSERTION_TAG, "Assert that the no password was given error message is displayed.")
        loginSignInPage.assertLoginErrorMessage(NO_PASSWORD_GIVEN_ERROR_MESSAGE)

        Log.d(STEP_TAG, "Try to login with leaving only the password field empty.")
        loginSignInPage.loginAs(INVALID_USERNAME, EMPTY_STRING)

        Log.d(ASSERTION_TAG, "Assert that the no password was given error message is displayed.")
        loginSignInPage.assertLoginErrorMessage(NO_PASSWORD_GIVEN_ERROR_MESSAGE)

        Log.d(STEP_TAG, "Try to login with leaving only the username field empty.")
        loginSignInPage.loginAs(EMPTY_STRING, INVALID_PASSWORD)

        Log.d(ASSERTION_TAG, "Assert that the invalid credentials error message is displayed.")
        loginSignInPage.assertLoginErrorMessage(INVALID_CREDENTIALS_ERROR_MESSAGE)
    }

    // Verify that students can sign into vanity domain
    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.LOGIN, TestCategory.E2E)
    fun testVanityDomainLoginE2E() {
        // Create a Retrofit client for our vanity domain
        val domain = "canvas.beta.jitops.computer" // Our test vanity domain
        val retrofitClient = CanvasNetworkAdapter.createAdminRetrofitClient(domain)

        Log.d(PREPARATION_TAG, "Create services off of that Retrofit client.")
        val userService = retrofitClient.create(UserApi.UserService::class.java)
        val coursesService = retrofitClient.create(CoursesApi.CoursesService::class.java)
        val enrollmentsService = retrofitClient.create(EnrollmentsApi.EnrollmentsService::class.java)

        Log.d(PREPARATION_TAG, "Create student, teacher, and a course via API.")
        val student = UserApi.createCanvasUser(userService, domain)
        val teacher = UserApi.createCanvasUser(userService, domain)
        val course = CoursesApi.createCourse(coursesService = coursesService)

        Log.d(PREPARATION_TAG, "Enroll '${student.name}' student to '${course.name}' course.")
        EnrollmentsApi.enrollUser(course.id, student.id, STUDENT_ENROLLMENT, enrollmentsService)

        Log.d(PREPARATION_TAG, "Enroll '${teacher.name}' teacher to '${course.name}' course.")
        EnrollmentsApi.enrollUser(course.id, teacher.id, TEACHER_ENROLLMENT, enrollmentsService)

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        loginWithUser(student)

        Log.d(STEP_TAG, "Attempt to sign into our vanity domain, and validate '${student.name}' user's role as a Student.")
        validateUserAndRole(student, course,"Student" )

        Log.d(STEP_TAG, "Navigate back to Dashboard Page.")
        pressBackButton(2)

        Log.d(STEP_TAG, "Log out with '${student.name}' student.")
        leftSideNavigationDrawerPage.logout()
    }

    @Test
    @Stub("Stubbed because there was some change on 7th or 8th of July, 2025 and on the CI it loads an invalid URL page, however the test runs locally.")
    @E2E
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.LOGIN, TestCategory.E2E, SecondaryFeatureCategory.CANVAS_NETWORK)
    fun testCanvasNetworkSignInPageE2E() {

        Log.d(STEP_TAG, "Click on the 'Canvas Network' link on the Login Landing Page to open the Canvas Network Page (learn.canvas.net).")
        loginLandingPage.clickCanvasNetworkButton()

        Log.d(ASSERTION_TAG, "Assert that the Canvas Network Page has been displayed.")
        canvasNetworkSignInPage.assertPageObjects()
    }

    @Test
    fun testFindSchoolPageObjects() {

        Log.d(STEP_TAG, "Click 'Find My School' button.")
        loginLandingPage.clickFindMySchoolButton()

        Log.d(ASSERTION_TAG, "Assert that the Find School Page has been displayed.")
        loginFindSchoolPage.assertPageObjects()
    }

    @Test
    fun testLoginLandingPageObjects() {

        Log.d(ASSERTION_TAG, "Assert that the Login Landing Page has been displayed.")
        loginLandingPage.assertPageObjects()
    }

    @Test
    fun testLoginSignInPageObjects() {

        Log.d(STEP_TAG, "Click 'Find My School' button.")
        loginLandingPage.clickFindMySchoolButton()

        Log.d(STEP_TAG, "Enter domain: 'mobileqa.beta.instructure.com', and click on the 'Next' button on the toolbar.")
        enterDomain()
        loginFindSchoolPage.clickToolbarNextMenuItem()

        Log.d(ASSERTION_TAG, "Assert that the Login SignIn Page has been displayed.")
        loginSignInPage.assertPageObjects()
    }

    private fun loginWithUser(user: CanvasUserApiModel, lastSchoolSaved: Boolean = false) {

        Thread.sleep(5100) //Need to wait > 5 seconds before each login attempt because of new 'too many attempts' login policy on web.

        if(lastSchoolSaved) {
            Log.d(STEP_TAG, "Click 'Find Another School' button.")
            loginLandingPage.clickFindAnotherSchoolButton()
        }
        else {
            Log.d(STEP_TAG, "Click 'Find My School' button.")
            loginLandingPage.clickFindMySchoolButton()
        }

        Log.d(STEP_TAG, "Enter domain: '${user.domain}'.")
        loginFindSchoolPage.enterDomain(user.domain)

        Log.d(STEP_TAG, "Click on 'Next' button on the Toolbar and login as user.")
        loginFindSchoolPage.clickToolbarNextMenuItem()
        loginSignInPage.loginAs(user)
    }

    private fun loginWithLastSavedSchool(user: CanvasUserApiModel) {

        Log.d(STEP_TAG, "Click on last saved school's button.")
        loginLandingPage.clickOnLastSavedSchoolButton()

        Log.d(STEP_TAG, "Login with '${user.name}' user.")
        loginSignInPage.loginAs(user)
    }

    private fun validateUserAndRole(user: CanvasUserApiModel, course: CourseApiModel, role: String) {

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        assertDashboardPageDisplayed(user)

        Log.d(STEP_TAG, "Navigate to 'People' Page of '${course.name}' course.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectPeople()

        Log.d(ASSERTION_TAG, "Assert that '${user.name}' user's role is: '$role'.")
        peopleListPage.assertPersonListed(user, role)
    }

    private fun assertDashboardPageDisplayed(user: CanvasUserApiModel)
    {
        dashboardPage.waitForRender()
        leftSideNavigationDrawerPage.assertUserLoggedIn(user)
        dashboardPage.assertDisplaysCourses()
        dashboardPage.assertPageObjects()
    }

}
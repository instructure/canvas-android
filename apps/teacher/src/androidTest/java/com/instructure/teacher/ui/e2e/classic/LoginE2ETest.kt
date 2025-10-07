/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.teacher.ui.e2e.classic

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
import com.instructure.dataseeding.api.SeedApi
import com.instructure.dataseeding.api.UserApi
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.espresso.withIdlingResourceDisabled
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.extensions.seedData
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class LoginE2ETest : TeacherTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.LOGIN, TestCategory.E2E)
    fun testLoginE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 2, courses = 1)
        val teacher1 = data.teachersList[0]
        val teacher2 = data.teachersList[1]
        val course = data.coursesList[0]

        Log.d(STEP_TAG, "Login with user: '${teacher1.name}', login id: '${teacher1.loginId}'.")
        loginWithUser(teacher1)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        assertSuccessfulLogin(teacher1)

        Log.d(STEP_TAG, "Validate '${teacher1.name}' user's role as a Teacher.")
        validateUserRole(teacher1, course, "Teacher")

        Log.d(STEP_TAG, "Log out with '${teacher1.name}' student.")
        leftSideNavigationDrawerPage.logout()

        Log.d(STEP_TAG, "Login with user: '${teacher2.name}', login id: '${teacher2.loginId}'.")
        loginWithUser(teacher2, true)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        assertSuccessfulLogin(teacher2)

        Log.d(STEP_TAG, "Click on 'Change User' button on the left-side menu.")
        leftSideNavigationDrawerPage.clickChangeUserMenu()

        Log.d(ASSERTION_TAG, "Assert that the previously logins has been displayed.")
        loginLandingPage.assertDisplaysPreviousLogins()

        Log.d(STEP_TAG, "Login with user: '${teacher1.name}', login id: '${teacher1.loginId}'.")
        loginWithUser(teacher1, true)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        assertSuccessfulLogin(teacher1)

        Log.d(STEP_TAG, "Click on 'Change User' button on the left-side menu.")
        leftSideNavigationDrawerPage.clickChangeUserMenu()

        Log.d(ASSERTION_TAG, "Assert that the previously logins has been displayed.")
        loginLandingPage.assertDisplaysPreviousLogins()

        Log.d(STEP_TAG, "Login with the previous user, '${teacher2.name}', with one click, by clicking on the user's name on the bottom.")
        loginLandingPage.loginWithPreviousUser(teacher2)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        assertSuccessfulLogin(teacher2)
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.LOGIN, TestCategory.E2E)
    fun testLoginWithNotTeacherRole() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val parentData = SeedApi.seedParentData(
                SeedApi.SeedParentDataRequest(
                        courses=1, students=1, parents=1
                )
        )
        val student = data.studentsList[0]
        val parent = parentData.parentsList[0]

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        loginWithUser(student)

        Log.d(ASSERTION_TAG, "Assert that the user has been landed on 'Not a teacher?' Page.")
        notATeacherPage.assertPageObjects()

        Log.d(STEP_TAG, "Navigate back to the Teacher app's Login Landing Page's screen.")
        notATeacherPage.clickOnLoginButton()

        Log.d(ASSERTION_TAG, "Assert the Teacher app's Login Landing Page's screen is displayed.")
        loginLandingPage.assertPageObjects()

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        loginWithUser(parent, true)

        Log.d(ASSERTION_TAG, "Assert that the user has been landed on 'Not a teacher?' Page.")
        notATeacherPage.assertPageObjects()

        Log.d(STEP_TAG, "Navigate back to the Teacher app's login screen.")
        notATeacherPage.clickOnLoginButton()

        Log.d(ASSERTION_TAG, "Assert that the user has landed on Teacher app's Login Landing Page's screen.")
        loginLandingPage.assertPageObjects()
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.LOGIN, TestCategory.E2E)
    fun testLoginE2EWithLastSavedSchool() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 2, courses = 1)
        val teacher1 = data.teachersList[0]
        val teacher2 = data.teachersList[1]

        Log.d(STEP_TAG, "Login with user: '${teacher1.name}', login id: '${teacher1.loginId}'.")
        loginWithUser(teacher1)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        assertSuccessfulLogin(teacher1)

        Log.d(STEP_TAG, "Log out with '${teacher1.name}' student.")
        leftSideNavigationDrawerPage.logout()

        Log.d(STEP_TAG, "Login with user: '${teacher2.name}', login id: '${teacher2.loginId}', via the last saved school's button.")
        loginWithLastSavedSchool(teacher2)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        assertSuccessfulLogin(teacher2)
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.LOGIN, TestCategory.E2E, SecondaryFeatureCategory.ACCESS_TOKEN_EXPIRATION)
    fun testTokenExpirationForcedLogoutThenLogBackE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        loginWithUser(teacher)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        dashboardPage.waitForRender()

        Log.d(PREPARATION_TAG, "Delete the '${teacher.name}' teacher's (valid) access token which has been created by the login mechanism.")
        UserApi.deleteToken(ApiPrefs.accessToken)

        withIdlingResourceDisabled {

            Log.d(STEP_TAG, "Try to make some action with expired (deleted) token, for example, select '${course.name}' course on the Dashboard Page.")
            dashboardPage.selectCourse(course)

            Log.d(ASSERTION_TAG, "Assert that the 'Login Required' dialog is displayed because the session (aka. access token) is expired/deleted, so we drop out the user when any action has been made.")
            dashboardPage.assertLoginRequiredDialog()

            Log.d(STEP_TAG, "Click on the 'LOG IN' button on the 'Login Required' dialog.")
            dashboardPage.clickLogInOnLoginRequiredDialog()

            Log.d(STEP_TAG, "Log back with the SAME user: '${teacher.name}', login id: '${teacher.loginId}'.")
            loginSignInPage.loginAs(teacher)

            Log.d(ASSERTION_TAG, "Assert that the Course Browser Page will be displayed and loaded correctly because that was the 'target' page of the last action which before dropping the user out.")
            courseBrowserPage.assertCourseTitle(course.name)
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

        Log.d(STEP_TAG, "Enter domain: '$DOMAIN.instructure.com'.")
        loginFindSchoolPage.enterDomain(DOMAIN)

        Log.d(STEP_TAG, "Click on 'Next' button on the Toolbar.")
        loginFindSchoolPage.clickToolbarNextMenuItem()

        Log.d(STEP_TAG, "Try to login with invalid, non-existing credentials: '$INVALID_USERNAME', '$INVALID_PASSWORD'.")
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

    private fun loginWithUser(user: CanvasUserApiModel, lastSchoolSaved: Boolean = false) {

        if(lastSchoolSaved) {
            Log.d(STEP_TAG, "Click 'Find another school' button.")
            loginLandingPage.clickFindAnotherSchoolButton()
        }
        else {
            Log.d(STEP_TAG, "Click 'Find My School' button.")
            loginLandingPage.clickFindMySchoolButton()
        }

        Log.d(STEP_TAG, "Enter domain: '${user.domain}'.")
        loginFindSchoolPage.enterDomain(user.domain)

        Log.d(STEP_TAG, "Click on 'Next' button on the Toolbar.")
        loginFindSchoolPage.clickToolbarNextMenuItem()

        Log.d(STEP_TAG, "Login with user: '${user.name}', login id: '${user.loginId}'.")
        loginSignInPage.loginAs(user)
    }

    private fun loginWithLastSavedSchool(user: CanvasUserApiModel) {

        Log.d(STEP_TAG, "Click on last saved school's button.")
        loginLandingPage.clickOnLastSavedSchoolButton()

        Log.d(STEP_TAG, "Login with '${user.name}' user.")
        loginSignInPage.loginAs(user)
    }

    private fun validateUserRole(user: CanvasUserApiModel, course: CourseApiModel, role: String) {

        Log.d(STEP_TAG, "Navigate to 'People' Page of '${course.name}' course.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.openPeopleTab()

        Log.d(ASSERTION_TAG, "Assert that '${user.name}' user's role is '$role'.")
        peopleListPage.assertPersonListed(user, role)

        Log.d(STEP_TAG, "Navigate back to Dashboard Page.")
        pressBackButton(2)
    }

    private fun assertSuccessfulLogin(user: CanvasUserApiModel)
    {
        dashboardPage.waitForRender()
        leftSideNavigationDrawerPage.assertUserLoggedIn(user)
        dashboardPage.assertDisplaysCourses()
        dashboardPage.assertPageObjects()
    }
}
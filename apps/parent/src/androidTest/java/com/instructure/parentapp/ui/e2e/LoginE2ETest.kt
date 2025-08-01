/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.ui.e2e

import android.util.Log
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.dataseeding.api.UserApi
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.withIdlingResourceDisabled
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.seedData
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class LoginE2ETest : ParentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.LOGIN, TestCategory.E2E)
    fun testLoginE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, teachers = 1, courses = 1, parents = 2)
        val parent = data.parentsList[0]
        val parent2 = data.parentsList[1]
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        loginWithUser(parent)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        dashboardPage.waitForRender()
        dashboardPage.assertPageObjects()

        Log.d(ASSERTION_TAG, "Assert that the '${parent.name}' parent user has logged in.")
        leftSideNavigationDrawerPage.assertUserLoggedIn(parent)

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu (to be able to log out).")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Log out with '${parent.name}' student.")
        leftSideNavigationDrawerPage.logout()

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        loginWithUser(student, true)

        Log.d(ASSERTION_TAG, "Assert that the 'Not a Parent' page has been displayed with all the corresponding information on it.")
        notAParentPage.assertNotAParentPageDetails()

        Log.d(STEP_TAG, "Click on 'Return to login' button to navigate back to the login page.")
        notAParentPage.clickReturnToLogin()

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        loginWithUser(teacher, true)

        Log.d(ASSERTION_TAG, "Assert that the 'Not a Parent' page has been displayed with all the corresponding information on it.")
        notAParentPage.assertNotAParentPageDetails()

        Log.d(STEP_TAG, "Expand the 'Are you a student or teacher?' option to see the Canvas Student and Canvas Teacher app icons as links.")
        notAParentPage.expandAppOptions()

        Log.d(ASSERTION_TAG, "Assert that the subtitle message is displayed.")
        notAParentPage.assertOtherAppSubtitleDisplayed()
        notAParentPage.assertStudentAppDisplayed()
        notAParentPage.assertTeacherAppDisplayed()

        Log.d(STEP_TAG, "Click on 'Return to login' button to navigate back to the login page.")
        notAParentPage.clickReturnToLogin()

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        loginWithUser(parent, true)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        dashboardPage.waitForRender()
        dashboardPage.assertPageObjects()

        Log.d(ASSERTION_TAG, "Assert that the '${parent.name}' parent user has logged in.")
        leftSideNavigationDrawerPage.assertUserLoggedIn(parent)

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu (to be able to log out).")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Click on 'Switch Users' button on the left-side menu.")
        leftSideNavigationDrawerPage.clickChangeUser()

        Log.d(ASSERTION_TAG, "Assert that the previously logins has been displayed.")
        loginLandingPage.assertDisplaysPreviousLogins()

        Log.d(STEP_TAG, "Login with user: '${parent2.name}', login id: '${parent2.loginId}'.")
        loginWithUser(parent2, true)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        dashboardPage.waitForRender()
        dashboardPage.assertPageObjects()

        Log.d(ASSERTION_TAG, "Assert that the '${parent2.name}' parent user has logged in.")
        leftSideNavigationDrawerPage.assertUserLoggedIn(parent2)

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu (to be able to log out).")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Click on 'Switch Users' button on the left-side menu.")
        leftSideNavigationDrawerPage.clickChangeUser()

        Log.d(ASSERTION_TAG, "Assert that the 'Previous Logins' section is displayed and both the '${parent.name}' and '${parent2.name}' parents are displayed on the previous login list.")
        loginLandingPage.assertDisplaysPreviousLogins()
        loginLandingPage.assertPreviousLoginUserDisplayed(parent.name)
        loginLandingPage.assertPreviousLoginUserDisplayed(parent2.name)

        Log.d(STEP_TAG, "Remove '${parent.name}' parent from the previous login section.")
        loginLandingPage.removeUserFromPreviousLogins(parent.name)

        Log.d(STEP_TAG, "Login with the '${parent2.name}' user, with one click, by clicking on the user's name on the bottom.")
        loginLandingPage.loginWithPreviousUser(parent2)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        dashboardPage.waitForRender()
        dashboardPage.assertPageObjects()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu (to be able to log out).")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Click on 'Switch Users' button on the left-side menu.")
        leftSideNavigationDrawerPage.clickChangeUser()

        Log.d(ASSERTION_TAG, "Assert that the previously logins has been displayed. Assert that '${parent2.name}' parent is displayed but '${parent.name}' parent is not displayed within the previous logins list (as it was removed before).")
        loginLandingPage.assertDisplaysPreviousLogins()
        loginLandingPage.assertPreviousLoginUserDisplayed(parent2.name)
        loginLandingPage.assertPreviousLoginUserNotExist(parent.name)

        Log.d(STEP_TAG, "Remove '${parent2.name}' parent from the previous login section as well.")
        loginLandingPage.removeUserFromPreviousLogins(parent2.name)

        Log.d(ASSERTION_TAG, "Assert that none of the parents, '${parent.name}' and '${parent2.name}' are displayed and not even the 'Previous Logins' label is displayed.")
        loginLandingPage.assertPreviousLoginUserNotExist(parent.name)
        loginLandingPage.assertPreviousLoginUserNotExist(parent2.name)
        loginLandingPage.assertNotDisplaysPreviousLogins()
      }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.LOGIN, TestCategory.E2E)
    fun testLoginE2EWithLastSavedSchool() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, courses = 1, parents = 1)
        val parent = data.parentsList[0]

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        loginWithUser(parent)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        dashboardPage.waitForRender()
        dashboardPage.assertPageObjects()

        Log.d(ASSERTION_TAG, "Assert that the '${parent.name}' parent user has logged in.")
        leftSideNavigationDrawerPage.assertUserLoggedIn(parent)

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu (to be able to log out).")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Log out with '${parent.name}' student.")
        leftSideNavigationDrawerPage.logout()

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        loginWithLastSavedSchool(parent)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        dashboardPage.waitForRender()
        dashboardPage.assertPageObjects()

        Log.d(ASSERTION_TAG, "Assert that the '${parent.name}' parent user has logged in.")
        leftSideNavigationDrawerPage.assertUserLoggedIn(parent)
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.LOGIN, TestCategory.E2E, SecondaryFeatureCategory.ACCESS_TOKEN_EXPIRATION)
    fun testTokenExpirationForcedLogoutThenLogBackE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, courses = 1, parents = 1)
        val parent = data.parentsList[0]

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        loginWithUser(parent)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        dashboardPage.waitForRender()

        Log.d(PREPARATION_TAG, "Delete the '${parent.name}' parent's (valid) access token which has been created by the login mechanism.")
        UserApi.deleteToken(ApiPrefs.accessToken)

        withIdlingResourceDisabled {

            Log.d(STEP_TAG, "Click on the 'Calendar' bottom menu to navigate to the Calendar page.")
            dashboardPage.clickCalendarBottomMenu()

            Log.d(ASSERTION_TAG, "Assert that the 'Login Required' dialog is displayed because the session (aka. access token) is expired/deleted, so we drop out the user when any action has been made.")
            loginSignInPage.assertLoginRequiredDialog()

            Log.d(STEP_TAG, "Click on the 'LOG IN' button on the 'Login Required' dialog.")
            loginSignInPage.clickLogInOnLoginRequiredDialog()

            Log.d(STEP_TAG, "Log back with the SAME user: '${parent.name}', login id: '${parent.loginId}'.")
            loginSignInPage.loginAs(parent)

            Log.d(ASSERTION_TAG, "Assert that the Calendar Screen page (with empty view) has been loaded.")
            calendarScreenPage.assertEmptyView()
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

        Log.d(STEP_TAG, "Enter domain: '$DOMAIN.instructure.com.'")
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

    private fun loginWithUser(user: CanvasUserApiModel, lastSchoolSaved: Boolean = false) {

        Thread.sleep(5100) //Need to wait > 5 seconds before each login attempt because of new 'too many attempts' login policy on web.

        if (lastSchoolSaved) {
            Log.d(STEP_TAG, "Click 'Find Another School' button.")
            loginLandingPage.clickFindAnotherSchoolButton()
        } else {
            Log.d(STEP_TAG, "Click 'Find My School' button.")
            loginLandingPage.clickFindMySchoolButton()
        }

        Log.d(STEP_TAG, "Enter domain: '${user.domain}'.")
        loginFindSchoolPage.enterDomain(user.domain)

        Log.d(STEP_TAG, "Click on 'Next' button on the Toolbar.")
        loginFindSchoolPage.clickToolbarNextMenuItem()
        loginSignInPage.loginAs(user)
    }

    private fun loginWithLastSavedSchool(user: CanvasUserApiModel) {

        Log.d(STEP_TAG, "Click on last saved school's button.")
        loginLandingPage.clickOnLastSavedSchoolButton()

        Log.d(STEP_TAG, "Login with '${user.name}' user.")
        loginSignInPage.loginAs(user)
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.LOGIN, TestCategory.E2E)
    fun testLoginWithParentWhoHasNoStudentE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, courses = 1, parents = 1)
        val parent = data.parentsList[0]
        val parentWithoutStudent = UserApi.createCanvasUser()
        val student = data.studentsList[0]

        Log.d(STEP_TAG, "Login with user: '${parentWithoutStudent.name}', login id: '${parentWithoutStudent.loginId}'.")
        loginWithUser(parentWithoutStudent)

        Log.d(ASSERTION_TAG, "Assert that the 'Not a Parent' page has been displayed with all the corresponding information on it.")
        notAParentPage.assertNotAParentPageDetails()

        Log.d(STEP_TAG, "Click on 'Return to login' button to navigate back to the login page.")
        notAParentPage.clickReturnToLogin()

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        loginWithUser(parent, true)

        Log.d(PREPARATION_TAG, "Generate a pairing code for the '${student.name}' student to be able to pair with an observer.")
        val responsePairingCodeObject = UserApi.postGeneratePairingCode(student.id)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        dashboardPage.waitForRender()
        dashboardPage.assertPageObjects()

        Log.d(ASSERTION_TAG, "Assert that the '${parent.name}' parent user has logged in.")
        leftSideNavigationDrawerPage.assertUserLoggedIn(parent)

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Click on the 'Manage Students' button on the left-side menu.")
        leftSideNavigationDrawerPage.clickManageStudents()

        Log.d(ASSERTION_TAG, "Assert that the 'Manage Students' page is displayed with the '${student.name}' student.")
        manageStudentsPage.assertToolbarTitle()
        manageStudentsPage.assertStudentItemDisplayed(student.shortName)

        Log.d(STEP_TAG, "Click on the 'Add Student' FAB (+) button on the bottom-right corner.")
        manageStudentsPage.tapAddStudent()

        Log.d(ASSERTION_TAG, "Assert that the 'Add student with...' label and both the 'Pairing Code' and the 'QR Code' options are displayed on the Add Student (bottom) Page.")
        addStudentBottomPage.assertAddStudentWithLabel()
        addStudentBottomPage.assertPairingCodeOptionDisplayed()
        addStudentBottomPage.assertQRCodeOptionDisplayed()

        Log.d(STEP_TAG, "Click on the 'Pairing Code' to add a student via pairing code.")
        addStudentBottomPage.clickOnPairingCode()

        Log.d(STEP_TAG, "Enter the pairing code of the student and click on the 'OK' button to apply.")
        pairingCodePage.enterPairingCode(responsePairingCodeObject.pairingCode.toString())
        pairingCodePage.clickOkButton()
        composeTestRule.waitForIdle()

        Log.d(ASSERTION_TAG, "Assert that the '${student.shortName}' student is displayed.")
        manageStudentsPage.assertStudentItemDisplayed(student.shortName)

        Log.d(STEP_TAG, "Click on the '${student.name}' student on the 'Manage Students' page.")
        manageStudentsPage.clickStudent(student.shortName)

        Log.d(ASSERTION_TAG, "Assert that the 'Alert Settings' page is displayed.")
        studentAlertSettingsPage.assertToolbarTitle()

        Log.d(STEP_TAG, "Click on the 'overflow menu' button on the 'Alert Settings' page.")
        studentAlertSettingsPage.clickOverflowMenu()

        Log.d(STEP_TAG, "Click on the 'Delete Student' button on the overflow menu.")
        studentAlertSettingsPage.clickDeleteStudent()

        Log.d(ASSERTION_TAG, "Assert that the 'Delete Student' dialog is displayed with the correct details.")
        studentAlertSettingsPage.assertDeleteStudentDialogDetails()

        Log.d(STEP_TAG, "Click on the 'Delete' button on the 'Delete Student' dialog.")
        studentAlertSettingsPage.clickDeleteStudentButton()

        Log.d(ASSERTION_TAG, "Assert that the previously deleted student is not displayed on the 'Manage Students' page anymore.")
        manageStudentsPage.assertStudentItemNotDisplayed(student.shortName)
        manageStudentsPage.assertEmptyContent()
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

}
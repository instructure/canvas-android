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
package com.instructure.teacher.ui.e2e

import android.util.Log
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.SeedApi
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.espresso.ViewUtils
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
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

        Log.d(STEP_TAG,"Click 'Find My School' button.")
        loginLandingPage.clickFindMySchoolButton()

        Log.d(STEP_TAG,"Enter domain: ${teacher1.domain}.")
        loginFindSchoolPage.enterDomain(teacher1.domain)

        Log.d(STEP_TAG,"Click on 'Next' button on the Toolbar.")
        loginFindSchoolPage.clickToolbarNextMenuItem()

        Log.d(STEP_TAG,"Login with user: ${teacher1.name}, login id: ${teacher1.loginId} , password: ${teacher1.password}")
        loginSignInPage.loginAs(teacher1)

        Log.d(STEP_TAG,"Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        verifyDashboardPage(teacher1)

        Log.d(STEP_TAG,"Validate ${teacher1.name} user's role as a Teacher.")
        validateUserRole(teacher1, course, "Teacher")

        Log.d(STEP_TAG,"Log out with ${teacher1.name} student.")
        dashboardPage.logOut()

        Log.d(STEP_TAG,"Click 'Find My School' button.")
        loginLandingPage.clickFindMySchoolButton()

        Log.d(STEP_TAG,"Enter domain: ${teacher2.domain}.")
        loginFindSchoolPage.enterDomain(teacher2.domain)

        Log.d(STEP_TAG,"Click on 'Next' button on the Toolbar.")
        loginFindSchoolPage.clickToolbarNextMenuItem()

        Log.d(STEP_TAG,"Login with user: ${teacher2.name}, login id: ${teacher2.loginId} , password: ${teacher2.password}")
        loginSignInPage.loginAs(teacher2)

        Log.d(STEP_TAG,"Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        verifyDashboardPage(teacher2)

        Log.d(STEP_TAG,"Click on 'Change User' button on the left-side menu.")
        dashboardPage.pressChangeUser()

        Log.d(STEP_TAG,"Assert that the previously logins has been displayed.")
        loginLandingPage.assertDisplaysPreviousLogins()

        Log.d(STEP_TAG,"Login MANUALLY. Click 'Find My School' button.")
        loginLandingPage.clickFindMySchoolButton()

        Log.d(STEP_TAG,"Enter domain: ${teacher1.domain}.")
        loginFindSchoolPage.enterDomain(teacher1.domain)

        Log.d(STEP_TAG,"Click on 'Next' button on the Toolbar.")
        loginFindSchoolPage.clickToolbarNextMenuItem()

        Log.d(STEP_TAG,"Login with user: ${teacher1.name}, login id: ${teacher1.loginId} , password: ${teacher1.password}")
        loginSignInPage.loginAs(teacher1)

        Log.d(STEP_TAG,"Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        verifyDashboardPage(teacher1)

        Log.d(STEP_TAG,"Click on 'Change User' button on the left-side menu.")
        dashboardPage.pressChangeUser()

        Log.d(STEP_TAG,"Assert that the previously logins has been displayed.")
        loginLandingPage.assertDisplaysPreviousLogins()

        Log.d(STEP_TAG,"Login with the previous user, ${teacher2.name}, with one click, by clicking on the user's name on the bottom.")
        loginLandingPage.loginWithPreviousUser(teacher2)

        Log.d(STEP_TAG,"Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        verifyDashboardPage(teacher2)
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

        Log.d(STEP_TAG,"Click 'Find My School' button.")
        loginLandingPage.clickFindMySchoolButton()

        Log.d(STEP_TAG,"Enter domain: ${parent.domain}.")
        loginFindSchoolPage.enterDomain(parent.domain)

        Log.d(STEP_TAG,"Enter domain: ${parent.domain}.")
        loginFindSchoolPage.clickToolbarNextMenuItem()

        Log.d(STEP_TAG,"Login with user: ${student.name}, login id: ${student.loginId} , password: ${student.password}")
        loginSignInPage.loginAs(student)

        Log.d(STEP_TAG,"Assert that the user has been landed on 'Not a teacher?' Page.")
        notATeacherPage.assertPageObjects()

        Log.d(STEP_TAG,"Navigate back to the Teacher app's Login Landing Page's screen.")
        notATeacherPage.clickOnLoginButton()

        Log.d(STEP_TAG,"Assert the Teacher app's Login Landing Page's screen is displayed.")
        loginLandingPage.assertPageObjects()

        Log.d(STEP_TAG,"Click 'Find My School' button.")
        loginLandingPage.clickFindMySchoolButton()

        Log.d(STEP_TAG,"Enter domain: ${parent.domain}.")
        loginFindSchoolPage.enterDomain(parent.domain)

        Log.d(STEP_TAG,"Enter domain: ${parent.domain}.")
        loginFindSchoolPage.clickToolbarNextMenuItem()

        Log.d(STEP_TAG,"Assert that the Login page has been displayed.")
        loginSignInPage.assertPageObjects()

        Log.d(STEP_TAG,"Login with user: ${parent.name}, login id: ${parent.loginId} , password: ${parent.password}")
        loginSignInPage.loginAs(parent)

        Log.d(STEP_TAG,"Assert that the user has been landed on 'Not a teacher?' Page.")
        notATeacherPage.assertPageObjects()

        Log.d(STEP_TAG,"Navigate back to the Teacher app's login screen.")
        notATeacherPage.clickOnLoginButton()

        Log.d(STEP_TAG,"Assert that the user has landed on Teacher app's Login Landing Page's screen.")
        loginLandingPage.assertPageObjects()
    }

    private fun validateUserRole(user: CanvasUserApiModel, course: CourseApiModel, role: String) {

        Log.d(STEP_TAG,"Navigate to 'People' Page of ${course.name} course.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.openPeopleTab()

        Log.d(STEP_TAG,"Assert that ${user.name} user's role is $role.")
        peopleListPage.assertPersonListed(user, role)

        Log.d(STEP_TAG,"Navigate back to Dashboard Page.")
        ViewUtils.pressBackButton(2)
    }

    private fun verifyDashboardPage(user: CanvasUserApiModel)
    {
        dashboardPage.waitForRender()
        dashboardPage.assertUserLoggedIn(user)
        dashboardPage.assertDisplaysCourses()
        dashboardPage.assertPageObjects()
    }
}
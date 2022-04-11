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

import androidx.test.espresso.Espresso
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
import com.instructure.panda_annotations.*
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class LoginE2ETest : StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.LOGIN, TestCategory.E2E)
    fun testLoginE2E() {

        // Seed data
        val data = seedData(students = 2, teachers = 1, courses = 1)
        val student1 = data.studentsList[0]
        val student2 = data.studentsList[1]

        // Sign in with student 1
        loginLandingPage.clickFindMySchoolButton()
        loginFindSchoolPage.enterDomain(student1.domain)
        loginFindSchoolPage.clickToolbarNextMenuItem()
        loginSignInPage.loginAs(student1)

        // Verify that the dashboard page looks good
        verifyDashboardPage(student1)

        // Sign out
        dashboardPage.signOut()

        // Sign in with student 2
        loginLandingPage.clickFindMySchoolButton()
        loginFindSchoolPage.enterDomain(student2.domain)
        loginFindSchoolPage.clickToolbarNextMenuItem()
        loginSignInPage.loginAs(student2)

        // Verify that the dashboard page looks good
        verifyDashboardPage(student2)

        // Change user back to student 1 with "change user" + manual login
        dashboardPage.pressChangeUser()
        loginLandingPage.assertDisplaysPreviousLogins()
        loginLandingPage.clickFindMySchoolButton()
        loginFindSchoolPage.enterDomain(student1.domain)
        loginFindSchoolPage.clickToolbarNextMenuItem()
        loginSignInPage.loginAs(student1)

        // Verify that the dashboard page looks good
        verifyDashboardPage(student1)

        // Now change back to student 2 with "change user" + select previous user
        dashboardPage.pressChangeUser()
        loginLandingPage.assertDisplaysPreviousLogins()
        loginLandingPage.loginWithPreviousUser(student2)

        // Verify that the dashboard page looks good
        verifyDashboardPage(student2)
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.LOGIN, TestCategory.E2E)
    fun testUserRolesLoginE2E() {

        // Seed student, teacher, TA and parent data
        val data = seedData(students = 1, teachers = 1, tas = 1, courses = 1)
        val parentData = SeedApi.seedParentData(
                SeedApi.SeedParentDataRequest(
                        courses=1, students=1, parents=1
                )
        )

        // Test for student
        validateUserAndRole(data.studentsList[0], data.coursesList[0], "Student")

        // Test for teacher
        validateUserAndRole(data.teachersList[0], data.coursesList[0], "Teacher")

        // Test for TA
        validateUserAndRole(data.taList[0], data.coursesList[0], "TA")

        // Test for parent, which is different/abbreviated because parents don't show
        // up in the "People" page so we can't verify their role.

        // Sign in as a parent
        val parent = parentData.parentsList[0]
        loginLandingPage.clickFindMySchoolButton()
        loginFindSchoolPage.enterDomain(parent.domain)
        loginFindSchoolPage.clickToolbarNextMenuItem()
        loginSignInPage.loginAs(parent)

        // Verify that we are signed in as the parent
        verifyDashboardPage(parent)

        // Sign the parent out
        dashboardPage.signOut()
    }

    // Verify that students can sign into vanity domain
    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.LOGIN, TestCategory.E2E)
    fun testVanityDomainLoginE2E() {
        // Create a Retrofit client for our vanity domain
        val domain = "canvas.beta.jitops.computer" // Our test vanity domain
        val retrofitClient = CanvasNetworkAdapter.createAdminRetrofitClient(domain)

        // Create services off of that Retrofit client
        val userService = retrofitClient.create(UserApi.UserService::class.java)
        val coursesService = retrofitClient.create(CoursesApi.CoursesService::class.java)
        val enrollmentsService = retrofitClient.create(EnrollmentsApi.EnrollmentsService::class.java)

        // Create student, teacher, course and enrollments in our vanity domain
        val student = UserApi.createCanvasUser(userService = userService, userDomain = domain)
        val teacher = UserApi.createCanvasUser(userService = userService, userDomain = domain)
        val course = CoursesApi.createCourse(coursesService = coursesService)
        EnrollmentsApi.enrollUser(
                courseId = course.id,
                userId = student.id,
                enrollmentType = STUDENT_ENROLLMENT,
                enrollmentService = enrollmentsService
        )
        EnrollmentsApi.enrollUser(
                courseId = course.id,
                userId = teacher.id,
                enrollmentType = TEACHER_ENROLLMENT,
                enrollmentService = enrollmentsService
        )

        // Attempt to sign into our vanity domain, and validate ourself as a student
        validateUserAndRole(user = student, course = course, role = "Student" )
    }

    // Repeated logic from the testUserRolesLoginE2E test.
    // Assumes that you start at the login landing page, and logs you out before completing.
    private fun validateUserAndRole(user: CanvasUserApiModel, course: CourseApiModel, role: String) {
        loginLandingPage.clickFindMySchoolButton()
        loginFindSchoolPage.enterDomain(user.domain)
        loginFindSchoolPage.clickToolbarNextMenuItem()
        loginSignInPage.loginAs(user)

        // Verify that we are signed in as the user
        verifyDashboardPage(user)

        // Verify that our role is correct
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectPeople()
        peopleListPage.assertPersonListed(user, role)
        Espresso.pressBack() // to course browser page
        Espresso.pressBack() // to dashboard page

        // Sign the user out
        dashboardPage.signOut()

    }

    private fun verifyDashboardPage(user: CanvasUserApiModel)
    {
        dashboardPage.waitForRender()
        dashboardPage.assertUserLoggedIn(user)
        dashboardPage.assertDisplaysCourses()
        dashboardPage.assertPageObjects()
    }
}
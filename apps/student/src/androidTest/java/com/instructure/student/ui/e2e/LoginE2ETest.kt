package com.instructure.student.ui.e2e

import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.panda_annotations.*
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import org.junit.Test

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

    private fun verifyDashboardPage(user: CanvasUserApiModel)
    {
        dashboardPage.waitForRender()
        dashboardPage.assertUserLoggedIn(user)
        dashboardPage.assertDisplaysCourses()
        dashboardPage.assertPageObjects()
    }
}
package com.instructure.teacher.ui

import com.instructure.espresso.TestRail
import com.instructure.espresso.filters.P1
import com.instructure.teacher.ui.utils.TeacherTest
import org.junit.Test

class LoginLandingPageTest: TeacherTest() {

    // Ditto doesn't support WebViews
    @Test
    @TestRail(ID = "C3108891")
    @P1
    override fun displaysPageObjects() {
        loginLandingPage.assertPageObjects()
    }

    // Ditto doesn't support WebViews
    @Test
    @TestRail(ID = "C3108893")
    fun opensCanvasNetworksSignInPage() {
        loginLandingPage.clickCanvasNetworkButton()
        loginSignInPage.assertPageObjects()
    }
}

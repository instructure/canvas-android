package com.instructure.teacher.ui

import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.espresso.TestRail
import com.instructure.espresso.ditto.Ditto
import org.junit.Test

class LoginFindSchoolPageTest: TeacherTest() {

    @Test
    @Ditto
    @TestRail(ID = "C3108892")
    override fun displaysPageObjects() {
        loginLandingPage.clickFindMySchoolButton()
        loginFindSchoolPage.assertPageObjects()
    }
}

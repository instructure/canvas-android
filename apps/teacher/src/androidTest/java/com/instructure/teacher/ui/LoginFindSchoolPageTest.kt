package com.instructure.teacher.ui

import com.instructure.espresso.TestRail
import com.instructure.teacher.ui.utils.TeacherTest
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class LoginFindSchoolPageTest: TeacherTest() {

    @Test
    @TestRail(ID = "C3108892")
    override fun displaysPageObjects() {
        loginLandingPage.clickFindMySchoolButton()
        loginFindSchoolPage.assertPageObjects()
    }
}

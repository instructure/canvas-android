package com.instructure.teacher.ui

import com.instructure.espresso.filters.P1
import com.instructure.teacher.ui.utils.TeacherTest
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class LoginLandingPageTest: TeacherTest() {

    // Runs live; no MockCanvas
    @Test
    @P1
    override fun displaysPageObjects() {
        loginLandingPage.assertPageObjects()
    }
}

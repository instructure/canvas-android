package com.instructure.teacher.ui.e2e

import com.instructure.teacher.ui.utils.TeacherTest
import dagger.hilt.android.testing.HiltAndroidTest
import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(Cucumber::class)
@CucumberOptions(plugin = ["pretty"])
class CucumberPOC : TeacherTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

}
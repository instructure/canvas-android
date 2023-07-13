package com.instructure.student.ui.renderTests

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.student.activity.NavigationActivity
import com.instructure.student.espresso.StudentRenderTest
import com.instructure.student.ui.pages.LeftSideNavigationDrawerPage
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LeftSideNavigationDrawerRenderTest: StudentRenderTest() {
    @get:Rule(order = 0)
    var mActivityTestRule: ActivityTestRule<NavigationActivity> = ActivityTestRule(NavigationActivity::class.java)

    @get:Rule(order = 1)
    var hiltAndroidRule = HiltAndroidRule(this)

    private val page = LeftSideNavigationDrawerPage()
    private var injected = false


    @Before
    fun setup() {
        if (!injected) {
            injected = true
            hiltAndroidRule.inject()
        }
    }

    @Test
    fun displaysUserData() {
        ApiPrefs.user = User()
        ApiPrefs.canvasForElementary = true
        mActivityTestRule.launchActivity(null)
        page.openMenu()
        page.assertDisplaysUserData()
    }

}
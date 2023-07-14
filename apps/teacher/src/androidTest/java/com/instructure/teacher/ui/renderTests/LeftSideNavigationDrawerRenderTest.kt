package com.instructure.teacher.ui.renderTests

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.instructure.teacher.activities.InitActivity
import com.instructure.teacher.ui.pages.LeftSideNavigationDrawerPage
import com.instructure.teacher.ui.utils.TeacherRenderTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LeftSideNavigationDrawerRenderTest: TeacherRenderTest() {
    // We have to use the real NavigationActivity because we want to test the visible menu items regarding to the user's role
    @get:Rule(order = 0)
    var mActivityTestRule: ActivityTestRule<InitActivity> = ActivityTestRule(InitActivity::class.java)

    @get:Rule(order = 1)
    var hiltAndroidRule = HiltAndroidRule(this)

    private val page = LeftSideNavigationDrawerPage()

    // Espresso will retry the test if it fails, so we need to make sure we only inject once
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
        page.openMenu()
        page.assertUserDataDisplayed()
    }

    @Test
    fun displaysNavigationMenuItems() {
        page.openMenu()
        page.assertNavigationItemsDisplayed()
    }

    @Test
    fun displaysOptionsMenuItem() {
        page.openMenu()
        page.assertOptionItemsDisplayed()
    }

    @Test
    fun displaysAccountMenuItem() {
        page.openMenu()
        page.assertAccountItemsDisplayed()
    }

    @Test
    fun displaysVersionNumber() {
        page.openMenu()
        page.assertVersionNumberDisplayed()
    }
}

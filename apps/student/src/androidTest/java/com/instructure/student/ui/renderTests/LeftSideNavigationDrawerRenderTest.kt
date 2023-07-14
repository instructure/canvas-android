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

// The test should be executed on a device with API level 29 or under
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LeftSideNavigationDrawerRenderTest: StudentRenderTest() {
    // We have to use the real NavigationActivity because we want to test the visible menu items regarding to the user's role
    @get:Rule(order = 0)
    var mActivityTestRule: ActivityTestRule<NavigationActivity> = ActivityTestRule(NavigationActivity::class.java)

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
        ApiPrefs.user = User()
        ApiPrefs.canvasForElementary = false
        mActivityTestRule.launchActivity(null)

        page.openMenu()
        page.assertUserDataDisplayed()
    }

    @Test
    fun displaysElementaryNavigationMenuItems() {
        ApiPrefs.user = User()
        ApiPrefs.canvasForElementary = true
        mActivityTestRule.launchActivity(null)

        page.openMenu()
        page.assertElementaryNavigationMenuItemsDisplayed()
    }

    @Test
    fun displaysDefaultNavigationMenuItems() {
        ApiPrefs.user = User()
        ApiPrefs.canvasForElementary = false
        mActivityTestRule.launchActivity(null)

        page.openMenu()
        page.assertDefaultNavigationMenuItemsDisplayed()
    }

    @Test
    fun displaysElementaryOptionsMenuItems() {
        ApiPrefs.user = User()
        ApiPrefs.canvasForElementary = true
        mActivityTestRule.launchActivity(null)

        page.openMenu()
        page.assertElementaryOptionsMenuItemsDisplayed()
    }

    @Test
    fun displaysDefaultOptionsMenuItems() {
        ApiPrefs.user = User()
        ApiPrefs.canvasForElementary = false
        mActivityTestRule.launchActivity(null)

        page.openMenu()
        page.assertDefaultOptionsMenuItemsDisplayed()
    }

    @Test
    fun displaysElementaryAccountMenuItems() {
        ApiPrefs.user = User()
        ApiPrefs.canvasForElementary = true
        mActivityTestRule.launchActivity(null)

        page.openMenu()
        page.assertElementaryAccountMenuItemsDisplayed()
    }

    @Test
    fun displaysDefaultAccountMenuItems() {
        ApiPrefs.user = User()
        ApiPrefs.canvasForElementary = false
        mActivityTestRule.launchActivity(null)

        page.openMenu()
        page.assertDefaultAccountMenuItemsDisplayed()
    }

    @Test
    fun displaysVersionNumber() {
        ApiPrefs.user = User()
        mActivityTestRule.launchActivity(null)

        page.openMenu()
        page.assertVersionNumberDisplayed()
    }

}
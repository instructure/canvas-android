package com.instructure.teacher.ui.pages.classic

import android.view.View
import androidx.appcompat.widget.SwitchCompat
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.canvas.espresso.waitForMatcherWithSleeps
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.OnViewWithContentDescription
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertContainsText
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.onViewWithId
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.scrollTo
import com.instructure.teacher.R
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher

/**
 * Represents the Left Side Navigation Drawer Page.
 *
 * This page extends the BasePage class and provides functionality for interacting with the left side
 * navigation drawer. It contains various view elements such as user name, user email, logout button,
 * version, hamburger button, and hamburger button matcher.
 */
class LeftSideNavigationDrawerPage: BasePage() {

    private val userName by OnViewWithId(R.id.navigationDrawerUserName)
    private val userEmail by OnViewWithId(R.id.navigationDrawerUserEmail)
    private val logoutButton by OnViewWithId(R.id.navigationDrawerItem_logout)
    private val version by OnViewWithId(R.id.navigationDrawerVersion)
    private val hamburgerButton by OnViewWithContentDescription(R.string.navigation_drawer_open)

    // Sometimes when we navigate back to the dashboard page, there can be several hamburger buttons
    // in the UI stack.  We want to choose the one that is displayed.
    private val hamburgerButtonMatcher = CoreMatchers.allOf(
        ViewMatchers.withContentDescription(R.string.navigation_drawer_open),
        ViewMatchers.isDisplayed()
    )

    /**
     * Assert that the proper user info is displayed on the Left Side Menu, so does the user's short name and below that the user email as well.
     * @param shortName: The shortname of the user to assert.
     * @param email: The email of the user to assert.
     */
    fun assertUserInfo(shortName: String, email: String) {
        userName.assertContainsText(shortName)
        userEmail.assertContainsText(email)
    }

    /**
     * Clicks the menu item with the specified ID in the navigation drawer.
     *
     * @param menuId The ID of the menu item to click.
     */
    private fun clickMenu(menuId: Int) {
        onView(hamburgerButtonMatcher).click()
        onViewWithId(menuId).scrollTo().click()
    }

    /**
     * Performs the logout action from the navigation drawer.
     * It waits for the sign-out to take effect before returning.
     */
    fun logout() {
        onView(hamburgerButtonMatcher).click()
        logoutButton.scrollTo().click()
        onViewWithText(android.R.string.ok).click()
        // It can potentially take a long time for the sign-out to take effect, especially on
        // slow FTL devices.  So let's pause for a bit until we see the canvas logo.
        waitForMatcherWithSleeps(ViewMatchers.withId(R.id.canvasLogo), 20000).check(matches(
            ViewMatchers.isDisplayed()
        ))
    }

    /**
     * Clicks the "Change User" menu item in the navigation drawer.
     */
    fun clickChangeUserMenu() {
        clickMenu(R.id.navigationDrawerItem_changeUser)
    }

    /**
     * Clicks the "Help" menu item in the navigation drawer.
     */
    fun clickHelpMenu() {
        clickMenu(R.id.navigationDrawerItem_help)
    }

    /**
     * Clicks the "Files" menu item in the navigation drawer.
     */
    fun clickFilesMenu() {
        clickMenu(R.id.navigationDrawerItem_files)
    }

    /**
     * Clicks the "Settings" menu item in the navigation drawer.
     */
    fun clickSettingsMenu() {
        clickMenu(R.id.navigationDrawerSettings)
    }

    /**
     * Sets the color overlay in the navigation drawer.
     *
     * @param colorOverlay Flag indicating whether to enable color overlay.
     */
    fun setColorOverlay(colorOverlay: Boolean) {
        hamburgerButton.click()
        onViewWithId(R.id.navigationDrawerColorOverlaySwitch).perform(SetSwitchCompat(colorOverlay))
        Espresso.pressBack()
    }

    /**
     * Asserts that the specified user is logged in.
     *
     * @param user The user to assert being logged in.
     */
    fun assertUserLoggedIn(user: CanvasUserApiModel) {
        onView(hamburgerButtonMatcher).click()
        onViewWithText(user.shortName).assertDisplayed()
        Espresso.pressBack()
    }

    /**
     * Custom ViewAction to set a SwitchCompat to the desired on/off position
     * [position]: true -> "on", false -> "off"
     */
    private class SetSwitchCompat(val position: Boolean) : ViewAction {
        override fun getDescription(): String {
            val desiredPosition =  if(position) "On" else "Off"
            return "Set SwitchCompat to $desiredPosition"
        }

        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isAssignableFrom(SwitchCompat::class.java)
        }

        override fun perform(uiController: UiController?, view: View?) {
            val switch = view as SwitchCompat
            if(switch != null) {
                switch.isChecked = position
            }
        }
    }
}

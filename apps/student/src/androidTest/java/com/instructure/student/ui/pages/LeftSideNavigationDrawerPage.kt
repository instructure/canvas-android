package com.instructure.student.ui.pages

import android.view.View
import androidx.appcompat.widget.SwitchCompat
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.waitForMatcherWithSleeps
import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.OnViewWithContentDescription
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.onViewWithId
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.page.waitForViewWithId
import com.instructure.espresso.scrollTo
import com.instructure.student.R
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher

class LeftSideNavigationDrawerPage: BasePage() {

    private val settings by OnViewWithId(R.id.navigationDrawerSettings)
    private val userName by OnViewWithId(R.id.navigationDrawerUserName)
    private val userEmail by OnViewWithId(R.id.navigationDrawerUserEmail)
    private val changeUser by OnViewWithId(R.id.navigationDrawerItem_changeUser)
    private val logoutButton by OnViewWithId(R.id.navigationDrawerItem_logout)
    private val version by OnViewWithId(R.id.navigationDrawerVersion)
    private val hamburgerButton by OnViewWithContentDescription(R.string.navigation_drawer_open)

    // Sometimes when we navigate back to the dashboard page, there can be several hamburger buttons
    // in the UI stack.  We want to choose the one that is displayed.
    private val hamburgerButtonMatcher = CoreMatchers.allOf(
        ViewMatchers.withContentDescription(R.string.navigation_drawer_open),
        ViewMatchers.isDisplayed()
    )

    private fun clickMenu(menuId: Int) {
        onView(hamburgerButtonMatcher).click()
        waitForViewWithId(menuId).scrollTo().click()
    }

    fun logout() {
        onView(hamburgerButtonMatcher).click()
        logoutButton.scrollTo().click()
        onViewWithText(android.R.string.yes).click()
        // It can potentially take a long time for the sign-out to take effect, especially on
        // slow FTL devices.  So let's pause for a bit until we see the canvas logo.
        waitForMatcherWithSleeps(ViewMatchers.withId(R.id.canvasLogo), 20000).check(matches(
            ViewMatchers.isDisplayed()
        ))
    }

    fun clickChangeUserMenu() {
        clickMenu(R.id.navigationDrawerItem_changeUser)
    }

    fun clickHelpMenu() {
        clickMenu(R.id.navigationDrawerItem_help)
    }

    fun clickFilesMenu() {
        clickMenu(R.id.navigationDrawerItem_files)
    }

    fun clickBookmarksMenu() {
        clickMenu(R.id.navigationDrawerItem_bookmarks)
    }

    fun clickSettingsMenu() {
        clickMenu(R.id.navigationDrawerSettings)
    }

    fun setShowGrades(showGrades: Boolean) {
        hamburgerButton.click()
        onViewWithId(R.id.navigationDrawerShowGradesSwitch).perform(SetSwitchCompat(showGrades))
        Espresso.pressBack()
    }

    fun setColorOverlay(colorOverlay: Boolean) {
        hamburgerButton.click()
        onViewWithId(R.id.navigationDrawerColorOverlaySwitch).perform(SetSwitchCompat(colorOverlay))
        Espresso.pressBack()
    }

    fun assertProfileDetails(teacher: User) {
        userName.check(matches(withText(teacher.shortName)))
    }

    fun assertUserLoggedIn(user: CanvasUserApiModel) {
        onView(hamburgerButtonMatcher).click()
        onViewWithText(user.shortName).assertDisplayed()
        Espresso.pressBack()
    }

    fun assertUserLoggedIn(user: User) {
        onView(hamburgerButtonMatcher).click()
        onViewWithText(user.shortName!!).assertDisplayed()
        Espresso.pressBack()
    }

    fun assertUserLoggedIn(userName: String) {
        onView(hamburgerButtonMatcher).click()
        onViewWithText(userName).assertDisplayed()
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

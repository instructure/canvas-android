package com.instructure.student.ui.pages.classic

import android.view.View
import androidx.appcompat.widget.SwitchCompat
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.CanvasTest
import com.instructure.canvas.espresso.waitForMatcherWithSleeps
import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.OnViewWithContentDescription
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.ViewAlphaAssertion
import com.instructure.espresso.assertContainsText
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.onViewWithId
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.waitForViewWithId
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.espresso.page.withId
import com.instructure.espresso.scrollTo
import com.instructure.espresso.swipeDown
import com.instructure.espresso.swipeUp
import com.instructure.student.R
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import java.lang.Thread.sleep

class LeftSideNavigationDrawerPage : BasePage() {

    private val hamburgerButton by OnViewWithContentDescription(R.string.navigation_drawer_open)

    // User data
    private val profileImage by OnViewWithId(R.id.navigationDrawerProfileImage)
    private val userName by OnViewWithId(R.id.navigationDrawerUserName)
    private val userEmail by OnViewWithId(R.id.navigationDrawerUserEmail)

    // Navigation items
    private val files by OnViewWithId(R.id.navigationDrawerItem_files)
    private val bookmarks by OnViewWithId(R.id.navigationDrawerItem_bookmarks)
    private val settings by OnViewWithId(R.id.navigationDrawerSettings)

    //Option items
    private val showGrades by OnViewWithId(R.id.navigationDrawerItem_showGrades)
    private val colorOverlay by OnViewWithId(R.id.navigationDrawerItem_colorOverlay)

    // Account items
    private val help by OnViewWithId(R.id.navigationDrawerItem_help)
    private val changeUser by OnViewWithId(R.id.navigationDrawerItem_changeUser)
    private val logoutButton by OnViewWithId(R.id.navigationDrawerItem_logout)

    private val offlineIndicator by OnViewWithId(R.id.navigationDrawerOfflineIndicator, autoAssert = false)

    // Sometimes when we navigate back to the dashboard page, there can be several hamburger buttons
    // in the UI stack.  We want to choose the one that is displayed.
    private val hamburgerButtonMatcher = CoreMatchers.allOf(
        ViewMatchers.withContentDescription(R.string.navigation_drawer_open),
        ViewMatchers.isDisplayed()
    )

    private fun clickMenu(menuId: Int) {
        sleep(1000) //to avoid listview a11y error (content description is missing)
        waitForView(hamburgerButtonMatcher).click()
        waitForViewWithId(menuId).scrollTo().click()
    }

    fun logout() {
        onView(hamburgerButtonMatcher).click()
        logoutButton.scrollTo().click()
        onViewWithText(android.R.string.ok).click()
        // It can potentially take a long time for the sign-out to take effect, especially on
        // slow FTL devices.  So let's pause for a bit until we see the canvas logo.
        waitForMatcherWithSleeps(ViewMatchers.withId(R.id.canvasLogo), 20000).check(
            matches(
                ViewMatchers.isDisplayed()
            )
        )
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

    fun clickStudioMenu() {
        clickMenu(R.id.navigationDrawerItem_studio)
    }

    fun clickSettingsMenu() {
        clickMenu(R.id.navigationDrawerSettings)
    }

    fun setShowGrades(showGrades: Boolean) {
        hamburgerButton.click()
        onViewWithId(R.id.navigationDrawerShowGradesSwitch).perform(SetSwitchCompat(showGrades))
        Espresso.pressBack()
        sleep(1000) //wait for drawer to close
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
        sleep(2000)
        waitForViewWithText(user.shortName).assertDisplayed()
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

    fun assertUserInfo(shortName: String, email: String) {
        userName.assertContainsText(shortName)
        userEmail.assertContainsText(email)
    }

    fun assertMenuItems(isElementaryStudent: Boolean) {
        hamburgerButton.click()
        userName.assertDisplayed()
        userEmail.assertDisplayed()

        settings.assertDisplayed()

        if(CanvasTest.isLandscapeDevice() || CanvasTest.isLowResDevice()) onView(withId(R.id.navigationDrawer)).swipeUp()
        changeUser.assertDisplayed()
        logoutButton.assertDisplayed()

        if (isElementaryStudent) {
            assertElementaryNavigationBehaviorMenuItems()
        } else {
            assertDefaultNavigationBehaviorMenuItems()
        }
    }

    fun assertOfflineIndicatorDisplayed() {
        sleep(1000) //to avoid listview a11y error (content description is missing)
        hamburgerButton.click()
        offlineIndicator.assertDisplayed()
    }

    fun assertOfflineIndicatorNotDisplayed() {
        sleep(1000) //to avoid listview a11y error (content description is missing)
        hamburgerButton.click()
        offlineIndicator.assertNotDisplayed()
    }

    private fun assertDefaultNavigationBehaviorMenuItems() {
        if(CanvasTest.isLandscapeDevice()) onView(withId(R.id.navigationDrawer)).swipeDown()
        files.assertDisplayed()
        bookmarks.assertDisplayed()
        settings.assertDisplayed()

        if(CanvasTest.isLandscapeDevice()) onView(withId(R.id.navigationDrawer)).swipeUp()
        showGrades.assertDisplayed()
        colorOverlay.assertDisplayed()

        help.assertDisplayed()
        changeUser.assertDisplayed()
        logoutButton.assertDisplayed()
    }

    private fun assertElementaryNavigationBehaviorMenuItems() {
        bookmarks.assertNotDisplayed()
        showGrades.assertNotDisplayed()
        colorOverlay.assertNotDisplayed()

        files.assertDisplayed()
        settings.assertDisplayed()
        help.assertDisplayed()
        changeUser.assertDisplayed()
        logoutButton.assertDisplayed()
    }

    //OfflineMethod
    private fun assertViewAlpha(matcher: Matcher<View>, expectedAlphaValue: Float) {
        onView(matcher).check(ViewAlphaAssertion(expectedAlphaValue))
    }

    //OfflineMethod
    private fun assertFilesMenuAlphaValue(expectedAlphaValue: Float) {
        assertViewAlpha(withId(R.id.navigationDrawerItem_files), expectedAlphaValue)
    }

    //OfflineMethod
    private fun assertBookmarksMenuAlphaValue(expectedAlphaValue: Float) {
        assertViewAlpha(withId(R.id.navigationDrawerItem_bookmarks), expectedAlphaValue)
    }

    //OfflineMethod
    private fun assertStudioMenuAlphaValue(expectedAlphaValue: Float) {
        assertViewAlpha(withId(R.id.navigationDrawerItem_studio), expectedAlphaValue)
    }

    //OfflineMethod
    private fun assertSettingsMenuAlphaValue(expectedAlphaValue: Float) {
        assertViewAlpha(withId(R.id.navigationDrawerSettings), expectedAlphaValue)
    }

    //OfflineMethod
    private fun assertShowGradesMenuAlphaValue(expectedAlphaValue: Float) {
        assertViewAlpha(withId(R.id.navigationDrawerItem_showGrades), expectedAlphaValue)
    }

    //OfflineMethod
    private fun assertColorOverlayMenuAlphaValue(expectedAlphaValue: Float) {
        assertViewAlpha(withId(R.id.navigationDrawerItem_colorOverlay), expectedAlphaValue)
    }

    //OfflineMethod
    private fun assertHelpMenuAlphaValue(expectedAlphaValue: Float) {
        assertViewAlpha(withId(R.id.navigationDrawerItem_help), expectedAlphaValue)
    }

    //OfflineMethod
    private fun assertChangeUserMenuAlphaValue(expectedAlphaValue: Float) {
        assertViewAlpha(withId(R.id.navigationDrawerItem_changeUser), expectedAlphaValue)
    }

    //OfflineMethod
    private fun assertLogoutMenuAlphaValue(expectedAlphaValue: Float) {
        assertViewAlpha(withId(R.id.navigationDrawerItem_logout), expectedAlphaValue)
    }

    //OfflineMethod
    fun assertOfflineDisabledMenus(expectedAlphaValue: Float) {
        assertFilesMenuAlphaValue(expectedAlphaValue)
        assertBookmarksMenuAlphaValue(expectedAlphaValue)
        assertStudioMenuAlphaValue(expectedAlphaValue)
        assertColorOverlayMenuAlphaValue(expectedAlphaValue)
        assertHelpMenuAlphaValue(expectedAlphaValue)
    }

    //OfflineMethod
    fun assertOfflineEnabledMenus(expectedAlphaValue: Float) {
        assertSettingsMenuAlphaValue(expectedAlphaValue)
        assertShowGradesMenuAlphaValue(expectedAlphaValue)
        assertChangeUserMenuAlphaValue(expectedAlphaValue)
        assertLogoutMenuAlphaValue(expectedAlphaValue)
    }

    /**
     * Custom ViewAction to set a SwitchCompat to the desired on/off position
     * [position]: true -> "on", false -> "off"
     */
    private class SetSwitchCompat(val position: Boolean) : ViewAction {
        override fun getDescription(): String {
            val desiredPosition = if (position) "On" else "Off"
            return "Set SwitchCompat to $desiredPosition"
        }

        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isAssignableFrom(SwitchCompat::class.java)
        }

        override fun perform(uiController: UiController?, view: View?) {
            val switch = view as SwitchCompat
            if (switch != null) {
                switch.isChecked = position
            }
        }
    }
}

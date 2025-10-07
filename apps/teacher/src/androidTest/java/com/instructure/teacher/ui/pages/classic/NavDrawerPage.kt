package com.instructure.teacher.ui.pages.classic

import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvasapi2.models.User
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.page.BasePage
import com.instructure.teacher.R

/**
 * Represents the Navigation Drawer Page.
 *
 * This page extends the BasePage class and provides functionality for interacting with the navigation drawer.
 * It contains assertion methods to verify the profile details of the user displayed in the navigation drawer.
 */
class NavDrawerPage: BasePage() {

    private val settings by OnViewWithId(R.id.navigationDrawerSettings)
    private val userName by OnViewWithId(R.id.navigationDrawerUserName)
    private val userEmail by OnViewWithId(R.id.navigationDrawerUserEmail)
    private val changeUser by OnViewWithId(R.id.navigationDrawerItem_changeUser)
    private val logout by OnViewWithId(R.id.navigationDrawerItem_logout)
    private val version by OnViewWithId(R.id.navigationDrawerVersion)

    /**
     * Asserts the profile details of the user displayed in the navigation drawer.
     *
     * @param teacher The user whose profile details are expected to be displayed.
     */
    fun assertProfileDetails(teacher: User) {
        userName.check(matches(withText(teacher.shortName)))
    }
}


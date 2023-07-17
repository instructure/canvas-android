package com.instructure.teacher.ui.pages

import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onViewWithText
import com.instructure.teacher.R

/**
 * Represents the Login Landing Page.
 *
 * This page extends the BasePage class and provides functionality for interacting with the login
 * landing page. It contains various view elements such as canvas logo image view, find my school button,
 * find another school button, last saved school button, canvas network text view, previous login wrapper,
 * previous login title text, previous login divider, previous login recycler view, canvas wordmark view,
 * and app description type text view.
 */
@Suppress("unused")
class LoginLandingPage : BasePage() {

    private val canvasLogoImageView by OnViewWithId(R.id.canvasLogo)
    private val findMySchoolButton by OnViewWithId(R.id.findMySchool, autoAssert = false)
    private val findAnotherSchoolButton by OnViewWithId(R.id.findAnotherSchool, autoAssert = false)
    private val lastSavedSchoolButton by OnViewWithId(R.id.openRecentSchool, autoAssert = false)
    private val canvasNetworkTextView by OnViewWithId(R.id.canvasNetwork)
    private val previousLoginWrapper by OnViewWithId(R.id.previousLoginWrapper, autoAssert = false)
    private val previousLoginTitleText by  OnViewWithId(R.id.previousLoginTitleText, autoAssert = false)
    private val previousLoginDivider by  OnViewWithId(R.id.previousLoginDivider, autoAssert = false)
    private val previousLoginRecyclerView by  OnViewWithId(R.id.previousLoginRecyclerView, autoAssert = false)
    private val canvasWordmarkView by OnViewWithId(R.id.canvasWordmark, autoAssert = false)
    private val appDescriptionTypeTextView by OnViewWithId(R.id.appDescriptionType, autoAssert = false)

    /**
     * Clicks the "Find My School" button.
     */
    fun clickFindMySchoolButton() {
        findMySchoolButton.click()
    }

    /**
     * Clicks the "Find Another School" button.
     */
    fun clickFindAnotherSchoolButton() {
        findAnotherSchoolButton.click()
    }

    /**
     * Clicks on the "Last Saved School" button.
     */
    fun clickOnLastSavedSchoolButton() {
        lastSavedSchoolButton.click()
    }

    /**
     * Clicks on the "Canvas Network" button.
     */
    fun clickCanvasNetworkButton() {
        canvasNetworkTextView.click()
    }

    /**
     * Asserts that the canvas wordmark view is displayed.
     */
    fun assertDisplaysCanvasWorkmark() {
        canvasWordmarkView.assertDisplayed()
    }

    /**
     * Asserts that the app description type text view is displayed.
     */
    fun assertDisplaysAppDescriptionType() {
        appDescriptionTypeTextView.assertDisplayed()
    }

    /**
     * Asserts that the previous logins section is displayed.
     */
    fun assertDisplaysPreviousLogins() {
        previousLoginTitleText.assertDisplayed()
    }

    /**
     * Logs in with the previous user specified.
     *
     * @param previousUser The previous user to log in with.
     */
    fun loginWithPreviousUser(previousUser: CanvasUserApiModel) {
        onViewWithText(previousUser.name).click()
    }

    /**
     * Logs in with the previous user specified.
     *
     * @param previousUser The previous user to log in with.
     */
    fun loginWithPreviousUser(previousUser: User) {
        onViewWithText(previousUser.name).click()
    }
}


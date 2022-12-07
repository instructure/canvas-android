package com.instructure.teacher.ui.pages

import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onViewWithText
import com.instructure.teacher.R

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

    fun clickFindMySchoolButton() {
        findMySchoolButton.click()
    }

    fun clickFindAnotherSchoolButton() {
        findAnotherSchoolButton.click()
    }

    fun clickOnLastSavedSchoolButton() {
        lastSavedSchoolButton.click()
    }

    fun clickCanvasNetworkButton() {
        canvasNetworkTextView.click()
    }

    fun assertDisplaysCanvasWorkmark() {
        canvasWordmarkView.assertDisplayed()
    }

    fun assertDisplaysAppDescriptionType() {
        appDescriptionTypeTextView.assertDisplayed()
    }

    fun assertDisplaysPreviousLogins() {
        previousLoginTitleText.assertDisplayed()
    }

    fun loginWithPreviousUser(previousUser: CanvasUserApiModel) {
        onViewWithText(previousUser.name).click()
    }

    fun loginWithPreviousUser(previousUser: User) {
        onViewWithText(previousUser.name).click()
    }
}

package com.instructure.teacher.ui.pages

import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.teacher.R

@Suppress("unused")
class LoginLandingPage : BasePage() {

    private val canvasLogoImageView by OnViewWithId(R.id.canvasLogo)
    private val findMySchoolButton by OnViewWithId(R.id.findMySchool)
    private val canvasNetworkTextView by OnViewWithId(R.id.canvasNetwork)
    private val previousLoginWrapper by OnViewWithId(R.id.previousLoginWrapper, autoAssert = false)
    private val previousLoginTitleText by  OnViewWithId(R.id.previousLoginTitleText, autoAssert = false)
    private val previousLoginDivider by  OnViewWithId(R.id.previousLoginDivider, autoAssert = false)
    private val previousLoginRecyclerView by  OnViewWithId(R.id.previousLoginRecyclerView, autoAssert = false)
    private val canvasNameTextView by OnViewWithId(R.id.canvasName, autoAssert = false)
    private val appDescriptionTypeTextView by OnViewWithId(R.id.appDescriptionType, autoAssert = false)

    fun clickFindMySchoolButton() {
        findMySchoolButton.click()
    }

    fun clickCanvasNetworkButton() {
        canvasNetworkTextView.click()
    }

    fun assertDisplaysCanvasName() {
        canvasNameTextView.assertDisplayed()
    }

    fun assertDisplaysAppDescriptionType() {
        appDescriptionTypeTextView.assertDisplayed()
    }
}

package com.instructure.teacher.ui.pages

import androidx.test.espresso.web.assertion.WebViewAssertions
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.DriverAtoms.clearElement
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.DriverAtoms.webKeys
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.page.BasePage
import com.instructure.teacher.R
import org.hamcrest.CoreMatchers

/**
 * Represents the Login Sign-In Page.
 *
 * This page extends the BasePage class and provides functionality for interacting with the login
 * sign-in page. It contains various view elements and helper methods for locating and interacting
 * with UI elements on the page.
 */
@Suppress("unused")
class LoginSignInPage : BasePage() {

    private val EMAIL_FIELD_CSS = "input[name=\"pseudonym_session[unique_id]\"]"
    private val PASSWORD_FIELD_CSS = "input[name=\"pseudonym_session[password]\"]"
    private val LOGIN_BUTTON_CSS = "button[type=\"submit\"]"
    private val FORGOT_PASSWORD_BUTTON_CSS = "a[class=\"forgot-password flip-to-back\"]"
    private val AUTHORIZE_BUTTON_CSS = "button[type=\"submit\"]"
    private val LOGIN_ERROR_MESSAGE_HOLDER_CSS = "div[class='error']"

    private val signInRoot by OnViewWithId(R.id.signInRoot, autoAssert = false)
    private val toolbar by OnViewWithId(R.id.toolbar, autoAssert = false)

    //region UI Element Locator Methods

    private fun emailField(): Web.WebInteraction<*> {
        return onWebView().withElement(findElement(Locator.CSS_SELECTOR, EMAIL_FIELD_CSS))
    }

    private fun passwordField(): Web.WebInteraction<*> {
        return onWebView().withElement(findElement(Locator.CSS_SELECTOR, PASSWORD_FIELD_CSS))
    }

    private fun loginButton(): Web.WebInteraction<*> {
        return onWebView().withElement(findElement(Locator.CSS_SELECTOR, LOGIN_BUTTON_CSS))
    }

    private fun forgotPasswordButton(): Web.WebInteraction<*> {
        return onWebView().withElement(findElement(Locator.CSS_SELECTOR, FORGOT_PASSWORD_BUTTON_CSS))
    }

    private fun authorizeButton(): Web.WebInteraction<*> {
        return onWebView().withElement(findElement(Locator.CSS_SELECTOR, AUTHORIZE_BUTTON_CSS))
    }

    private fun loginErrorMessageHolder(): Web.WebInteraction<*> {
        return onWebView().withElement(findElement(Locator.CSS_SELECTOR, LOGIN_ERROR_MESSAGE_HOLDER_CSS))
    }

    //endregion

    //region Assertion Helpers

    /**
     * Asserts the presence of page objects on the Sign-In page.
     *
     * @param duration The duration to wait for the assertion.
     */
    override fun assertPageObjects(duration: Long) {
        signInRoot.assertDisplayed()
        toolbar.assertDisplayed()

        emailField()
        passwordField()
        loginButton()
        forgotPasswordButton()
    }

    //endregion

    //region UI Action Helpers

    /**
     * Enters the email into the email field.
     *
     * @param email The email to enter.
     */
    fun enterEmail(email: String) {
        emailField().perform(clearElement())
        emailField().perform(webKeys(email))
    }

    /**
     * Enters the password into the password field.
     *
     * @param password The password to enter.
     */
    fun enterPassword(password: String) {
        passwordField().perform(clearElement())
        passwordField().perform(webKeys(password))
    }

    /**
     * Clicks the login button.
     */
    fun clickLoginButton() {
        loginButton().perform(webClick())
    }

    /**
     * Clicks the forgot password button.
     */
    fun clickForgotPasswordButton() {
        forgotPasswordButton().perform(webClick())
    }

    /**
     * Asserts the login error message.
     *
     * @param errorMessage The expected error message.
     */
    fun assertLoginErrorMessage(errorMessage: String) {
        loginErrorMessageHolder().check(
            WebViewAssertions.webMatches(
                DriverAtoms.getText(),
                CoreMatchers.containsString(errorMessage)
            )
        )
    }

    /**
     * Logs in as the specified teacher.
     *
     * @param teacher The teacher to log in as.
     */
    fun loginAs(teacher: CanvasUserApiModel) {
        loginAs(teacher.loginId, teacher.password)
    }

    /**
     * Logs in with the specified login ID and password.
     *
     * @param loginId The login ID to enter.
     * @param password The password to enter.
     */
    fun loginAs(loginId: String, password: String) {
        enterEmail(loginId)
        enterPassword(password)
        clickLoginButton()

        // Apparently not necessary when running on mobileqa.beta.instructure.com
//        authorizeButton().repeatedlyUntilNot(action = webClick(),
//                desiredStateMatcher = ::authorizeButton,
//                maxAttempts = 20)
    }

    //endregion
}



package com.instructure.teacher.ui.pages

import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.checkToastText
import com.instructure.canvas.espresso.withElementRepeat
import com.instructure.dataseeding.model.PageApiModel
import com.instructure.espresso.ActivityHelper
import com.instructure.espresso.ModuleItemInteractions
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.click
import com.instructure.espresso.extractInnerTextById
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.replaceText
import com.instructure.espresso.scrollTo
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TypeInRCETextEditor
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString

/**
 * The `EditPageDetailsPage` class represents a page for editing page details.
 * It extends the `BasePage` class.
 *
 * @constructor Creates an instance of `EditPageDetailsPage`.
 */
class EditPageDetailsPage(val moduleItemInteractions: ModuleItemInteractions) : BasePage() {
    private val contentRceView by WaitForViewWithId(R.id.rce_webView)

    /**
     * Runs text checks on the web view.
     *
     * @param checks The array of `WebViewTextCheck` objects representing the text checks to perform.
     */
    fun runTextChecks(vararg checks: WebViewTextCheck) {
        for (check in checks) {
            if (check.repeatSecs != null) {
                onWebView(allOf(withId(R.id.contentWebView), isDisplayed()))
                    .withElementRepeat(findElement(check.locatorType, check.locatorValue), check.repeatSecs)
                    .check(webMatches(getText(), containsString(check.textValue)))
            } else {
                onWebView(allOf(withId(R.id.contentWebView), isDisplayed()))
                    .withElement(findElement(check.locatorType, check.locatorValue))
                    .check(webMatches(getText(), containsString(check.textValue)))
            }
        }
    }

    /**
     * Opens the edit page.
     */
    fun openEdit() {
        onView(withId(R.id.menu_edit)).click()
    }

    /**
     * Saves the page.
     */
    fun savePage() {
        onView(withId(R.id.menuSavePage)).click()
    }

    /**
     * Toggles the front page.
     */
    fun toggleFrontPage() {
        onView(withId(R.id.frontPageSwitchWrapper)).scrollTo()
        onView(withId(R.id.frontPageSwitch)).click()
    }

    /**
     * Toggles the published state of the page.
     */
    fun togglePublished() {
        onView(withId(R.id.publishWrapper)).scrollTo()
        onView(withId(R.id.publishSwitch)).click()
    }

    /**
     * Edits the name of the page.
     *
     * @param editedPageName The edited name of the page.
     */
    fun editPageName(editedPageName: String) {
        onView(withId(R.id.pageNameEditText)).replaceText(editedPageName)
    }

    /**
     * Edits the description of the page.
     *
     * @param newDescription The new description of the page.
     */
    fun editDescription(newDescription: String) {
        contentRceView.perform(TypeInRCETextEditor(newDescription))
    }

    /**
     * Displays a toast message indicating that saving an unpublished front page is not possible.
     */
    fun unableToSaveUnpublishedFrontPage() {
        savePage()
        checkToastText(R.string.frontPageUnpublishedError, ActivityHelper.currentActivity())
    }

    /**
     * Assert that the page's body is equal to the expected
     *
     * @param page The page object to assert.
     */
    fun assertPageDetails(page: PageApiModel) {
        val innerText = extractInnerTextById(page.body, "header1")
        runTextChecks(WebViewTextCheck(Locator.ID, "header1", innerText!!))
    }
}

data class WebViewTextCheck(
        val locatorType: Locator,
        val locatorValue: String,
        val textValue: String,
        val repeatSecs: Int? = null
)
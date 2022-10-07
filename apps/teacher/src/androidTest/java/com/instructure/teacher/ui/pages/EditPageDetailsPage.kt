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
import com.instructure.espresso.*
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TypeInRCETextEditor
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString

class EditPageDetailsPage : BasePage() {
    private val contentRceView by WaitForViewWithId(R.id.rce_webView)

    fun runTextChecks(vararg checks : WebViewTextCheck) {
        for(check in checks) {
            if(check.repeatSecs != null) {
                onWebView(allOf(withId(R.id.canvasWebView), isDisplayed()))
                        .withElementRepeat(findElement(check.locatorType, check.locatorValue), check.repeatSecs)
                        .check(webMatches(getText(), containsString(check.textValue)))
            }
            else {
                onWebView(allOf(withId(R.id.canvasWebView), isDisplayed()))
                        .withElement(findElement(check.locatorType, check.locatorValue))
                        .check(webMatches(getText(), containsString(check.textValue)))
            }
        }
    }

    fun openEdit() {
        onView(withId(R.id.menu_edit)).click()
    }

    fun savePage() {
        onView(withId(R.id.menuSavePage)).click()
    }

    fun toggleFrontPage() {
        onView(withId(R.id.frontPageSwitchWrapper)).scrollTo()
        onView(withId(R.id.frontPageSwitch)).click()
    }

    fun togglePublished() {
        onView(withId(R.id.publishWrapper)).scrollTo()
        onView(withId(R.id.publishSwitch)).click()
    }

    fun editPageName(editedPageName: String) {
        onView(withId(R.id.pageNameEditText)).replaceText(editedPageName)
    }

    fun editDescription(newDescription: String) {
        contentRceView.perform(TypeInRCETextEditor(newDescription))
    }

    fun unableToSaveUnpublishedFrontPage() {
        savePage()
        checkToastText(R.string.frontPageUnpublishedError, ActivityHelper.currentActivity())
    }
}
data class WebViewTextCheck(
        val locatorType: Locator,
        val locatorValue: String,
        val textValue: String,
        val repeatSecs: Int? = null
)
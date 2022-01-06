package com.instructure.teacher.ui.pages

import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.checkToastText
import com.instructure.canvas.espresso.withElementRepeat
import com.instructure.espresso.ActivityHelper
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withParent
import com.instructure.espresso.replaceText
import com.instructure.espresso.scrollTo
import com.instructure.teacher.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString

class EditPageDetailsPage : BasePage() {

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
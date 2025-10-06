/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.ui.pages.classic

import androidx.annotation.StringRes
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.model.Atoms.getCurrentUrl
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.DriverAtoms.webScrollIntoView
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.withElementRepeat
import com.instructure.espresso.assertVisible
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.onViewWithId
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.student.R
import com.instructure.student.ui.utils.TypeInRCETextEditor
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString

/**
 * An abstraction for operations on a full-screen (or mostly-full-screen) webpage.
 */
open class CanvasWebViewPage : BasePage(R.id.contentWebView) {

    fun assertTitle(@StringRes title: Int) {
        onView(withAncestor(R.id.toolbar) + withText(title)).assertVisible()
    }

    fun assertTitle(title: String) {
        onView(withAncestor(R.id.toolbar) + withText(title)).assertVisible()
    }

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

    fun checkWebViewURL(expectedURL: String) {
        onWebView(allOf(withId(R.id.contentWebView), isDisplayed()))
            .check(webMatches(getCurrentUrl(), containsString(expectedURL)))
    }

    fun acceptCookiePolicyIfNecessary() {
        try {
            onWebView(allOf(withId(R.id.contentWebView), isDisplayed()))
                .withElementRepeat(findElement(Locator.ID, "onetrust-accept-btn-handler"))
                .perform(webClick())
        }
        catch(t: Throwable) {
            // Take no action if gdprAccept is not displayed
        }
    }

    fun pressButton(locatorType : Locator, locatorValue: String) {
        onWebView(allOf(withId(R.id.contentWebView), isDisplayed()))
                .withElement(findElement(locatorType, locatorValue))
                .perform(webScrollIntoView())
                .perform(webClick())
    }

    fun pressButton(locatorType : Locator, locatorValue: String, subElementType : Locator, subElementValue: String) {
        onWebView(allOf(withId(R.id.contentWebView), isDisplayed()))
                .withElement(findElement(locatorType, locatorValue))
                .withContextualElement(findElement(subElementType, subElementValue))
                .perform(webClick())
    }

    fun waitForWebView() {
        waitForView(allOf(withId(R.id.contentWebView), isDisplayed()))
    }

    fun clickEditPencilIcon() {
        onView(withId(R.id.menu_edit)).click()
    }

    fun assertDoesNotEditable() {
        onView(withId(R.id.menu_edit)).check(doesNotExist())
    }

    fun typeInRCEEditor(textToType: String) {
        waitForView(ViewMatchers.withId(R.id.rce_webView)).perform(TypeInRCETextEditor(textToType))
    }

    fun clickOnSave() {
        onViewWithId(R.id.menuSavePage).click()
    }

}

/** data class that encapsulates info for a webview text check */
// TODO: There may still be a better place for this
data class WebViewTextCheck(
        val locatorType: Locator,
        val locatorValue: String,
        val textValue: String,
        val repeatSecs: Int? = null
)
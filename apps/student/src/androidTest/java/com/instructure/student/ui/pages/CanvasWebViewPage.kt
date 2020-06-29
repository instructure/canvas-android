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
package com.instructure.student.ui.pages

import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.withElementRepeat
import com.instructure.espresso.page.BasePage
import com.instructure.student.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString

/**
 * An abstraction for operations on a full-screen (or mostly-full-screen) webpage.
 */
class CanvasWebViewPage : BasePage(R.id.canvasWebView) {
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

    fun acceptCookiePolicyIfNecessary() {
        try {
            onWebView(allOf(withId(R.id.canvasWebView), isDisplayed()))
                    .withElement(findElement(Locator.ID, "gdprAccept"))
                    .perform(webClick())
        }
        catch(t: Throwable) {
            // Take no action if gdprAccept is not displayed
        }
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
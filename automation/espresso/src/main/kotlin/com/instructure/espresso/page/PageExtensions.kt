/*
 * Copyright (C) 2018 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
@file:Suppress("unused")

package com.instructure.espresso.page

import android.view.View
import androidx.annotation.PluralsRes
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import com.adevinta.android.barista.interaction.BaristaScrollInteractions
import com.adevinta.android.barista.interaction.BaristaSleepInteractions
import com.instructure.espresso.actions.ViewCallOnClick
import com.instructure.espresso.click
import com.instructure.espresso.matchers.WaitForViewMatcher
import org.hamcrest.Matcher
import org.hamcrest.Matchers

operator fun Matcher<View>.plus(other: Matcher<View>): Matcher<View> = Matchers.allOf(this, other)

operator fun Matcher<View>.minus(other: Matcher<View>): Matcher<View> = Matchers.allOf(this, Matchers.not(other))

fun BasePage.onView(matcher: Matcher<View>): ViewInteraction = Espresso.onView(matcher)

fun BasePage.waitForView(matcher: Matcher<View>): ViewInteraction = WaitForViewMatcher.waitForView(matcher)

fun BasePage.withId(id: Int): Matcher<View> = ViewMatchers.withId(id)

fun BasePage.withParent(id: Int): Matcher<View> = ViewMatchers.withParent(withId(id))

fun BasePage.withParent(matcher: Matcher<View>): Matcher<View> = ViewMatchers.withParent(matcher)

fun BasePage.withAncestor(id: Int): Matcher<View> = ViewMatchers.isDescendantOfA(withId(id))

fun BasePage.withAncestor(matcher: Matcher<View>): Matcher<View> = ViewMatchers.isDescendantOfA(matcher)

fun BasePage.withChild(id: Int): Matcher<View> = ViewMatchers.withChild(withId(id))

fun BasePage.withChild(matcher: Matcher<View>): Matcher<View> = ViewMatchers.withChild(matcher)

fun BasePage.withDescendant(descendantMatcher: Matcher<View>): Matcher<View> = ViewMatchers.hasDescendant(descendantMatcher)

fun BasePage.withText(arg0: String): Matcher<View> = ViewMatchers.withText(arg0)

fun BasePage.withText(arg0: Int): Matcher<View> = ViewMatchers.withText(arg0)

fun BasePage.onViewWithParent(arg0: Matcher<View>): ViewInteraction = Espresso.onView(ViewMatchers.withParent(arg0))

fun BasePage.onViewWithText(arg0: String): ViewInteraction = Espresso.onView(ViewMatchers.withText(arg0))

fun BasePage.onViewWithText(arg0: Matcher<String>): ViewInteraction = Espresso.onView(ViewMatchers.withText(arg0))

fun BasePage.onViewWithText(arg0: Int): ViewInteraction = Espresso.onView(ViewMatchers.withText(arg0))

fun BasePage.onViewWithId(arg0: Int): ViewInteraction = Espresso.onView(ViewMatchers.withId(arg0))

fun BasePage.onViewWithId(arg0: Matcher<Int>): ViewInteraction = Espresso.onView(ViewMatchers.withId(arg0))

fun BasePage.onViewWithClassName(arg0: Matcher<String>): ViewInteraction = Espresso.onView(ViewMatchers.withClassName(arg0))

fun BasePage.onViewWithContentDescription(arg0: Int): ViewInteraction = Espresso.onView(ViewMatchers.withContentDescription(arg0))

fun BasePage.onViewWithContentDescription(arg0: Matcher<out CharSequence>): ViewInteraction = Espresso.onView(ViewMatchers.withContentDescription(arg0))

fun BasePage.onViewWithContentDescription(arg0: String): ViewInteraction = Espresso.onView(ViewMatchers.withContentDescription(arg0))

fun BasePage.onViewWithResourceName(arg0: String): ViewInteraction = Espresso.onView(ViewMatchers.withResourceName(arg0))

fun BasePage.onViewWithResourceName(arg0: Matcher<String>): ViewInteraction = Espresso.onView(ViewMatchers.withResourceName(arg0))

fun BasePage.onViewWithTagKey(arg0: Int, arg1: Matcher<Any>): ViewInteraction = Espresso.onView(ViewMatchers.withTagKey(arg0, arg1))

fun BasePage.onViewWithTagKey(arg0: Int): ViewInteraction = Espresso.onView(ViewMatchers.withTagKey(arg0))

fun BasePage.onViewWithTagValue(arg0: Matcher<Any>): ViewInteraction = Espresso.onView(ViewMatchers.withTagValue(arg0))

fun BasePage.onViewWithHint(arg0: Int): ViewInteraction = Espresso.onView(ViewMatchers.withHint(arg0))

fun BasePage.onViewWithHint(arg0: Matcher<String>): ViewInteraction = Espresso.onView(ViewMatchers.withHint(arg0))

fun BasePage.onViewWithHint(arg0: String): ViewInteraction = Espresso.onView(ViewMatchers.withHint(arg0))

fun BasePage.onViewWithEffectiveVisibility(arg0: ViewMatchers.Visibility): ViewInteraction = Espresso.onView(ViewMatchers.withEffectiveVisibility(arg0))

fun BasePage.onViewWithChild(arg0: Matcher<View>): ViewInteraction = Espresso.onView(ViewMatchers.withChild(arg0))

fun BasePage.onViewWithSpinnerText(arg0: String): ViewInteraction = Espresso.onView(ViewMatchers.withSpinnerText(arg0))

fun BasePage.onViewWithSpinnerText(arg0: Matcher<String>): ViewInteraction = Espresso.onView(ViewMatchers.withSpinnerText(arg0))

fun BasePage.onViewWithSpinnerText(arg0: Int): ViewInteraction = Espresso.onView(ViewMatchers.withSpinnerText(arg0))

fun BasePage.onViewWithInputType(arg0: Int): ViewInteraction = Espresso.onView(ViewMatchers.withInputType(arg0))

fun BasePage.waitForViewWithParent(arg0: Matcher<View>): ViewInteraction = WaitForViewMatcher.waitForView(ViewMatchers.withParent(arg0))

fun BasePage.waitForViewWithText(arg0: String): ViewInteraction = WaitForViewMatcher.waitForView(ViewMatchers.withText(arg0))

fun BasePage.waitForViewWithText(arg0: Matcher<String>): ViewInteraction = WaitForViewMatcher.waitForView(ViewMatchers.withText(arg0))

fun BasePage.waitForViewWithText(arg0: Int): ViewInteraction = WaitForViewMatcher.waitForView(ViewMatchers.withText(arg0))

fun BasePage.waitForViewWithId(arg0: Int): ViewInteraction = WaitForViewMatcher.waitForView(ViewMatchers.withId(arg0))

fun BasePage.waitForViewWithId(arg0: Matcher<Int>): ViewInteraction = WaitForViewMatcher.waitForView(ViewMatchers.withId(arg0))

fun BasePage.waitForViewWithClassName(arg0: Matcher<String>): ViewInteraction = WaitForViewMatcher.waitForView(ViewMatchers.withClassName(arg0))

fun BasePage.waitForViewWithContentDescription(arg0: Int): ViewInteraction = WaitForViewMatcher.waitForView(ViewMatchers.withContentDescription(arg0))

fun BasePage.waitForViewWithContentDescription(arg0: Matcher<out CharSequence>): ViewInteraction = WaitForViewMatcher.waitForView(ViewMatchers.withContentDescription(arg0))

fun BasePage.waitForViewWithContentDescription(arg0: String): ViewInteraction = WaitForViewMatcher.waitForView(ViewMatchers.withContentDescription(arg0))

fun BasePage.waitForViewWithResourceName(arg0: String): ViewInteraction = WaitForViewMatcher.waitForView(ViewMatchers.withResourceName(arg0))

fun BasePage.waitForViewWithResourceName(arg0: Matcher<String>): ViewInteraction = WaitForViewMatcher.waitForView(ViewMatchers.withResourceName(arg0))

fun BasePage.waitForViewWithTagKey(arg0: Int, arg1: Matcher<Any>): ViewInteraction = WaitForViewMatcher.waitForView(ViewMatchers.withTagKey(arg0, arg1))

fun BasePage.waitForViewWithTagKey(arg0: Int): ViewInteraction = WaitForViewMatcher.waitForView(ViewMatchers.withTagKey(arg0))

fun BasePage.waitForViewWithTagValue(arg0: Matcher<Any>): ViewInteraction = WaitForViewMatcher.waitForView(ViewMatchers.withTagValue(arg0))

fun BasePage.waitForViewWithHint(arg0: Int): ViewInteraction = WaitForViewMatcher.waitForView(ViewMatchers.withHint(arg0))

fun BasePage.waitForViewWithHint(arg0: Matcher<String>): ViewInteraction = WaitForViewMatcher.waitForView(ViewMatchers.withHint(arg0))

fun BasePage.waitForViewWithHint(arg0: String): ViewInteraction = WaitForViewMatcher.waitForView(ViewMatchers.withHint(arg0))

fun BasePage.waitForViewWithEffectiveVisibility(arg0: ViewMatchers.Visibility): ViewInteraction = WaitForViewMatcher.waitForView(ViewMatchers.withEffectiveVisibility(arg0))

fun BasePage.waitForViewWithChild(arg0: Matcher<View>): ViewInteraction = WaitForViewMatcher.waitForView(ViewMatchers.withChild(arg0))

fun BasePage.waitForViewWithSpinnerText(arg0: String): ViewInteraction = WaitForViewMatcher.waitForView(ViewMatchers.withSpinnerText(arg0))

fun BasePage.waitForViewWithSpinnerText(arg0: Matcher<String>): ViewInteraction = WaitForViewMatcher.waitForView(ViewMatchers.withSpinnerText(arg0))

fun BasePage.waitForViewWithSpinnerText(arg0: Int): ViewInteraction = WaitForViewMatcher.waitForView(ViewMatchers.withSpinnerText(arg0))

fun BasePage.waitForViewWithInputType(arg0: Int): ViewInteraction = WaitForViewMatcher.waitForView(ViewMatchers.withInputType(arg0))

fun getStringFromResource(stringResource: Int): String{
    val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
    return targetContext.resources.getString(stringResource)
}

fun BasePage.getStringFromResource(stringResource: Int, vararg params: Any): String {
    val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
    return targetContext.resources.getString(stringResource, *params)
}

fun BasePage.getPluralFromResource(@PluralsRes pluralsResource: Int, quantity: Int, vararg params: Any): String {
    val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
    return targetContext.resources.getQuantityString(pluralsResource, quantity, *params)
}

fun BasePage.callOnClick(matcher: Matcher<View>) = ViewCallOnClick.callOnClick(matcher)

fun BasePage.scrollTo(viewId: Int) = BaristaScrollInteractions.safelyScrollTo(viewId)

fun BasePage.scrollTo(text: String) = BaristaScrollInteractions.safelyScrollTo(text)

fun BasePage.scrollTo(matcher: Matcher<View>) = BaristaScrollInteractions.safelyScrollTo(matcher)

fun BasePage.waitScrollClick(viewId: Int) {
    val view = waitForViewWithId(viewId)
    scrollTo(viewId)
    view.click()
}

fun BasePage.pause(milli: Long = 100) = BaristaSleepInteractions.sleep(milli)

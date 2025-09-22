/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 *
 */
@file:Suppress("unused")

package com.instructure.espresso

import android.view.View
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.EspressoKey
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.canvas.espresso.SetViewPagerCurrentItemAction
import io.github.kakaocup.kakao.common.views.KBaseView
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import java.util.concurrent.TimeUnit


fun ViewInteraction.assertVisible(): ViewInteraction
        = assertVisibility(ViewMatchers.Visibility.VISIBLE)

fun ViewInteraction.assertInvisible(): ViewInteraction
        = assertVisibility(ViewMatchers.Visibility.INVISIBLE)

fun ViewInteraction.assertGone(): ViewInteraction
        = assertVisibility(ViewMatchers.Visibility.GONE)

fun ViewInteraction.assertVisibility(visibility: ViewMatchers.Visibility): ViewInteraction
        = check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(visibility)))

fun ViewInteraction.assertDisplayed(): ViewInteraction
        = check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

fun ViewInteraction.assertCompletelyDisplayed(): ViewInteraction
        = check(ViewAssertions.matches(ViewMatchers.isCompletelyDisplayed()))

fun ViewInteraction.assertNotDisplayed(): ViewInteraction
        = check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())))

fun ViewInteraction.assertHasText(text: String): ViewInteraction
        = check(ViewAssertions.matches(ViewMatchers.withText(text)))

fun ViewInteraction.assertHasText(stringId: Int): ViewInteraction
        = check(ViewAssertions.matches(ViewMatchers.withText(stringId)))

fun ViewInteraction.assertNotHasText(stringId: Int): ViewInteraction
        = check(ViewAssertions.matches(Matchers.not(ViewMatchers.withText(stringId))))

fun ViewInteraction.assertContainsText(text: String): ViewInteraction
        = check(ViewAssertions.matches(ViewMatchers.withText(Matchers.containsString(text))))

fun ViewInteraction.assertHasContentDescription(text: String): ViewInteraction
        = check(ViewAssertions.matches(ViewMatchers.withContentDescription(text)))

fun ViewInteraction.assertHasContentDescription(stringId: Int): ViewInteraction
        = check(ViewAssertions.matches(ViewMatchers.withContentDescription(stringId)))

fun ViewInteraction.assertHasChild(matcher: Matcher<View>): ViewInteraction
        = check(ViewAssertions.matches(ViewMatchers.withChild(matcher)))

fun ViewInteraction.assertSelected(): ViewInteraction
    = check(ViewAssertions.matches(ViewMatchers.isSelected()))

// Extensions for ViewActions

fun ViewInteraction.typeText(arg0: String): ViewInteraction = perform(ViewActions.typeText(arg0))

fun ViewInteraction.pressKey(arg0: EspressoKey): ViewInteraction = perform(ViewActions.pressKey(arg0))

fun ViewInteraction.pressKey(arg0: Int): ViewInteraction = perform(ViewActions.pressKey(arg0))

fun ViewInteraction.swipeUp(): ViewInteraction = perform(ViewActions.swipeUp())

fun ViewInteraction.click(): ViewInteraction = perform(ViewActions.click())

fun ViewInteraction.click(arg0: ViewAction): ViewInteraction = perform(ViewActions.click(arg0))

fun ViewInteraction.actionWithAssertions(arg0: ViewAction): ViewInteraction = perform(ViewActions.actionWithAssertions(arg0))

fun ViewInteraction.clearText(): ViewInteraction = perform(ViewActions.clearText())

fun ViewInteraction.swipeLeft(): ViewInteraction = perform(ViewActions.swipeLeft())

fun ViewInteraction.swipeRight(): ViewInteraction = perform(ViewActions.swipeRight())

fun ViewInteraction.swipeDown(): ViewInteraction = perform(ViewActions.swipeDown())

fun ViewInteraction.closeSoftKeyboard(): ViewInteraction = perform(ViewActions.closeSoftKeyboard())

fun ViewInteraction.pressImeActionButton(): ViewInteraction = perform(ViewActions.pressImeActionButton())

fun ViewInteraction.pressBack(): ViewInteraction = perform(ViewActions.pressBack())

fun ViewInteraction.pressMenuKey(): ViewInteraction = perform(ViewActions.pressMenuKey())

fun ViewInteraction.doubleClick(): ViewInteraction = perform(ViewActions.doubleClick())

fun ViewInteraction.longClick(): ViewInteraction = perform(ViewActions.longClick())

fun ViewInteraction.scrollTo(): ViewInteraction = perform(NestedScrollViewExtension())

fun <T : KBaseView<T>> T.scrollToKakao(): T { view.perform(NestedScrollViewExtension())
    return this }

fun ViewInteraction.typeTextIntoFocusedView(arg0: String): ViewInteraction = perform(ViewActions.typeTextIntoFocusedView(arg0))

fun ViewInteraction.replaceText(arg0: String): ViewInteraction = perform(ViewActions.replaceText(arg0))

fun ViewInteraction.openLinkWithText(arg0: org.hamcrest.Matcher<String>): ViewInteraction = perform(ViewActions.openLinkWithText(arg0))

fun ViewInteraction.openLinkWithText(arg0: String): ViewInteraction = perform(ViewActions.openLinkWithText(arg0))

fun ViewInteraction.openLink(arg0: org.hamcrest.Matcher<String>, arg1: org.hamcrest.Matcher<android.net.Uri>): ViewInteraction = perform(ViewActions.openLink(arg0, arg1))

fun ViewInteraction.openLinkWithUri(arg0: org.hamcrest.Matcher<android.net.Uri>): ViewInteraction = perform(ViewActions.openLinkWithUri(arg0))

fun ViewInteraction.openLinkWithUri(arg0: String): ViewInteraction = perform(ViewActions.openLinkWithUri(arg0))

fun ViewInteraction.pageToItem(pageNumber: Int): ViewInteraction = perform(
    SetViewPagerCurrentItemAction(
        pageNumber
    )
)

fun ViewInteraction.swipeToTop(): ViewInteraction = perform(GeneralSwipeAction(Swipe.FAST, CoordinatesProvider {
        GeneralLocation.CENTER.calculateCoordinates(it)
    }, CoordinatesProvider {
        val coordinates = GeneralLocation.CENTER.calculateCoordinates(it)
        coordinates[1] = 0F
        coordinates
    }, Press.FINGER))

fun ViewInteraction.waitForCheck(assertion: ViewAssertion) {
    val waitTime = TimeUnit.SECONDS.toMillis(10)
    val endTime = System.currentTimeMillis() + waitTime
    do {
        try {
            check(assertion)
            return
        } catch (ignored: Throwable) {
        }
    } while (System.currentTimeMillis() < endTime)
    check(assertion)
}

fun ViewInteraction.assertChecked() {
    check(ViewAssertions.matches(ViewMatchers.isChecked()))
}

fun ViewInteraction.assertNotChecked() {
    check(ViewAssertions.matches(ViewMatchers.isNotChecked()))
}

fun ViewInteraction.assertEnabled() {
    check(ViewAssertions.matches(ViewMatchers.isEnabled()))
}

fun ViewInteraction.assertDisabled() {
    check(ViewAssertions.matches(Matchers.not(ViewMatchers.isEnabled())))
}
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
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.espresso.matchers.WaitForViewMatcher
import com.instructure.espresso.page.BasePage
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalToIgnoringCase
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Abstract class for implementing concrete delegate classes for [ViewInteraction] properties.
 *
 * The class hosting the delegated property delegate must be a subclass of [BasePage]. If *true* is passed for the
 * [autoAssert] parameter, the delegate provider will attempt to register the [ViewInteraction] with the [BasePage] so
 * that it may automatically assert that the view is displayed whenever [BasePage.assertPageObjects] is invoked.
 */
abstract class ViewInteractionDelegate(val autoAssert: Boolean) : ReadOnlyProperty<BasePage, ViewInteraction> {

    operator fun provideDelegate(thisRef: BasePage, prop: KProperty<*>): ReadOnlyProperty<BasePage, ViewInteraction> {
        if (autoAssert) thisRef.registerPropertyInfo(Pair(this, prop))
        return this
    }

    abstract fun onProvide(matcher: Matcher<View>): ViewInteraction

    abstract fun getMatcher(): Matcher<View>

    override fun getValue(thisRef: BasePage, property: KProperty<*>): ViewInteraction {
        val matcher = thisRef.pageResId?.let {
            Matchers.allOf(ViewMatchers.isDescendantOfA(ViewMatchers.withId(it)), getMatcher())
        } ?: getMatcher()
        return onProvide(matcher)
    }
}

class OnViewWithId(@IdRes private val viewId: Int, autoAssert: Boolean = true) : ViewInteractionDelegate(autoAssert) {
    override fun onProvide(matcher: Matcher<View>): ViewInteraction = Espresso.onView(matcher)
    override fun getMatcher(): Matcher<View> = ViewMatchers.withId(viewId)
}

class OnViewWithMatcher(private val viewMatcher: Matcher<View>, autoAssert: Boolean = true) : ViewInteractionDelegate(autoAssert) {
    override fun onProvide(matcher: Matcher<View>): ViewInteraction = Espresso.onView(matcher)
    override fun getMatcher(): Matcher<View> = viewMatcher
}

class OnViewWithContentDescription(@StringRes private val stringResId: Int, autoAssert: Boolean = true) : ViewInteractionDelegate(autoAssert) {
    override fun onProvide(matcher: Matcher<View>): ViewInteraction = Espresso.onView(matcher)
    override fun getMatcher(): Matcher<View> = ViewMatchers.withContentDescription(stringResId)
}

class OnViewWithStringContentDescription(val text: String, autoAssert: Boolean = true) : ViewInteractionDelegate(autoAssert) {
    override fun onProvide(matcher: Matcher<View>): ViewInteraction = Espresso.onView(matcher)
    override fun getMatcher(): Matcher<View> = ViewMatchers.withContentDescription(text)
}

class OnViewWithText(@StringRes private val stringResId: Int, autoAssert: Boolean = true) : ViewInteractionDelegate(autoAssert) {
    override fun onProvide(matcher: Matcher<View>): ViewInteraction = Espresso.onView(matcher)
    override fun getMatcher(): Matcher<View> = ViewMatchers.withText(stringResId)
}

class OnViewWithStringText(val text: String, autoAssert: Boolean = true) : ViewInteractionDelegate(autoAssert) {
    override fun onProvide(matcher: Matcher<View>): ViewInteraction = Espresso.onView(matcher)
    override fun getMatcher(): Matcher<View> = ViewMatchers.withText(text)
}

class OnViewWithStringTextIgnoreCase(val text: String, autoAssert: Boolean = true) : ViewInteractionDelegate(autoAssert) {
    override fun onProvide(matcher: Matcher<View>): ViewInteraction = Espresso.onView(matcher)
    override fun getMatcher(): Matcher<View> = ViewMatchers.withText(equalToIgnoringCase(text))
}

class WaitForViewWithId(@IdRes private val viewId: Int, autoAssert: Boolean = false) : ViewInteractionDelegate(autoAssert) {
    override fun onProvide(matcher: Matcher<View>): ViewInteraction = WaitForViewMatcher.waitForView(matcher)
    override fun getMatcher(): Matcher<View> = ViewMatchers.withId(viewId)
}

class WaitForViewWithContentDescription(@StringRes private val stringResId: Int, autoAssert: Boolean = false) : ViewInteractionDelegate(autoAssert) {
    override fun onProvide(matcher: Matcher<View>): ViewInteraction = WaitForViewMatcher.waitForView(matcher)
    override fun getMatcher(): Matcher<View> = ViewMatchers.withContentDescription(stringResId)
}

class WaitForViewWithStringContentDescription(val text: String, autoAssert: Boolean = false) : ViewInteractionDelegate(autoAssert) {
    override fun onProvide(matcher: Matcher<View>): ViewInteraction = WaitForViewMatcher.waitForView(matcher)
    override fun getMatcher(): Matcher<View> = ViewMatchers.withContentDescription(text)
}

class WaitForViewWithText(@StringRes private val stringResId: Int, autoAssert: Boolean = false) : ViewInteractionDelegate(autoAssert) {
    override fun onProvide(matcher: Matcher<View>): ViewInteraction = WaitForViewMatcher.waitForView(matcher)
    override fun getMatcher(): Matcher<View> = ViewMatchers.withText(stringResId)
}

class WaitForViewWithStringTextIgnoreCase(val text: String, autoAssert: Boolean = false) : ViewInteractionDelegate(autoAssert) {
    override fun onProvide(matcher: Matcher<View>): ViewInteraction = WaitForViewMatcher.waitForView(matcher)
    override fun getMatcher(): Matcher<View> = ViewMatchers.withText(equalToIgnoringCase(text))
}

class WaitForViewWithStringText(val text: String, autoAssert: Boolean = false) : ViewInteractionDelegate(autoAssert) {
    override fun onProvide(matcher: Matcher<View>): ViewInteraction = WaitForViewMatcher.waitForView(matcher)
    override fun getMatcher(): Matcher<View> = ViewMatchers.withText(text)
}

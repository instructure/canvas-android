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
package com.instructure.espresso.pages

import androidx.annotation.IdRes
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertVisible
import com.instructure.espresso.matchers.WaitForViewMatcher
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class BasePage(@IdRes val pageResId: Int? = null) {

    open fun assertPageObjects(duration: Long = 10) {
        assertProperties(duration)
    }

    private val assertPropertiesInfo = arrayListOf<Pair<ReadOnlyProperty<BasePage, ViewInteraction>, KProperty<*>>>()

    private fun getRegisteredProperties(): List<ViewInteraction> {
        return assertPropertiesInfo.map { (prop, kProp) -> prop.getValue(this, kProp) }
    }

    fun registerPropertyInfo(info: Pair<ReadOnlyProperty<BasePage, ViewInteraction>, KProperty<*>>) {
        assertPropertiesInfo += info
    }

    private fun assertProperties(duration: Long) {
        pageResId?.let { WaitForViewMatcher.waitForView(ViewMatchers.withId(it), duration).assertDisplayed() }
        getRegisteredProperties().forEach { it.assertVisible() }
    }

}

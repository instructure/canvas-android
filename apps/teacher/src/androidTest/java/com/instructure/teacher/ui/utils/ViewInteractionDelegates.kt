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
package com.instructure.teacher.ui.utils

import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.espresso.ViewInteractionDelegate
import com.instructure.teacher.R
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher


/**
 *  The toolbar's title text view's resource id is the same as the course text view in course cards.
 *  Use this to narrow the matcher to the toolbar itself.
 */
class WaitForToolbarTitle(val text: Int, autoAssert: Boolean = true) : ViewInteractionDelegate(autoAssert) {
    override fun onProvide(matcher: Matcher<View>): ViewInteraction = Espresso.onView(matcher)
    override fun getMatcher(): Matcher<View> {
        return allOf(ViewMatchers.withText(text), ViewMatchers.withParent(ViewMatchers.withId(R.id.toolbar)))
    }
}

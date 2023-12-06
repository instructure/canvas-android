//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//



package com.instructure.espresso.matchers

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import com.instructure.espresso.EspressoLog
import org.hamcrest.Matcher
import java.util.concurrent.*
import java.util.concurrent.atomic.*

object WaitForViewMatcher {
    private val log = EspressoLog(WaitForViewMatcher::class.java)
    private val waiting = AtomicBoolean(false)

    fun finishedWaiting(): Boolean {
        return !waiting.get()
    }

    // http://stackoverflow.com/questions/21417954/espresso-thread-sleep/22563297#22563297
    // https://github.com/braintree/braintree_android/blob/25513d76da88fe2ce9f476c4dc51f24cf6e26104/TestUtils/src/main/java/com/braintreepayments/testutils/ui/ViewHelper.java#L30

    // The viewMatcher is called on every view to determine what matches. Must be fast!
    fun waitForView(viewMatcher: Matcher<View>?, duration: Long = 10): ViewInteraction {
        log.i("Wait for View to be visible.")
        return waitForViewWithCustomMatcher(viewMatcher!!, duration, withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
    }

    fun waitForViewToBeClickable(viewMatcher: Matcher<View>, duration: Long = 10): ViewInteraction {
        log.i("Wait for View to be clickable.")
        return waitForViewWithCustomMatcher(viewMatcher, duration, ViewMatchers.isClickable())
    }

    fun waitForViewToBeCompletelyDisplayed(viewMatcher: Matcher<View>, duration: Long = 10): ViewInteraction {
        log.i("Wait for View to be completely displayed.")
        return waitForViewWithCustomMatcher(viewMatcher, duration, ViewMatchers.isCompletelyDisplayed())
    }

    private fun waitForViewWithCustomMatcher(viewMatcher: Matcher<View>, duration: Long = 10, customMatcher: Matcher<View>): ViewInteraction {
        waiting.set(true)
        val waitTime = TimeUnit.SECONDS.toMillis(duration)
        val endTime = System.currentTimeMillis() + waitTime

        do {
            try {
                val result = onView(viewMatcher).check(matches(customMatcher))
                waiting.set(false)
                return result
            } catch (ignored: Exception) {
            } catch (ignored: Error) {
            }

        } while (System.currentTimeMillis() < endTime)

        waiting.set(false)
        return onView(viewMatcher).check(matches(customMatcher))
    }
}

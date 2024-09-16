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

import androidx.test.espresso.core.internal.deps.dagger.internal.Preconditions.checkNotNull
import com.instructure.espresso.EspressoLog
import com.instructure.espresso.UiControllerSingleton
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class WaitForCheckMatcher<T>(private val matcher: Matcher<T>) : BaseMatcher<T>() {

    override fun matches(arg: Any): Boolean {
        checkNotNull(arg)
        waiting.set(true)

        uiController!!.loopMainThreadUntilIdle()
        val waitTime = TimeUnit.SECONDS.toMillis(10)
        val endTime = System.currentTimeMillis() + waitTime
        do {
            log.i("waitForCheck matching...")
            try {
                if (matcher.matches(arg)) {
                    waiting.set(false)
                    return true
                }
            } catch (ignored: Exception) {
            } catch (ignored: Error) {
            }

            uiController.loopMainThreadForAtLeast(100L)
        } while (System.currentTimeMillis() < endTime)

        waiting.set(false)
        return matcher.matches(arg)
    }

    override fun describeTo(description: Description) {
        description.appendText("wait ").appendDescriptionOf(matcher)
    }

    companion object {
        private val log =
            EspressoLog(WaitForCheckMatcher::class.java)
        private val uiController = UiControllerSingleton.get()

        private val waiting = AtomicBoolean(false)

        fun finishedWaiting(): Boolean {
            return !waiting.get()
        }

        // note: due to conflict with Object.wait, we use 'waitFor'
        //       instead of 'wait' to name the static import.

        /**
         * Creates a wait matcher that wraps an existing matcher.
         * The default wait is 10 seconds.
         *
         *
         * Examples:
         * <pre>
         * onView(withId(...)).check(matches(waitFor(not(isDisplayed()))));
         * onView(withId(...)).check(matches(waitFor(isDisplayed())));
        </pre> *
         *
         * @param matcher the matcher to wrap
         */
        fun <T> waitFor(matcher: Matcher<T>): Matcher<T> {
            return WaitForCheckMatcher(matcher)
        }
    }
}

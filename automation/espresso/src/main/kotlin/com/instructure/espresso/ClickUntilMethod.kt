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


package com.instructure.espresso

import androidx.test.espresso.ViewInteraction

import java.util.concurrent.atomic.AtomicBoolean

import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed

//    before:
//    Espresso.onView(ViewMatchers.withText(R.string.submission_versions)).check(doesNotExist())
//
//    Espresso.onView(withId(R.id.submissionVersionsButton)).perform(click())
//
//    Espresso.onView(ViewMatchers.withText(R.string.submission_versions)).check(matches(isDisplayed()))
//
//    after:
//    ClickUntilMethod.run(
//                Espresso.onView(withId(R.id.submissionVersionsButton)),
//                Espresso.onView(withText(R.string.submission_versions))
//    )
object ClickUntilMethod {

    private val waiting = AtomicBoolean(false)

    fun finishedWaiting(): Boolean {
        return !waiting.get()
    }

    fun run(
            before: ViewInteraction,
            after: ViewInteraction
    ) {
        waiting.set(true)
        var error = true
        var tryCount = 0

        try {
            after.check(doesNotExist())

            while (error && tryCount < 100) {
                tryCount += 1

                try {
                    before.perform(click())
                } catch (ignored: Exception) {
                }

                try {
                    after.check(matches(isDisplayed()))
                    error = false
                } catch (ignored: Exception) {
                }

                if (error) {
                    try {
                        Thread.sleep(100)
                    } catch (ignored: Exception) {
                    }

                }
            }
        } finally {
            waiting.set(false)
        }

        if (error) {
            after.check(matches(isDisplayed()))
        }
    } // run()
}

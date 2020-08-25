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

package com.instructure.espresso.actions


import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.*
import android.view.View
import com.instructure.espresso.matchers.WaitForViewMatcher.waitForView
import org.hamcrest.CoreMatchers.both
import org.hamcrest.CoreMatchers.either
import org.hamcrest.Matcher

/**
 * Espresso click randomly turns into long press. view.callOnClick enables reliable clicking
 *
 * https://issuetracker.google.com/issues/37078920
 * https://stackoverflow.com/questions/32330671/android-espresso-performs-longclick-instead-of-click
 */
class ViewCallOnClick : ViewAction {
    override fun getConstraints(): Matcher<View> {
        return isDisplayingAtLeast(90)
    }

    override fun getDescription(): String {
        return "callOnClick"
    }

    override fun perform(uiController: UiController, view: View) {
        uiController.loopMainThreadUntilIdle()
        view.callOnClick()
        uiController.loopMainThreadForAtLeast(1000)
    }

    companion object {
        /**
         * Clickable views are either themself clickable or a descendant of a clickable view.
         * This will first attempt to find the view assuming it is clickable using [matcher].
         * If unsuccessful this will attempt to find the clickable parent of the view.
         */
        fun callOnClick(matcher: Matcher<View>) {
            val target = both(isClickable()).and(either(matcher).or(hasDescendant(matcher)))
            waitForView(target).perform(ViewCallOnClick())
        }
    }
}

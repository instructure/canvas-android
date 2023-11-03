//
// Copyright (C) 2023-present Instructure, Inc.
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


import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher


/**
 * During Espresso click the coordinates calculation may have been broken by setRotation call on the view.
 * This forceClick will perform click without checking the coordinates.
 *
 */
class ForceClick : ViewAction {

    override fun getConstraints(): Matcher<View?>? {
        return allOf(isClickable(), isEnabled(), isDisplayed())
    }

    override fun getDescription(): String? {
        return "force click"
    }

    override fun perform(uiController: UiController, view: View) {
        view.performClick() // perform click without checking view coordinates.
        uiController.loopMainThreadUntilIdle()
    }

}

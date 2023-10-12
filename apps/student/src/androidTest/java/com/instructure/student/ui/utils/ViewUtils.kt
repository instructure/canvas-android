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

package com.instructure.student.ui.utils

import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import org.hamcrest.Matcher

object ViewUtils {

    fun pressBackButton(times: Int) {
        for(i in 1..times) {
            Espresso.pressBack()
        }
    }

    fun waitForViewToDisappear(viewMatcher: Matcher<View>, timeoutInSeconds: Long) {
        val startTime = System.currentTimeMillis()

        while (System.currentTimeMillis() - startTime < (timeoutInSeconds * 1000)) {
            try {
                onView(viewMatcher)
                    .check(ViewAssertions.doesNotExist())
                return
            } catch (e: AssertionError) {
                Thread.sleep(200)
            }
        }
        throw AssertionError("The view has not been displayed within $timeoutInSeconds seconds.")
    }
}

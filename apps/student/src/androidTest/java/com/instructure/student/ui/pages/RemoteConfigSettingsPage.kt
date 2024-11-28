/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.ui.pages

import android.view.View
import android.widget.EditText
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.canvas.espresso.clearFocus
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.espresso.click
import com.instructure.espresso.pages.BasePage
import com.instructure.pandautils.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class RemoteConfigSettingsPage : BasePage(R.id.remoteConfigSettingsFragment) {

    fun clickRemoteConfigParamValue(param: RemoteConfigParam) {
        val target = getParamValueMatcher(param)

        scrollRecyclerView(R.id.recyclerView, target)
        onView(target).click()
    }

    fun clearRemoteConfigParamValueFocus(param: RemoteConfigParam) {
        val target = getParamValueMatcher(param)

        scrollRecyclerView(R.id.recyclerView, target)
        onView(target).perform(clearFocus())
    }

    fun verifyRemoteConfigParamValue(param: RemoteConfigParam, targetValue: String) {
        val target = getParamValueMatcher(param)

        scrollRecyclerView(R.id.recyclerView, target)
        onView(target).check(ViewAssertions.matches((ViewMatchers.withText(targetValue))))
    }

    private fun getParamValueMatcher(param: RemoteConfigParam): Matcher<View> {
        val matcher = Matchers.allOf(
                ViewMatchers.isAssignableFrom(EditText::class.java),
                ViewMatchers.hasSibling(containsTextCaseInsensitive("${param.rc_name}:"))
        )

        return matcher
    }
}
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
package com.instructure.teacher.ui.pages.classic

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
import com.instructure.espresso.page.BasePage
import com.instructure.pandautils.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers

/**
 * Represents the Remote Config Settings page.
 *
 * This page extends the BasePage class and provides functionality for interacting with remote configuration parameters.
 * It includes methods for clicking on a remote config parameter value, clearing the focus of a remote config parameter value,
 * and verifying the value of a remote config parameter. It also includes a private helper method to get the matcher for locating
 * the remote config parameter value. The page is identified by the resource ID R.id.remoteConfigSettingsFragment.
 */
class RemoteConfigSettingsPage : BasePage(R.id.remoteConfigSettingsFragment) {

    /**
     * Clicks on the value of the specified remote config parameter.
     *
     * @param param The remote config parameter to click on.
     */
    fun clickRemoteConfigParamValue(param: RemoteConfigParam) {
        val target = getParamValueMatcher(param)

        scrollRecyclerView(R.id.recyclerView, target)
        onView(target).click()
    }

    /**
     * Clears the focus of the specified remote config parameter value.
     *
     * @param param The remote config parameter to clear the focus of.
     */
    fun clearRemoteConfigParamValueFocus(param: RemoteConfigParam) {
        val target = getParamValueMatcher(param)

        scrollRecyclerView(R.id.recyclerView, target)
        onView(target).perform(clearFocus())
    }

    /**
     * Verifies the value of the specified remote config parameter.
     *
     * @param param The remote config parameter to verify the value of.
     * @param targetValue The expected value of the remote config parameter.
     */
    fun verifyRemoteConfigParamValue(param: RemoteConfigParam, targetValue: String) {
        val target = getParamValueMatcher(param)

        scrollRecyclerView(R.id.recyclerView, target)
        onView(target).check(ViewAssertions.matches((ViewMatchers.withText(targetValue))))
    }

    /**
     * Returns the matcher for locating the value of the specified remote config parameter.
     *
     * @param param The remote config parameter to get the matcher for.
     * @return The matcher for locating the remote config parameter value.
     */
    private fun getParamValueMatcher(param: RemoteConfigParam): Matcher<View> {
        val matcher = Matchers.allOf(
            ViewMatchers.isAssignableFrom(EditText::class.java),
            ViewMatchers.hasSibling(containsTextCaseInsensitive("${param.rc_name}:"))
        )

        return matcher
    }
}
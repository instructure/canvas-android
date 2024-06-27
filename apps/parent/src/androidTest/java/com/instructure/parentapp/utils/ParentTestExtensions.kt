/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.parentapp.utils

import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.canvas.espresso.waitForMatcherWithSleeps
import com.instructure.canvasapi2.models.User
import com.instructure.parentapp.R
import com.instructure.parentapp.features.login.LoginActivity


fun ParentTest.tokenLogin(domain: String, token: String, user: User) {
    activityRule.runOnUiThread {
        (originalActivity as LoginActivity).loginWithToken(
            token,
            domain,
            user
        )
    }

    waitForMatcherWithSleeps(ViewMatchers.withId(R.id.toolbar), 20000).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
}

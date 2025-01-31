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

import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import com.instructure.canvasapi2.models.User
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertContainsText
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.withId
import com.instructure.student.R
import org.hamcrest.Matchers

class PersonDetailsPage: BasePage(R.id.clickContainer) {

    private val avatar by OnViewWithId(R.id.avatar)
    private val userName by OnViewWithId(R.id.userName)
    private val userRole by OnViewWithId(R.id.userRole)
    private val compose by OnViewWithId(R.id.compose)

    fun clickCompose() {
        compose.click()
    }

    fun assertIsPerson(user: User) {
        userName.assertContainsText(user.name)
    }

    fun assertIsPerson(userNameString: String) {
        userName.assertContainsText(userNameString)
    }

    //OfflineMethod
    fun assertComposeMessageIcon(visibility: ViewMatchers.Visibility) {
        onView(Matchers.allOf(withId(R.id.compose))).check(matches(withEffectiveVisibility(visibility)))
    }
}
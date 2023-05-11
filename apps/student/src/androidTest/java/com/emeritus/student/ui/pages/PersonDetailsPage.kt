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
package com.emeritus.student.ui.pages

import com.instructure.canvasapi2.models.User
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertContainsText
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.emeritus.student.R

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
}
/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 *
 */

package com.instructure.teacher.ui.utils

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.instructure.teacher.activities.LoginActivity
import com.instructure.teacher.ui.pages.ProgressPage
import org.junit.Rule

abstract class ComposeTest : TeacherTest() {

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<LoginActivity>()

    val progressPage = ProgressPage(composeTestRule)
}
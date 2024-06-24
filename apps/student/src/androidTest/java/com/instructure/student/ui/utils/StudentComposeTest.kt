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

package com.instructure.student.ui.utils

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.instructure.canvas.espresso.common.pages.compose.CalendarEventCreateEditPage
import com.instructure.canvas.espresso.common.pages.compose.CalendarEventDetailsPage
import com.instructure.canvas.espresso.common.pages.compose.CalendarFilterPage
import com.instructure.canvas.espresso.common.pages.compose.CalendarScreenPage
import com.instructure.canvas.espresso.common.pages.compose.CalendarToDoCreateUpdatePage
import com.instructure.canvas.espresso.common.pages.compose.CalendarToDoDetailsPage
import com.instructure.student.activity.LoginActivity
import org.junit.Rule

abstract class StudentComposeTest : StudentTest() {

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<LoginActivity>()

    val calendarEventCreateEditPage = CalendarEventCreateEditPage(composeTestRule)
    val calendarScreenPage = CalendarScreenPage(composeTestRule)
    val calendarEventDetailsPage = CalendarEventDetailsPage(composeTestRule)
    val calendarToDoCreateUpdatePage = CalendarToDoCreateUpdatePage(composeTestRule)
    val calendarToDoDetailsPage = CalendarToDoDetailsPage(composeTestRule)
    val calendarFilterPage = CalendarFilterPage(composeTestRule)
}
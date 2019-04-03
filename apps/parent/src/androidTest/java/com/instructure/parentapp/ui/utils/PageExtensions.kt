/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
 */
package com.instructure.parentapp.ui.utils

import androidx.test.espresso.action.ViewActions
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.waitForViewWithContentDescription
import com.instructure.parentapp.R

fun BasePage.openNavDrawer() { waitForViewWithContentDescription(R.string.navigation_drawer_open).perform(ViewActions.click()) }

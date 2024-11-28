/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.ui.pages

import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.click
import com.instructure.espresso.pages.BasePage
import com.instructure.teacher.R

/**
 * Represents the Not a Teacher Page.
 *
 * This page extends the BasePage class and provides functionality for interacting with the elements on the "Not a Teacher" page.
 * It contains a method to click on the login button.
 */
class NotATeacherPage : BasePage() {

    private val notATeacherTitle by WaitForViewWithId(R.id.not_a_teacher_header, autoAssert = true)
    private val explanation by WaitForViewWithId(R.id.explanation, autoAssert = true)
    private val studentLink by WaitForViewWithId(R.id.studentLink)
    private val parentLink by WaitForViewWithId(R.id.parentLink)
    private val loginButton by WaitForViewWithId(R.id.login)

    /**
     * Clicks on the login button.
     */
    fun clickOnLoginButton() {
        loginButton.click()
    }
}

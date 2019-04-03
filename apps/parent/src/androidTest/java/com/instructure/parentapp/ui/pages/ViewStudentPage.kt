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
@file:Suppress("unused")

package com.instructure.parentapp.ui.pages

import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.*
import com.instructure.parentapp.R

class ViewStudentPage : BasePage(pageResId = R.id.viewStudentPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val studentSpinner by WaitForViewWithId(R.id.action_bar_spinner)
    private val bottomBar by OnViewWithId(R.id.bottomBar)

    fun assertDisplaysStudentName(student: CanvasUserApiModel) {
        waitForView(withText(student.shortName) + withAncestor(R.id.toolbar)).assertDisplayed()
    }

    fun selectStudent(student: CanvasUserApiModel) {
        studentSpinner.click()
        waitForView(withText(student.shortName) - withAncestor(R.id.toolbar)).click()
    }

}

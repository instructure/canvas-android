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
 */
package com.instructure.student.ui.pages.classic

import com.instructure.espresso.ModuleItemInteractions
import com.instructure.espresso.OnViewWithText
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.student.R

class GoToQuizPage(val moduleItemInteractions: ModuleItemInteractions) : BasePage() {

    private val goToQuizButton by OnViewWithText(R.string.goToQuiz)

    fun clickGoToQuizButton() {
        goToQuizButton.click()
    }

    fun assertQuizTitle(expectedTitle: String) {
        onView(withId(R.id.quizTitle) + withText(expectedTitle) + withAncestor(R.id.quizInfoContainer)).assertDisplayed()
    }
}

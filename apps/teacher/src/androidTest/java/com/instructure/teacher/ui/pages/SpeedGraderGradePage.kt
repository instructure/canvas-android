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

import com.instructure.espresso.*
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.getStringFromResource
import com.instructure.espresso.page.onViewWithId
import com.instructure.teacher.R

class SpeedGraderGradePage : BasePage() {

    private val gradeContainer by OnViewWithId(R.id.gradeContainer)
    private val gradeTextContainer by OnViewWithId(R.id.gradeTextContainer)
    private val gradingField by OnViewWithText(R.string.grade)

    private val addGradeIcon by WaitForViewWithId(R.id.addGradeIcon)
    private val gradeValueText by WaitForViewWithId(R.id.gradeValueText)

    //dialog views
    private val gradeEditText by WaitForViewWithId(R.id.gradeEditText)
    private val customizeGradeTitle by WaitForViewWithText(R.string.customize_grade)
    private val excuseStudentCheckbox by WaitForViewWithId(R.id.excuseStudentCheckbox)
    private val confirmDialogButton by WaitForViewWithStringText(getStringFromResource(android.R.string.ok).toUpperCase())


    fun openGradeDialog() {
        gradeTextContainer.click()
    }

    fun enterNewGrade(grade: String) {
        gradeEditText.replaceText(grade)
        confirmDialogButton.click()
    }

    fun assertGradeDialog() {
        customizeGradeTitle.assertDisplayed()
        excuseStudentCheckbox.assertDisplayed()
    }

    fun assertHasGrade(grade: String) {
        gradeValueText.assertContainsText(grade)
    }

    fun assertRubricHidden() {
        onViewWithId(R.id.rubricEditView).assertGone()
    }

}

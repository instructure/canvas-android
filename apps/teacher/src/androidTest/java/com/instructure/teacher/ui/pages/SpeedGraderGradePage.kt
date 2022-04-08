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

import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.teacher.R
import org.hamcrest.Matchers
import org.hamcrest.Matchers.not
import java.text.DecimalFormat
import java.util.Locale

class SpeedGraderGradePage : BasePage() {

    private val gradeContainer by OnViewWithId(R.id.gradeContainer)
    private val gradeTextContainer by OnViewWithId(R.id.gradeTextContainer)
    private val gradingField by OnViewWithText(R.string.grade)

    private val addGradeIcon by WaitForViewWithId(R.id.addGradeIcon)
    private val gradeValueText by WaitForViewWithId(R.id.gradeValueText)

    private val slider by OnViewWithId(R.id.speedGraderSlider)

    //dialog views
    private val gradeEditText by WaitForViewWithId(R.id.gradeEditText)
    private val customizeGradeTitle by WaitForViewWithText(R.string.customize_grade)
    private val confirmDialogButton by WaitForViewWithStringText(getStringFromResource(android.R.string.ok).uppercase(Locale.getDefault()))


    fun openGradeDialog() {
        onView(Matchers.allOf((withId(R.id.gradeTextContainer)), ViewMatchers.isDisplayed())).click()
    }

    fun enterNewGrade(grade: String) {
        gradeEditText.replaceText(grade)
        confirmDialogButton.click()
    }

    fun assertGradeDialog() {
        customizeGradeTitle.assertDisplayed()
    }

    fun assertHasGrade(grade: String) {
        onView(Matchers.allOf((withId(R.id.gradeValueText)), ViewMatchers.isDisplayed())).assertContainsText(grade)
    }

    fun assertRubricHidden() {
        onViewWithId(R.id.rubricEditView).assertGone()
    }

    fun assertRubricVisible() {
        onViewWithId(R.id.rubricEditView).assertVisible()
    }

    fun assertSliderVisible() {
        slider.assertVisible()
    }

    fun assertSliderHidden() {
        slider.assertGone()
    }

    fun assertCheckboxVisible() {
        onViewWithId(R.id.excuseStudentCheckbox).assertVisible()
    }

    fun assertCheckboxHidden() {
        onViewWithId(R.id.excuseStudentCheckbox).assertGone()
    }

    fun assertSliderMaxValue(value: String) {
        onView(Matchers.allOf((withId(R.id.maxGrade)), ViewMatchers.isDisplayed())).assertContainsText(value)
    }

    fun assertSliderMinValue(value: String) {
        onView(Matchers.allOf((withId(R.id.minGrade)), ViewMatchers.isDisplayed())).assertContainsText(value)
    }

    fun assertHasOvergradeWarning(overgradedBy: Double) {
        val numberFormatter = DecimalFormat("##.##")
        onView(Matchers.allOf((withId(R.id.gradeText)), ViewMatchers.isDisplayed())).assertHasText(getStringFromResource(R.string.speed_grader_overgraded_by, numberFormatter.format(overgradedBy)))
    }

    fun clickExcuseStudentButton() {
        onViewWithId(R.id.excuseButton).click()
    }

    fun assertStudentExcused() {
        waitForView(Matchers.allOf((withId(R.id.gradeValueText)), ViewMatchers.isDisplayed())).assertHasText(getStringFromResource(R.string.excused))
    }

    fun assertExcuseButtonEnabled() {
        onViewWithId(R.id.excuseButton).check(matches(isEnabled()))
    }

    fun assertExcuseButtonDisabled() {
        onViewWithId(R.id.excuseButton).check(matches(not(isEnabled())))
    }

    fun assertNoGradeButtonEnabled() {
        onViewWithId(R.id.noGradeButton).check(matches(isEnabled()))
    }

    fun assertNoGradeButtonDisabled() {
        onViewWithId(R.id.noGradeButton).check(matches(not(isEnabled())))
    }

    fun clickNoGradeButton() {
        onViewWithId(R.id.noGradeButton).click()
    }

    fun assertHasNoGrade() {
        onViewWithId(R.id.gradeValueText).assertGone()
        onViewWithId(R.id.addGradeIcon).assertVisible()
    }

}

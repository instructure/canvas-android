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
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.OnViewWithText
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.WaitForViewWithStringText
import com.instructure.espresso.WaitForViewWithText
import com.instructure.espresso.assertContainsText
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertGone
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertVisible
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.getStringFromResource
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.onViewWithId
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.withId
import com.instructure.espresso.replaceText
import com.instructure.teacher.R
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.not
import java.text.DecimalFormat
import java.util.*

/**
 * Represents the SpeedGrader grade page.
 *
 * This page provides functionality for interacting with the elements on the SpeedGrader grade page. It contains methods
 * for opening the grade dialog, entering a new grade, asserting the grade dialog, asserting the presence of a grade,
 * asserting the visibility of the rubric, slider, and checkbox, asserting the values of the slider's max and min values,
 * asserting the presence of an overgrade warning, clicking the excuse student button, asserting the student's excused
 * status, and asserting the enabled or disabled state of the excuse and no grade buttons. This page extends the BasePage
 * class.
 */
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


    /**
     * Opens the grade dialog.
     */
    fun openGradeDialog() {
        onView(CoreMatchers.allOf((withId(R.id.gradeTextContainer)), ViewMatchers.isDisplayed())).click()
    }

    /**
     * Enters a new grade into the grade dialog.
     *
     * @param grade The new grade to be entered.
     */
    fun enterNewGrade(grade: String) {
        gradeEditText.replaceText(grade)
        confirmDialogButton.click()
    }

    /**
     * Asserts the presence of the grade dialog.
     */
    fun assertGradeDialog() {
        customizeGradeTitle.assertDisplayed()
    }

    /**
     * Asserts the presence of a grade.
     *
     * @param grade The expected grade.
     */
    fun assertHasGrade(grade: String) {
        onView(CoreMatchers.allOf((withId(R.id.gradeValueText)), ViewMatchers.isDisplayed())).assertContainsText(grade)
    }

    /**
     * Asserts that the rubric is hidden.
     */
    fun assertRubricHidden() {
        onViewWithId(R.id.rubricEditView).assertGone()
    }

    /**
     * Asserts that the rubric is visible.
     */
    fun assertRubricVisible() {
        onViewWithId(R.id.rubricEditView).assertVisible()
    }

    /**
     * Asserts that the slider is visible.
     */
    fun assertSliderVisible() {
        slider.assertVisible()
    }

    /**
     * Asserts that the slider is hidden.
     */
    fun assertSliderHidden() {
        slider.assertGone()
    }

    /**
     * Asserts that the checkbox is visible.
     */
    fun assertCheckboxVisible() {
        onViewWithId(R.id.excuseStudentCheckbox).assertVisible()
    }

    /**
     * Asserts that the checkbox is hidden.
     */
    fun assertCheckboxHidden() {
        onViewWithId(R.id.excuseStudentCheckbox).assertGone()
    }

    /**
     * Asserts the max value of the slider.
     *
     * @param value The expected max value.
     */
    fun assertSliderMaxValue(value: String) {
        onView(CoreMatchers.allOf((withId(R.id.maxGrade)), ViewMatchers.isDisplayed())).assertContainsText(value)
    }

    /**
     * Asserts the min value of the slider.
     *
     * @param value The expected min value.
     */
    fun assertSliderMinValue(value: String) {
        onView(CoreMatchers.allOf((withId(R.id.minGrade)), ViewMatchers.isDisplayed())).assertContainsText(value)
    }

    /**
     * Asserts the presence of an overgrade warning with the specified overgraded value.
     *
     * @param overgradedBy The overgraded value.
     */
    fun assertHasOvergradeWarning(overgradedBy: Double) {
        val numberFormatter = DecimalFormat("##.##")
        onView(CoreMatchers.allOf((withId(R.id.gradeText)), ViewMatchers.isDisplayed())).assertHasText(getStringFromResource(R.string.speed_grader_overgraded_by, numberFormatter.format(overgradedBy)))
    }

    /**
     * Clicks the excuse student button.
     */
    fun clickExcuseStudentButton() {
        onViewWithId(R.id.excuseButton).click()
    }

    /**
     * Asserts that the student is excused.
     */
    fun assertStudentExcused() {
        waitForView(CoreMatchers.allOf((withId(R.id.gradeValueText)), ViewMatchers.isDisplayed())).assertHasText(getStringFromResource(R.string.excused))
    }

    /**
     * Asserts that the excuse button is enabled.
     */
    fun assertExcuseButtonEnabled() {
        onViewWithId(R.id.excuseButton).check(matches(isEnabled()))
    }

    /**
     * Asserts that the excuse button is disabled.
     */
    fun assertExcuseButtonDisabled() {
        onViewWithId(R.id.excuseButton).check(matches(not(isEnabled())))
    }

    /**
     * Asserts that the no grade button is enabled.
     */
    fun assertNoGradeButtonEnabled() {
        onViewWithId(R.id.noGradeButton).check(matches(isEnabled()))
    }

    /**
     * Asserts that the no grade button is disabled.
     */
    fun assertNoGradeButtonDisabled() {
        onViewWithId(R.id.noGradeButton).check(matches(not(isEnabled())))
    }

    /**
     * Clicks the no grade button.
     */
    fun clickNoGradeButton() {
        onViewWithId(R.id.noGradeButton).click()
    }

    /**
     * Asserts the absence of a grade.
     */
    fun assertHasNoGrade() {
        onViewWithId(R.id.gradeValueText).assertGone()
        onViewWithId(R.id.addGradeIcon).assertVisible()
    }

}

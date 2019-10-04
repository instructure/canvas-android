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
 */
package com.instructure.student.ui.renderTests.views

import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.RootMatchers.hasWindowLayoutParams
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertCompletelyDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onViewWithId
import com.instructure.student.R
import com.instructure.student.espresso.StudentRenderTest
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui.SubmissionRubricTooltipHandler
import com.instructure.student.ui.utils.assertCompletelyAbove
import com.instructure.student.ui.utils.assertCompletelyBelow
import com.instructure.student.ui.utils.assertLineCount
import kotlinx.android.synthetic.qa.test_rubric_tooltip.view.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SubmissionRubricTooltipTest : StudentRenderTest() {

    private val page = RubricTooltipTestPage()
    private val testText = "Testing 1234 Testing 123"

    @Test
    fun displaysTooltipAbove() {
        val container = setupTestLayout()
        SubmissionRubricTooltipHandler.setTooltipText(container.buttonCenter, testText)
        page.buttonCenter.click()
        page.tooltipText.assertCompletelyDisplayed()
        page.tooltipText.assertCompletelyAbove(page.buttonCenter)
    }

    @Test
    fun displaysTooltipBelow() {
        val container = setupTestLayout()
        SubmissionRubricTooltipHandler.setTooltipText(container.buttonTopCenter, testText)
        page.buttonTopCenter.click()
        page.tooltipText.assertCompletelyDisplayed()
        page.tooltipText.assertCompletelyBelow(page.buttonTopCenter)
    }

    @Test
    fun displaysTooltipStartBelow() {
        val container = setupTestLayout()
        SubmissionRubricTooltipHandler.setTooltipText(container.buttonTopLeft, testText)
        page.buttonTopLeft.click()
        page.tooltipText.assertCompletelyDisplayed()
        page.tooltipText.assertCompletelyBelow(page.buttonTopLeft)
    }

    @Test
    fun displaysTooltipStartAbove() {
        val container = setupTestLayout()
        SubmissionRubricTooltipHandler.setTooltipText(container.buttonBottomLeft, testText)
        page.buttonBottomLeft.click()
        page.tooltipText.assertCompletelyDisplayed()
        page.tooltipText.assertCompletelyAbove(page.buttonBottomLeft)
    }

    @Test
    fun displaysTooltipEndBelow() {
        val container = setupTestLayout()
        SubmissionRubricTooltipHandler.setTooltipText(container.buttonTopRight, testText)
        page.buttonTopRight.click()
        page.tooltipText.assertCompletelyDisplayed()
        page.tooltipText.assertCompletelyBelow(page.buttonTopRight)
    }

    @Test
    fun displaysTooltipEndAbove() {
        val container = setupTestLayout()
        SubmissionRubricTooltipHandler.setTooltipText(container.buttonBottomRight, testText)
        page.buttonBottomRight.click()
        page.tooltipText.assertCompletelyDisplayed()
        page.tooltipText.assertCompletelyAbove(page.buttonBottomRight)
    }

    @Test
    fun hidesTooltipAfterDuration() {
        val container = setupTestLayout()
        val duration = 1500L
        SubmissionRubricTooltipHandler.setTooltipText(container.buttonCenter, testText, duration)
        page.buttonCenter.click()
        page.tooltipText.assertCompletelyDisplayed()
        Thread.sleep(duration)
        page.tooltipText.check(doesNotExist())
    }

    @Test
    fun limitsLineCountToThree() {
        val container = setupTestLayout()
        SubmissionRubricTooltipHandler.setTooltipText(container.buttonCenter, "Testing 123 ".repeat(100))
        page.buttonCenter.click()
        page.tooltipText.assertCompletelyDisplayed()
        page.tooltipText.assertLineCount(3)
    }

    @Test
    fun changesAnchors() {
        val container = setupTestLayout()
        SubmissionRubricTooltipHandler.setTooltipText(container.buttonCenter, testText)
        SubmissionRubricTooltipHandler.setTooltipText(container.buttonBottomRight, testText)

        page.buttonCenter.click()
        page.tooltipText.assertCompletelyDisplayed()
        page.tooltipText.assertCompletelyAbove(page.buttonCenter)

        page.buttonBottomRight.click()
        page.tooltipText.assertCompletelyDisplayed()
        page.tooltipText.assertCompletelyAbove(page.buttonBottomRight)
        page.tooltipText.assertCompletelyBelow(page.buttonCenter)
    }

    @Test
    fun hidesOnOutsideClick() {
        val container = setupTestLayout()
        SubmissionRubricTooltipHandler.setTooltipText(container.buttonCenter, testText)

        page.buttonCenter.click()
        page.tooltipText.assertCompletelyDisplayed()

        // This button has not been set up, so clicking it should only dismiss the existing tooltip
        page.buttonBottomRight.click()
        page.tooltipText.check(doesNotExist())
    }

    private fun setupTestLayout(): View {
        var complete = false
        activityRule.runOnUiThread {
            activityRule.activity.loadLayout(R.layout.test_rubric_tooltip)
            complete = true
        }
        while (!complete) {
        }
        return activityRule.activity.findViewById(R.id.container)
    }

}

class RubricTooltipTestPage : BasePage() {
    val buttonCenter by OnViewWithId(R.id.buttonCenter)
    val buttonTopLeft by OnViewWithId(R.id.buttonTopLeft)
    val buttonTopRight by OnViewWithId(R.id.buttonTopRight)
    val buttonTopCenter by OnViewWithId(R.id.buttonTopCenter)
    val buttonBottomLeft by OnViewWithId(R.id.buttonBottomLeft)
    val buttonBottomRight by OnViewWithId(R.id.buttonBottomRight)
    val tooltipText: ViewInteraction
        get() = onViewWithId(R.id.tooltipTextView).inRoot(hasWindowLayoutParams())
}

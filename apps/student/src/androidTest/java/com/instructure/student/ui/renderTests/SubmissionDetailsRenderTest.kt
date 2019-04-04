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
package com.instructure.student.ui.renderTests

import android.content.pm.ActivityInfo
import androidx.test.espresso.action.GeneralLocation
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.espresso.assertGone
import com.instructure.espresso.assertVisible
import com.instructure.espresso.click
import com.instructure.student.espresso.StudentRenderTest
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsModel
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsFragment
import com.spotify.mobius.runners.WorkRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SubmissionDetailsRenderTest : StudentRenderTest() {

    private lateinit var baseModel: SubmissionDetailsModel

    @Before
    fun setup() {
        baseModel = SubmissionDetailsModel(
            assignmentId = 0,
            isLoading = false,
            canvasContext = Course(name = "Test Course"),
            assignment = DataResult.Fail(),
            rootSubmission = DataResult.Fail()
        )
    }

    @Test
    fun displaysToolbarTitle() {
        loadPageWithModel(baseModel)
        submissionDetailsRenderPage.assertDisplaysToolbarTitle("Submission")
    }

    @Test
    fun displaysErrorState() {
        loadPageWithModel(baseModel)
        submissionDetailsRenderPage.assertDisplaysError()
    }

    @Test
    fun displaysLoadingState() {
        loadPageWithModel(baseModel.copy(isLoading = true))
        submissionDetailsRenderPage.assertDisplaysLoadingView()
    }

    @Test
    fun displaysLoadedState() {
        loadPageWithModel(
            baseModel.copy(
                selectedSubmissionAttempt = 1,
                assignment = DataResult.Success(Assignment()),
                rootSubmission = DataResult.Success(
                    Submission(submissionHistory = listOf(Submission(attempt = 1)))
                )
            )
        )
        submissionDetailsRenderPage.assertDisplaysContent()
    }

    @Test
    fun hidesVersionSpinnerForSingleSubmission() {
        loadPageWithModel(
            baseModel.copy(
                selectedSubmissionAttempt = 1,
                assignment = DataResult.Success(Assignment()),
                rootSubmission = DataResult.Success(
                    Submission(submissionHistory = listOf(Submission(attempt = 1)))
                )
            )
        )
        submissionDetailsRenderPage.versionSpinner.assertGone()
    }

    @Test
    fun showsVersionSpinnerForMultipleSubmission() {
        val firstSubmission = Submission(attempt = 1)
        val secondSubmission = Submission(attempt = 2)
        loadPageWithModel(
            baseModel.copy(
                selectedSubmissionAttempt = 1,
                assignment = DataResult.Success(Assignment()),
                rootSubmission = DataResult.Success(
                    Submission(submissionHistory = listOf(firstSubmission, secondSubmission))
                )
            )
        )
        submissionDetailsRenderPage.versionSpinner.assertVisible()
    }

    @Test
    fun spinnerShowsCorrectSelectedSubmission() {
        val firstSubmission = Submission(
            attempt = 1,
            submittedAt = DateHelper.makeDate(2050, 0, 30, 23, 59, 0)
        )
        val secondSubmission = Submission(
            attempt = 2,
            submittedAt = DateHelper.makeDate(2050, 0, 31, 23, 59, 0)
        )
        loadPageWithModel(
            baseModel.copy(
                selectedSubmissionAttempt = 2,
                assignment = DataResult.Success(Assignment()),
                rootSubmission = DataResult.Success(
                    Submission(submissionHistory = listOf(firstSubmission, secondSubmission))
                )
            )
        )
        submissionDetailsRenderPage.assertSpinnerMatchesText("Jan 31 at 11:59 PM")
    }

    @Test
    fun clickingSpinnerShowsSubmissionVersions() {
        val firstSubmission = Submission(
            attempt = 1,
            submittedAt = DateHelper.makeDate(2050, 0, 30, 23, 59, 0)
        )
        val secondSubmission = Submission(
            attempt = 2,
            submittedAt = DateHelper.makeDate(2050, 0, 31, 23, 59, 0)
        )
        loadPageWithModel(
            baseModel.copy(
                selectedSubmissionAttempt = 2,
                assignment = DataResult.Success(Assignment()),
                rootSubmission = DataResult.Success(
                    Submission(submissionHistory = listOf(firstSubmission, secondSubmission))
                )
            )
        )
        submissionDetailsRenderPage.versionSpinner.click()
        submissionDetailsRenderPage.assertSpinnerDropdownItemHasText(0, "Jan 31 at 11:59 PM")
        submissionDetailsRenderPage.assertSpinnerDropdownItemHasText(1, "Jan 30 at 11:59 PM")
    }

    @Test
    fun tappingSelectedTabOpensDrawer() {
        loadPageWithModel(
            baseModel.copy(
                selectedSubmissionAttempt = 1,
                assignment = DataResult.Success(Assignment()),
                rootSubmission = DataResult.Success(
                    Submission(submissionHistory = listOf(Submission(attempt = 1)))
                )
            )
        )
        submissionDetailsRenderPage.clickTab("COMMENTS")
        submissionDetailsRenderPage.assertDisplaysDrawerContent()
    }

    @Test
    fun tappingUnselectedTabOpensDrawer() {
        loadPageWithModel(
            baseModel.copy(
                selectedSubmissionAttempt = 1,
                assignment = DataResult.Success(Assignment()),
                rootSubmission = DataResult.Success(
                    Submission(submissionHistory = listOf(Submission(attempt = 1)))
                )
            )
        )
        submissionDetailsRenderPage.clickTab("FILES")
        submissionDetailsRenderPage.assertDisplaysDrawerContent()
    }

    @Test
    fun swipingOnTabLayoutOpensDrawer() {
        loadPageWithModel(
            baseModel.copy(
                selectedSubmissionAttempt = 1,
                assignment = DataResult.Success(Assignment()),
                rootSubmission = DataResult.Success(
                    Submission(submissionHistory = listOf(Submission(attempt = 1)))
                )
            )
        )
        submissionDetailsRenderPage.swipeDrawerTo(GeneralLocation.CENTER)
        submissionDetailsRenderPage.assertDisplaysDrawerContent()
    }

    @Test
    fun swipingGauntlet() {
        loadPageWithModel(
            baseModel.copy(
                selectedSubmissionAttempt = 1,
                assignment = DataResult.Success(Assignment()),
                rootSubmission = DataResult.Success(
                    Submission(submissionHistory = listOf(Submission(attempt = 1)))
                )
            )
        )
        submissionDetailsRenderPage.swipeDrawerTo(GeneralLocation.CENTER)
        submissionDetailsRenderPage.swipeDrawerTo(GeneralLocation.TOP_CENTER)
        submissionDetailsRenderPage.swipeDrawerTo(GeneralLocation.CENTER)
        submissionDetailsRenderPage.swipeDrawerTo(GeneralLocation.BOTTOM_CENTER)
        submissionDetailsRenderPage.swipeDrawerTo(GeneralLocation.TOP_CENTER)
        submissionDetailsRenderPage.swipeDrawerTo(GeneralLocation.BOTTOM_CENTER)
        submissionDetailsRenderPage.swipeDrawerTo(GeneralLocation.CENTER)
        submissionDetailsRenderPage.assertDisplaysDrawerContent()
    }

    @Test
    fun updatesDrawerHeightOnOrientationChangeToLandscape() {
        loadPageWithModel(
            baseModel.copy(
                selectedSubmissionAttempt = 1,
                assignment = DataResult.Success(Assignment()),
                rootSubmission = DataResult.Success(
                    Submission(submissionHistory = listOf(Submission(attempt = 1)))
                )
            )
        )
        submissionDetailsRenderPage.swipeDrawerTo(GeneralLocation.CENTER)
        activityRule.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        submissionDetailsRenderPage.assertDisplaysDrawerContent()
    }

    @Test
    fun updatesDrawerHeightOnOrientationChangeToPortrait() {
        loadPageWithModel(
            baseModel.copy(
                selectedSubmissionAttempt = 1,
                assignment = DataResult.Success(Assignment()),
                rootSubmission = DataResult.Success(
                    Submission(submissionHistory = listOf(Submission(attempt = 1)))
                )
            )
        )
        submissionDetailsRenderPage.swipeDrawerTo(GeneralLocation.CENTER)
        activityRule.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        submissionDetailsRenderPage.assertDisplaysDrawerContent()
    }

    private fun loadPageWithModel(model: SubmissionDetailsModel) {
        val emptyEffectRunner = object : WorkRunner {
            override fun dispose() = Unit
            override fun post(runnable: Runnable) = Unit
        }
        val route = SubmissionDetailsFragment.makeRoute(model.canvasContext, model.assignmentId)
        val fragment = SubmissionDetailsFragment.newInstance(route)!!.apply {
            overrideInitModel = model
            loopMod = { it.effectRunner { emptyEffectRunner } }
        }
        activityRule.activity.loadFragment(fragment)
    }

}

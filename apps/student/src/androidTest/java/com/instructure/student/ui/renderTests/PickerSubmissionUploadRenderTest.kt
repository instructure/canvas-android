/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.ui.renderTests

import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.espresso.*
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.makeBundle
import com.instructure.student.R
import com.instructure.student.espresso.StudentRenderTest
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerListItemViewState
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerSubmissionUploadViewState
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerVisibilities
import com.instructure.student.ui.pages.renderPages.PickerSubmissionUploadRenderPage
import com.spotify.mobius.runners.WorkRunner
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PickerSubmissionUploadRenderTest : StudentRenderTest() {

    private val page = PickerSubmissionUploadRenderPage()
    private val baseVisibilities = PickerVisibilities(fab = true)

    @Test
    @TestMetaData(Priority.P2, FeatureCategory.SUBMISSIONS, TestCategory.RENDER, false)
    fun displaysEmptyState() {
        loadPageWithViewState(PickerSubmissionUploadViewState.Empty(baseVisibilities))
        page.emptyView.assertVisible()
        page.fabPick.assertVisible()

        page.recycler.assertNotDisplayed()
        page.submitButton.check(doesNotExist())
        assertExtraFabsNotDisplayed()
    }

    @Test
    @TestMetaData(Priority.P2, FeatureCategory.SUBMISSIONS, TestCategory.RENDER, false)
    fun displaysListState() {
        val fileItemStates = listOf(
            PickerListItemViewState(0, R.drawable.vd_media_recordings, "title", "12.3 KB")
        )
        loadPageWithViewState(
            PickerSubmissionUploadViewState.FileList(
                baseVisibilities.copy(submit = true),
                fileItemStates
            )
        )
        page.recycler.assertVisible()
        page.fabPick.assertVisible()
        page.submitButton.assertVisible()

        page.emptyView.assertNotDisplayed()
        assertExtraFabsNotDisplayed()
    }

    @Test
    @TestMetaData(Priority.P2, FeatureCategory.SUBMISSIONS, TestCategory.RENDER, false)
    fun displaysFabsAndHandlesClicks() {
        loadPageWithViewState(
            PickerSubmissionUploadViewState.Empty(
                baseVisibilities.copy(
                    fabCamera = true,
                    fabGallery = true,
                    fabFile = true
                )
            )
        )
        page.fabPick.assertVisible()
        assertExtraFabsNotDisplayed()

        // Perform click and assert displayed
        page.fabPick.click()
        assertExtraFabsDisplayed()

        // Test file click closes fab
        page.fabFile.click()
        assertExtraFabsNotDisplayed()

        // Perform click and assert displayed
        page.fabPick.click()
        assertExtraFabsDisplayed()

        // Test gallery click closes fab
        page.fabGallery.click()
        assertExtraFabsNotDisplayed()

        // Perform click and assert displayed
        page.fabPick.click()
        assertExtraFabsDisplayed()

        // Test camera click closes fab
        page.fabCamera.click()
        assertExtraFabsNotDisplayed()
    }

    private fun assertExtraFabsDisplayed() {
        page.fabFile.assertDisplayed()
        page.fabCamera.assertDisplayed()
        page.fabGallery.assertDisplayed()
    }

    private fun assertExtraFabsNotDisplayed() {
        page.fabFile.assertNotDisplayed()
        page.fabCamera.assertNotDisplayed()
        page.fabGallery.assertNotDisplayed()
    }

    private fun loadPageWithViewState(viewState: PickerSubmissionUploadViewState): PickerSubmissionUploadFragment {
        val course = Course()
        val assignment = Assignment()

        val emptyEffectRunner = object : WorkRunner {
            override fun dispose() = Unit
            override fun post(runnable: Runnable) = Unit
        }
        val fragment = PickerSubmissionUploadFragment().apply {
            overrideInitViewState = viewState
            loopMod = { it.effectRunner { emptyEffectRunner } }
            arguments = course.makeBundle {
                putParcelable(Const.ASSIGNMENT, assignment)
                putBoolean(Const.IS_MEDIA_TYPE, false)
            }
        }
        activityRule.activity.loadFragment(fragment)
        return fragment
    }
}

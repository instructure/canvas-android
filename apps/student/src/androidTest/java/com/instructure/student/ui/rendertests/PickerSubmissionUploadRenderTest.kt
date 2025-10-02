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
package com.instructure.student.ui.rendertests

import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.assertVisible
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionMode
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerListItemViewState
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerSubmissionUploadViewState
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerVisibilities
import com.instructure.student.ui.rendertests.renderpages.PickerSubmissionUploadRenderPage
import com.instructure.student.ui.utils.StudentRenderTest
import com.spotify.mobius.runners.WorkRunner
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PickerSubmissionUploadRenderTest : StudentRenderTest() {

    private val page = PickerSubmissionUploadRenderPage()
    private val baseVisibilities = PickerVisibilities(sources = true)

    @Test
    @TestMetaData(Priority.NICE_TO_HAVE, FeatureCategory.SUBMISSIONS, TestCategory.RENDER)
    fun displaysEmptyState() {
        loadPageWithViewState(PickerSubmissionUploadViewState.Empty(baseVisibilities))
        page.emptyView.assertVisible()
        page.sourcesContainer.assertVisible()

        page.recycler.assertNotDisplayed()
        page.submitButton.check(doesNotExist())
        assertSourceButtonsNotDisplayed()
    }

    @Test
    @TestMetaData(Priority.NICE_TO_HAVE, FeatureCategory.SUBMISSIONS, TestCategory.RENDER)
    fun displaysEmptyStateWithLoading() {
        loadPageWithViewState(PickerSubmissionUploadViewState.Empty(baseVisibilities.copy(loading = true)))
        page.emptyView.assertVisible()
        page.sourcesContainer.assertVisible()
        page.loading.assertVisible()

        page.recycler.assertNotDisplayed()
        page.submitButton.check(doesNotExist())
        assertSourceButtonsNotDisplayed()
    }

    @Test
    @TestMetaData(Priority.NICE_TO_HAVE, FeatureCategory.SUBMISSIONS, TestCategory.RENDER)
    fun displaysListState() {
        val fileItemStates = listOf(
            PickerListItemViewState(0, R.drawable.ic_media_recordings, "title", "12.3 KB")
        )
        loadPageWithViewState(
            PickerSubmissionUploadViewState.FileList(
                baseVisibilities.copy(submit = true),
                fileItemStates
            )
        )
        page.recycler.assertVisible()
        page.sourcesContainer.assertVisible()
        page.submitButton.assertVisible()

        page.emptyView.assertNotDisplayed()
        assertSourceButtonsNotDisplayed()
    }

    @Test
    @TestMetaData(Priority.NICE_TO_HAVE, FeatureCategory.SUBMISSIONS, TestCategory.RENDER)
    fun displaysListStateWithLoading() {
        val fileItemStates = listOf(
            PickerListItemViewState(0, R.drawable.ic_media_recordings, "title", "12.3 KB")
        )
        loadPageWithViewState(
            PickerSubmissionUploadViewState.FileList(
                baseVisibilities.copy(submit = true, loading = true),
                fileItemStates
            )
        )
        page.recycler.assertVisible()
        page.sourcesContainer.assertVisible()
        page.submitButton.assertVisible()
        page.loading.assertVisible()

        page.emptyView.assertNotDisplayed()
        assertSourceButtonsNotDisplayed()
    }

    @Test
    @TestMetaData(Priority.NICE_TO_HAVE, FeatureCategory.SUBMISSIONS, TestCategory.RENDER)
    fun displaysSourceButtons() {
        loadPageWithViewState(
            PickerSubmissionUploadViewState.Empty(
                baseVisibilities.copy(
                    sourceCamera = true,
                    sourceGallery = true,
                    sourceFile = true
                )
            )
        )
        page.sourcesContainer.assertVisible()
        page.sourcesDivider.assertDisplayed()
        page.cameraButton.assertDisplayed()
        page.deviceButton.assertDisplayed()
        page.galleryButton.assertDisplayed()
    }

    @Test
    @TestMetaData(Priority.NICE_TO_HAVE, FeatureCategory.SUBMISSIONS, TestCategory.RENDER)
    fun showsOnlyCameraSource() {
        loadPageWithViewState(
            PickerSubmissionUploadViewState.Empty(
                baseVisibilities.copy(
                    sourceCamera = true,
                    sourceGallery = false,
                    sourceFile = false
                )
            )
        )
        page.sourcesContainer.assertVisible()

        // Assert displayed
        page.cameraButton.assertDisplayed()

        page.deviceButton.assertNotDisplayed()
        page.galleryButton.assertNotDisplayed()
    }

    @Test
    @TestMetaData(Priority.NICE_TO_HAVE, FeatureCategory.SUBMISSIONS, TestCategory.RENDER)
    fun showsOnlyGallerySource() {
        loadPageWithViewState(
            PickerSubmissionUploadViewState.Empty(
                baseVisibilities.copy(
                    sourceCamera = false,
                    sourceGallery = true,
                    sourceFile = false
                )
            )
        )
        page.sourcesContainer.assertVisible()

        // PAssert displayed
        page.galleryButton.assertDisplayed()

        page.deviceButton.assertNotDisplayed()
        page.cameraButton.assertNotDisplayed()

    }

    @Test
    @TestMetaData(Priority.NICE_TO_HAVE, FeatureCategory.SUBMISSIONS, TestCategory.RENDER)
    fun showsOnlyFileSource() {
        loadPageWithViewState(
            PickerSubmissionUploadViewState.Empty(
                baseVisibilities.copy(
                    sourceCamera = false,
                    sourceGallery = false,
                    sourceFile = true
                )
            )
        )
        page.sourcesContainer.assertVisible()

        // Assert displayed
        page.deviceButton.assertDisplayed()

        page.cameraButton.assertNotDisplayed()
        page.galleryButton.assertNotDisplayed()
    }

    @Test
    @TestMetaData(Priority.NICE_TO_HAVE, FeatureCategory.SUBMISSIONS, TestCategory.RENDER)
    fun displaysCorrectStringsForSubmissionMode() {
        loadPageWithViewState(
            PickerSubmissionUploadViewState.Empty(baseVisibilities)
        )
        page.assertHasTitle(R.string.submission)
        page.emptyMessage.assertHasText(R.string.chooseFileSubtext)
    }

    @Test
    @TestMetaData(Priority.NICE_TO_HAVE, FeatureCategory.SUBMISSIONS, TestCategory.RENDER)
    fun displaysCorrectStringsForCommentMode() {
        loadPageWithViewState(
            viewState = PickerSubmissionUploadViewState.Empty(baseVisibilities),
            mode = PickerSubmissionMode.CommentAttachment
        )
        page.assertHasTitle(R.string.commentUpload)
        page.emptyMessage.assertHasText(R.string.chooseFileForCommentSubtext)
    }

    @Test
    @TestMetaData(Priority.NICE_TO_HAVE, FeatureCategory.SUBMISSIONS, TestCategory.RENDER)
    fun hidesSourceButtons() {
        loadPageWithViewState(
            PickerSubmissionUploadViewState.Empty(
                baseVisibilities.copy(sources = false)
            )
        )
        page.sourcesContainer.assertNotDisplayed()
        page.sourcesDivider.assertNotDisplayed()
        page.cameraButton.assertNotDisplayed()
        page.deviceButton.assertNotDisplayed()
        page.galleryButton.assertNotDisplayed()
    }

    private fun assertSourceButtonsNotDisplayed() {
        page.deviceButton.assertNotDisplayed()
        page.cameraButton.assertNotDisplayed()
        page.galleryButton.assertNotDisplayed()
    }

    private fun loadPageWithViewState(
        viewState: PickerSubmissionUploadViewState,
        mode: PickerSubmissionMode = PickerSubmissionMode.FileSubmission
    ): PickerSubmissionUploadFragment {
        val course = Course()
        val assignment = Assignment()

        val emptyEffectRunner = object : WorkRunner {
            override fun dispose() = Unit
            override fun post(runnable: Runnable) = Unit
        }
        val fragment = PickerSubmissionUploadFragment().apply {
            overrideInitViewState = viewState
            loopMod = { it.effectRunner { emptyEffectRunner } }
            arguments = PickerSubmissionUploadFragment.makeRoute(course, assignment, mode).arguments
        }
        activityRule.activity.loadFragment(fragment)
        return fragment
    }
}

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
 *
 */
package com.instructure.student.ui.rendertests

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Course
import com.instructure.espresso.assertGone
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertVisible
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.files.SubmissionFileData
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.files.SubmissionFilesViewState
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.files.ui.SubmissionFilesFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsTabData
import com.instructure.student.ui.rendertests.renderpages.SubmissionFilesRenderPage
import com.instructure.student.ui.utils.StudentRenderTest
import com.spotify.mobius.runners.WorkRunner
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SubmissionFilesRenderTest : StudentRenderTest() {

    private val page = SubmissionFilesRenderPage()

    private val dataTemplate = SubmissionFileData(
        id = 123L,
        name = "File name",
        icon = R.drawable.ic_document,
        thumbnailUrl = null,
        isSelected = false,
        iconColor = Color.BLUE,
        selectionColor = Color.RED
    )

    @Test
    fun displaysEmptyState() {
        loadPageWithViewState(SubmissionFilesViewState.Empty)
        page.emptyView.assertVisible()
        page.recyclerView.assertGone()
    }

    @Test
    fun displaysFile() {
        val data = dataTemplate.copy()
        loadPageWithViewState(
            SubmissionFilesViewState.FileList(listOf(data))
        )
        page.icon.assertVisible()
        page.thumbnail.assertGone()
        page.filename.assertVisible().assertHasText(data.name)
        page.checkmark.assertGone()
    }

    @Test
    fun displaysSelectedFile() {
        val data = dataTemplate.copy(isSelected = true)
        loadPageWithViewState(
            SubmissionFilesViewState.FileList(listOf(data))
        )
        page.icon.assertVisible()
        page.thumbnail.assertGone()
        page.filename.assertVisible().assertHasText(data.name)
        page.checkmark.assertVisible()
    }

    @Test
    fun displaysFileWithThumbnail() {
        val data = dataTemplate.copy(
            thumbnailUrl = "https://avatars.githubusercontent.com/u/515326"
        )
        loadPageWithViewState(
            SubmissionFilesViewState.FileList(listOf(data))
        )
        page.icon.assertGone()
        page.thumbnail.assertVisible()
        page.filename.assertVisible().assertHasText(data.name)
        page.checkmark.assertGone()
    }

    private fun loadPageWithViewState(state: SubmissionFilesViewState): SubmissionFilesFragment {
        val emptyEffectRunner = object : WorkRunner {
            override fun dispose() = Unit
            override fun post(runnable: Runnable) = Unit
        }
        val data = SubmissionDetailsTabData.FileData(
            name = "Files",
            files = emptyList(),
            selectedFileId = 0L,
            canvasContext = Course()
        )
        val fragment = SubmissionFilesFragment.newInstance(data).apply {
            overrideInitViewState = state
            loopMod = { it.effectRunner { emptyEffectRunner } }
        }
        activityRule.activity.loadFragment(fragment)
        return fragment
    }

}

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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.student.espresso.StudentRenderTest
import com.instructure.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionModel
import com.instructure.student.mobius.assignmentDetails.submission.file.ui.UploadStatusSubmissionFragment
import com.instructure.pandautils.room.studentdb.entities.CreateFileSubmissionEntity
import com.spotify.mobius.runners.WorkRunner
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class UploadStatusSubmissionRenderTest : StudentRenderTest() {

    private lateinit var baseModel: UploadStatusSubmissionModel
    private val submissionId = 1L

    @Before
    fun setup() {
        baseModel = UploadStatusSubmissionModel(
            submissionId = submissionId
        )
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SUBMISSIONS, TestCategory.RENDER)
    fun displaysSuccessState() {
        loadPageWithModel(baseModel)
        uploadStatusSubmissionViewRenderPage.assertSuccessVisible()
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SUBMISSIONS, TestCategory.RENDER)
    fun displaysLoadingState() {
        loadPageWithModel(baseModel.copy(isLoading = true))
        uploadStatusSubmissionViewRenderPage.assertLoadingVisible()
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SUBMISSIONS, TestCategory.RENDER)
    fun displaysFailedState() {
        loadPageWithModel(baseModel.copy(isFailed = true))
        uploadStatusSubmissionViewRenderPage.assertFailedVisible()
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SUBMISSIONS, TestCategory.RENDER)
    fun displaysInProgressState() {
        loadPageWithModel(baseModel.copy(files = listOf(
            CreateFileSubmissionEntity(0, 0, null, null, null, null, null, null, false)
        )))
        uploadStatusSubmissionViewRenderPage.assertInProgressVisible()
    }

    private fun loadPageWithModel(model: UploadStatusSubmissionModel) {
        val emptyEffectRunner = object : WorkRunner {
            override fun dispose() = Unit
            override fun post(runnable: Runnable) = Unit
        }
        val route = UploadStatusSubmissionFragment.makeRoute(model.submissionId)
        val fragment = UploadStatusSubmissionFragment.newInstance(route)!!.apply {
            overrideInitModel = model
            loopMod = { it.effectRunner { emptyEffectRunner } }
        }
        activityRule.activity.loadFragment(fragment)
    }

}

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
package com.instructure.student.ui.renderTests.renderPages

import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertVisible
import com.instructure.espresso.page.BasePage
import com.instructure.student.R

class UploadStatusSubmissionViewRenderPage : BasePage(R.id.uploadStatusSubmissionPage) {

    val uploadStatusStateTitle by OnViewWithId(R.id.uploadStatusStateTitle)
    val uploadStatusStateMessage by OnViewWithId(R.id.uploadStatusStateMessage)
    val uploadStatusStateProgressLabel by OnViewWithId(R.id.uploadStatusStateProgressLabel)
    val uploadStatusStateProgress by OnViewWithId(R.id.uploadStatusStateProgress)
    val uploadStatusStateProgressPercent by OnViewWithId(R.id.uploadStatusStateProgressPercent)
    val uploadStatusStateProgressSize by OnViewWithId(R.id.uploadStatusStateProgressSize)

    val uploadStatusStateRetry by OnViewWithId(R.id.uploadStatusStateRetry)
    val uploadStatusStateCancel by OnViewWithId(R.id.uploadStatusStateCancel)
    val uploadStatusStateDone by OnViewWithId(R.id.uploadStatusStateDone)

    val uploadStatusRecycler by OnViewWithId(R.id.uploadStatusRecycler)
    val uploadStatusSuccessPanda by OnViewWithId(R.id.uploadStatusSuccessPanda)
    val uploadStatusLoading by OnViewWithId(R.id.uploadStatusLoading)

    fun assertLoadingVisible() {
        uploadStatusLoading.assertVisible()
    }

    fun assertSuccessVisible() {
        uploadStatusStateTitle.assertVisible()
        uploadStatusStateMessage.assertVisible()
        uploadStatusStateDone.assertVisible()
        uploadStatusSuccessPanda.assertVisible()
    }

    fun assertFailedVisible() {
        uploadStatusStateTitle.assertVisible()
        uploadStatusStateMessage.assertVisible()
        uploadStatusStateCancel.assertVisible()
        uploadStatusRecycler.assertVisible()
        uploadStatusStateRetry.assertVisible()
    }

    fun assertInProgressVisible() {
        uploadStatusStateProgressLabel.assertVisible()
        uploadStatusStateProgress.assertVisible()
        uploadStatusStateProgressPercent.assertVisible()
        uploadStatusStateProgressSize.assertVisible()
        uploadStatusStateCancel.assertVisible()
    }
}

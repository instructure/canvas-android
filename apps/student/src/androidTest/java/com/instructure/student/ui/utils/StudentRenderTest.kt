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
package com.instructure.student.ui.utils

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.student.SingleFragmentTestActivity
import com.instructure.student.ui.renderTests.renderPages.ConferenceDetailsRenderPage
import com.instructure.student.ui.renderTests.renderPages.ConferenceListRenderPage
import com.instructure.student.ui.renderTests.renderPages.PairObserverRenderPage
import com.instructure.student.ui.renderTests.renderPages.SubmissionDetailsEmptyContentRenderPage
import com.instructure.student.ui.renderTests.renderPages.SubmissionDetailsRenderPage
import com.instructure.student.ui.renderTests.renderPages.SyllabusRenderPage
import com.instructure.student.ui.renderTests.renderPages.TextSubmissionUploadRenderPage
import com.instructure.student.ui.renderTests.renderPages.UploadStatusSubmissionViewRenderPage
import com.instructure.student.ui.renderTests.renderPages.UrlSubmissionUploadRenderPage
import com.instructure.student.ui.renderTests.renderPages.UrlSubmissionViewRenderPage
import org.junit.runner.RunWith

// Test from which all Student PageRender/SingleFragmentTestActivity tests will derive
@RunWith(AndroidJUnit4::class)
abstract class StudentRenderTest : StudentTest() {

    override val activityRule = StudentActivityTestRule(SingleFragmentTestActivity::class.java)

    override fun displaysPageObjects() {
        // Do nothing for page render tests
    }

    val conferenceListRenderPage = ConferenceListRenderPage()
    val conferenceDetailsRenderPage = ConferenceDetailsRenderPage()
    val submissionDetailsRenderPage = SubmissionDetailsRenderPage()
    val submissionDetailsEmptyContentRenderPage = SubmissionDetailsEmptyContentRenderPage()
    val syllabusRenderPage = SyllabusRenderPage()
    val pairObserverRenderPage = PairObserverRenderPage()
    val textSubmissionUploadRenderPage = TextSubmissionUploadRenderPage()
    val urlSubmissionViewRenderPage = UrlSubmissionViewRenderPage()
    val urlSubmissionUploadRenderPage = UrlSubmissionUploadRenderPage()
    val uploadStatusSubmissionViewRenderPage = UploadStatusSubmissionViewRenderPage()
}

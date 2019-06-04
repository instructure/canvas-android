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

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.student.espresso.StudentRenderTest
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsContentType
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.MediaSubmissionViewFragment
import com.instructure.student.ui.pages.renderPages.MediaSubmissionViewRenderPage
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaSubmissionViewRenderTest : StudentRenderTest() {

    private val page = MediaSubmissionViewRenderPage()

    private val mediaTemplate = SubmissionDetailsContentType.MediaContent(
        Uri.parse("https://notareal.instructure.com/users/1234/media_download?entryId=abc123&redirect=1&type=mp4"),
        "video/mp4",
        null,
        "Media Test"
    )

    @Test
    fun displaysMediaButton() {
        loadPageWithViewData(mediaTemplate)

        page.mediaProgressBar.assertNotDisplayed()
        page.submissionMediaPlayerView.assertNotDisplayed()
        page.mediaPlaybackErrorView.assertNotDisplayed()

        page.prepareMediaButton.assertDisplayed()
    }

    @Test
    fun displaysErrorView() {
        loadPageWithViewData(mediaTemplate)

        page.prepareMediaButton.click()
        page.mediaPlaybackErrorView.assertDisplayed()

        page.mediaProgressBar.assertNotDisplayed()
        page.submissionMediaPlayerView.assertNotDisplayed()
        page.prepareMediaButton.assertNotDisplayed()
    }

    private fun loadPageWithViewData(data: SubmissionDetailsContentType.MediaContent): MediaSubmissionViewFragment {
        val fragment = MediaSubmissionViewFragment.newInstance(data)
        activityRule.activity.loadFragment(fragment)
        return fragment
    }

}
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

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import com.instructure.canvasapi2.models.Attachment
import com.instructure.dataseeding.model.AttachmentApiModel
import com.instructure.espresso.*
import com.instructure.espresso.matchers.RecyclerViewMatcher
import com.instructure.espresso.page.BasePage
import com.instructure.teacher.R
import org.hamcrest.Matchers

class SpeedGraderFilesPage : BasePage() {

    private val speedGraderFileRecyclerView by OnViewWithId(R.id.speedGraderFilesRecyclerView)

    private val emptySpeedGraderFileView by WaitForViewWithId(R.id.speedGraderFilesEmptyView)

    fun assertDisplaysEmptyView() {
        emptySpeedGraderFileView.assertDisplayed()
    }

    fun assertHasFiles(attachments: MutableList<Attachment>) {
        speedGraderFileRecyclerView.check(RecyclerViewItemCountAssertion(attachments.size))
        for (attachment in attachments) onView(withText(attachment.filename))
    }

    fun assertFileSelected(position: Int) {
        onView(RecyclerViewMatcher(R.id.speedGraderFilesRecyclerView).atPosition(position))
                .check(ViewAssertions.matches(hasDescendant(Matchers.allOf(withId(R.id.isSelectedIcon), withEffectiveVisibility(Visibility.VISIBLE)))))
    }

    fun selectFile(position: Int) {
        onView(RecyclerViewMatcher(R.id.speedGraderFilesRecyclerView).atPosition(position)).click()
    }

}

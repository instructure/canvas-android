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
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvasapi2.models.Attachment
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.matchers.RecyclerViewMatcher
import com.instructure.espresso.page.BasePage
import com.instructure.teacher.R
import org.hamcrest.Matchers

/**
 * Represents the SpeedGrader files page.
 *
 * This page provides functionality for interacting with the elements on the SpeedGrader files page. It contains methods
 * for asserting the presence of an empty view, asserting the presence of files, asserting the selection of a file,
 * and selecting a file. This page extends the BasePage class.
 */
class SpeedGraderFilesPage : BasePage() {

    private val speedGraderFileRecyclerView by OnViewWithId(R.id.speedGraderFilesRecyclerView)

    private val emptySpeedGraderFileView by WaitForViewWithId(R.id.speedGraderFilesEmptyView)

    /**
     * Asserts the presence of an empty view.
     */
    fun assertDisplaysEmptyView() {
        emptySpeedGraderFileView.assertDisplayed()
    }

    /**
     * Asserts the presence of files with the given attachments.
     *
     * @param attachments The list of attachments representing the files.
     */
    fun assertHasFiles(attachments: MutableList<Attachment>) {
        speedGraderFileRecyclerView.check(RecyclerViewItemCountAssertion(attachments.size))
        for (attachment in attachments) onView(withText(attachment.filename))
    }

    /**
     * Asserts the selection of a file at the specified position.
     *
     * @param position The position of the file in the list.
     */
    fun assertFileSelected(position: Int) {
        onView(RecyclerViewMatcher(R.id.speedGraderFilesRecyclerView).atPosition(position))
            .check(ViewAssertions.matches(hasDescendant(Matchers.allOf(withId(R.id.isSelectedIcon), withEffectiveVisibility(Visibility.VISIBLE)))))
    }

    /**
     * Selects a file at the specified position.
     *
     * @param position The position of the file in the list.
     */
    fun selectFile(position: Int) {
        onView(RecyclerViewMatcher(R.id.speedGraderFilesRecyclerView).atPosition(position)).click()
    }

}

/*
 * Copyright (C) 2018 - present Instructure, Inc.
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

import androidx.test.espresso.action.ViewActions
import com.instructure.dataseeding.model.PageApiModel
import com.instructure.espresso.*
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.teacher.R

class PageListPage : BasePage() {

    private val searchButton by OnViewWithId(R.id.search)

    private val searchInput by WaitForViewWithId(androidx.appcompat.R.id.search_src_text)

    private val pageRecyclerView by OnViewWithId(R.id.pageRecyclerView)

    private val toolbar by OnViewWithId(R.id.pageListToolbar)

    fun assertHasPage(page: PageApiModel) {
        waitForViewWithText(page.title).assertDisplayed()
    }

    fun openSearch() {
        searchButton.click()
    }

    fun enterSearchQuery(query: String) {
        searchInput.perform(ViewActions.replaceText(query))
    }

    fun assertPageCount(count: Int) {
        pageRecyclerView.waitForCheck(RecyclerViewItemCountAssertion(count))
    }

}

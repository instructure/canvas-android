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

import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvasapi2.models.Page
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.teacher.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString

class PageListPage : BasePage() {

    private val searchButton by OnViewWithId(R.id.search)

    private val searchInput by WaitForViewWithId(androidx.appcompat.R.id.search_src_text)

    private val pageRecyclerView by OnViewWithId(R.id.pageRecyclerView)

    private val toolbar by OnViewWithId(R.id.pageListToolbar)

    fun clickOnCreateNewPage() {
        onView(withId(R.id.createNewPage)).click()
    }

    fun assertHasPage(page: Page) {
        waitForViewWithText(page.title!!).assertDisplayed()
    }

    fun openPage(pageTitle: String) {
        val matcher = getPageMatcherByTitle(pageTitle = pageTitle)
        scrollRecyclerView(R.id.pageRecyclerView, matcher)
        onView(matcher).click()
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

    fun assertFrontPageDisplayed(pageTitle: String) {
        val matcher = getFrontPageMatcher(pageTitle = pageTitle)
        scrollRecyclerView(R.id.pageRecyclerView, matcher)
        Espresso.onView(matcher).assertDisplayed()
    }

    fun assertPageDisplayed(pageTitle: String) {
        assertCommonPageDisplayed(pageTitle)
    }

    fun assertPageIsUnpublished(pageTitle: String) {
        checkPagePublishedStatus(pageTitle = pageTitle, published = false)
    }

    fun assertPageIsPublished(pageTitle: String) {
        checkPagePublishedStatus(pageTitle = pageTitle, published = true)
    }

    private fun checkPagePublishedStatus(pageTitle: String, published: Boolean) {
        onView(allOf(
                withId(R.id.pageLayout),
                withContentDescription(containsString(pageTitle)),
                withContentDescription(containsString(if(published) "Published" else "Unpublished"))))
                .assertDisplayed()
    }
    private fun assertCommonPageDisplayed(pageTitle: String) {
        val matcher = getPageMatcherByTitle(pageTitle)
        scrollRecyclerView(R.id.pageRecyclerView, matcher)
        onView(matcher).assertDisplayed()
    }

    private fun getPageMatcherByTitle(pageTitle: String) : Matcher<View> {
        return allOf(
                withId(R.id.pageTitle),
                withText(pageTitle)
        )
    }

    private fun getFrontPageMatcher(pageTitle: String) : Matcher<View> {
        return allOf(
                withId(R.id.pageTitle),
                withText(pageTitle),
                hasSibling(allOf(
                        withId(R.id.statusIndicator),
                        withText(R.string.frontPage)
                )))
    }
}

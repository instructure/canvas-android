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
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvasapi2.models.Page
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.Searchable
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.espresso.waitForCheck
import com.instructure.teacher.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString

/**
 * Represents the Page List Page.
 *
 * This page extends the BasePage class and provides functionality for interacting with the elements on the "Page List" page.
 * It contains methods for clicking on the create new page button, opening a page, performing a search, and asserting various page-related conditions.
 */
class PageListPage(val searchable: Searchable) : BasePage() {

    private val pageRecyclerView by OnViewWithId(R.id.pageRecyclerView)

    private val toolbar by OnViewWithId(R.id.pageListToolbar)

    /**
     * Clicks on the "Create New Page" button.
     */
    fun clickOnCreateNewPage() {
        onView(withId(R.id.createNewPage)).click()
    }

    /**
     * Asserts that the page list contains the specified page.
     *
     * @param page The page to be asserted.
     */
    fun assertHasPage(page: Page) {
        waitForViewWithText(page.title!!).assertDisplayed()
    }

    /**
     * Opens the specified page.
     *
     * @param pageTitle The title of the page to be opened.
     */
    fun openPage(pageTitle: String) {
        val matcher = getPageMatcherByTitle(pageTitle = pageTitle)
        scrollRecyclerView(R.id.pageRecyclerView, matcher)
        onView(matcher).click()
    }

    /**
     * Asserts the number of pages in the page list.
     *
     * @param count The expected page count.
     */
    fun assertPageCount(count: Int) {
        pageRecyclerView.waitForCheck(RecyclerViewItemCountAssertion(count))
    }

    /**
     * Asserts that the front page with the specified title is displayed.
     *
     * @param pageTitle The title of the front page.
     */
    fun assertFrontPageDisplayed(pageTitle: String) {
        val matcher = getFrontPageMatcher(pageTitle = pageTitle)
        scrollRecyclerView(R.id.pageRecyclerView, matcher)
        Espresso.onView(matcher).assertDisplayed()
    }

    /**
     * Asserts that a page with the specified title is displayed.
     *
     * @param pageTitle The title of the page.
     */
    fun assertPageDisplayed(pageTitle: String) {
        assertCommonPageDisplayed(pageTitle)
    }

    /**
     * Asserts that a page with the specified title is unpublished.
     *
     * @param pageTitle The title of the page.
     */
    fun assertPageIsUnpublished(pageTitle: String) {
        checkPagePublishedStatus(pageTitle = pageTitle, published = false)
    }

    /**
     * Asserts that a page with the specified title is published.
     *
     * @param pageTitle The title of the page.
     */
    fun assertPageIsPublished(pageTitle: String) {
        checkPagePublishedStatus(pageTitle = pageTitle, published = true)
    }

    private fun checkPagePublishedStatus(pageTitle: String, published: Boolean) {
        onView(allOf(
            withId(R.id.pageLayout),
            withContentDescription(containsString(pageTitle)),
            withContentDescription(containsString(if (published) "Published" else "Unpublished"))))
            .assertDisplayed()
    }

    private fun assertCommonPageDisplayed(pageTitle: String) {
        val matcher = getPageMatcherByTitle(pageTitle)
        scrollRecyclerView(R.id.pageRecyclerView, matcher)
        onView(matcher).assertDisplayed()
    }

    private fun getPageMatcherByTitle(pageTitle: String): Matcher<View> {
        return allOf(
            withId(R.id.pageTitle),
            withText(pageTitle)
        )
    }

    private fun getFrontPageMatcher(pageTitle: String): Matcher<View> {
        return allOf(
            withId(R.id.pageTitle),
            withText(pageTitle),
            hasSibling(allOf(
                withId(R.id.statusIndicator),
                withText(R.string.frontPage)
            )))
    }
}


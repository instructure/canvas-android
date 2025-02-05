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
package com.instructure.student.ui.pages

import android.view.View
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvasapi2.models.Page
import com.instructure.dataseeding.model.PageApiModel
import com.instructure.espresso.DoesNotExistAssertion
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.Searchable
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.waitForCheck
import com.instructure.student.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf

class PageListPage(val searchable: Searchable) : BasePage(R.id.pageListPage) {

    fun assertFrontPageDisplayed(page: PageApiModel) {
        val matcher = getFrontPageMatcher(page)
        scrollRecyclerView(R.id.listView, matcher)
        onView(matcher).assertDisplayed()
    }

    fun selectFrontPage(page: PageApiModel) {
        val matcher = getFrontPageMatcher(page)
        scrollRecyclerView(R.id.listView, matcher)
        onView(matcher).click()
    }

    // This matcher was sufficiently complex that I didn't want to replicate the code for it
    private fun getFrontPageMatcher(page: PageApiModel) : Matcher<View> {
        return allOf(
                withId(R.id.homeSubLabel),
                withText(page.title),
                hasSibling(allOf(
                        withId(R.id.homeLabel),
                        withText(R.string.frontPage)
                )))
    }

    fun assertRegularPageDisplayed(page: PageApiModel) {
        assertRegularPageDisplayedCommon(page.title)
    }

    fun assertRegularPageDisplayed(page: Page) {
        assertRegularPageDisplayedCommon(page.title!!)
    }

    private fun assertRegularPageDisplayedCommon(pageTitle: String) {
        val matcher = allOf(
                withId(R.id.title),
                withText(pageTitle)
        )

        scrollRecyclerView(R.id.listView, matcher)
        onView(matcher).assertDisplayed()

    }

    fun selectRegularPage(page: PageApiModel) {
        val matcher = allOf(
                withId(R.id.title),
                withText(page.title)
        )
        scrollRecyclerView(R.id.listView, matcher)
        onView(matcher).click()
    }

    fun selectRegularPage(page: Page) {
        val matcher = allOf(
                withId(R.id.title),
                withText(page.title)
        )
        scrollRecyclerView(R.id.listView, matcher)
        onView(matcher).click()
    }

    fun assertPageNotDisplayed(page: PageApiModel) {
        // Check for front page
        onView(allOf(withId(R.id.homeSubLabel), withText(page.title))).check(DoesNotExistAssertion(10))

        // Check for regular page
        onView(allOf(withId(R.id.title), withText(page.title))).check(DoesNotExistAssertion(10))
    }

    fun assertPageListItemCount(expectedCount: Int) {
        onView(allOf(withId(R.id.listView) +
                ViewMatchers.withParent(withId(R.id.swipeRefreshLayout)) +
                withAncestor(withId(R.id.pageListPage)))).waitForCheck(RecyclerViewItemCountAssertion(expectedCount))
    }
}
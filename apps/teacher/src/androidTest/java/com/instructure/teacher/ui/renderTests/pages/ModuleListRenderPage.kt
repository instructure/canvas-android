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
package com.instructure.teacher.ui.renderTests.pages

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.matchers.WithDrawableViewMatcher
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.SwipeRefreshLayoutMatchers
import com.instructure.teacher.ui.utils.ViewSizeMatcher
import org.hamcrest.CoreMatchers.allOf

class ModuleListRenderPage : BasePage(R.id.moduleList) {

    val emptyView by OnViewWithId(R.id.moduleListEmptyView)
    val fullErrorView by OnViewWithId(R.id.moduleListFullErrorView)
    val inlineErrorView by OnViewWithId(R.id.moduleListInlineErrorView)
    val inlineLoadingView by OnViewWithId(R.id.moduleListInlineLoadingView)
    val swipeRefreshView by OnViewWithId(R.id.swipeRefreshLayout)
    val recyclerView by OnViewWithId(R.id.recyclerView)

    /* Module views. Can only be used if there is a single module present. */
    val moduleName by OnViewWithId(R.id.moduleName)
    val modulePublishedIcon by OnViewWithId(R.id.publishedIcon)
    val moduleUnpublishedIcon by OnViewWithId(R.id.unpublishedIcon)

    /* Module Item views. Can only be used if there is a single module item present. */
    val moduleItemRoot by OnViewWithId(R.id.moduleItemRoot)
    val moduleItemIcon by OnViewWithId(R.id.moduleItemIcon)
    val moduleItemTitle by OnViewWithId(R.id.moduleItemTitle)
    val moduleItemIndent by OnViewWithId(R.id.moduleItemIndent)
    val moduleItemSubtitle by OnViewWithId(R.id.moduleItemSubtitle)
    val moduleItemStatusIcon by OnViewWithId(R.id.moduleItemStatusIcon)
    val moduleItemPublishedIcon by OnViewWithId(R.id.moduleItemPublishedIcon)
    val moduleItemUnpublishedIcon by OnViewWithId(R.id.moduleItemUnpublishedIcon)
    val moduleItemLoadingView by OnViewWithId(R.id.moduleItemLoadingView)

    fun assertDisplaysToolbarText(title: String) {
        onView(allOf(withText(title), withAncestor(R.id.toolbar))).assertDisplayed()
    }

    fun assertRefreshing(isRefreshing: Boolean) {
        swipeRefreshView.check(matches(SwipeRefreshLayoutMatchers.isRefreshing(isRefreshing)))
    }

    fun clickItemAtPosition(position: Int) {
        recyclerView.perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(position, ViewActions.click())
        )
    }

    fun assertListItemCount(count: Int) {
        recyclerView.check(RecyclerViewItemCountAssertion(count))
    }

    fun assertHasItemIndent(indent: Int) {
        moduleItemIndent.check(matches(ViewSizeMatcher.hasWidth(indent)))
    }

    fun assertStatusIcon(@DrawableRes iconId: Int, @ColorRes tintId: Int) {
        WithDrawableViewMatcher(iconId, tintId).matches(withId(R.id.moduleItemStatusIcon))
    }
}

/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.features.pages.list

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.filterWithQuery
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.utils.textAndIconColor
import com.instructure.pandautils.utils.toast
import com.instructure.student.R
import com.instructure.student.adapter.BaseListRecyclerAdapter
import com.instructure.student.holders.FrontPageViewHolder
import com.instructure.student.holders.PageViewHolder
import com.instructure.student.interfaces.AdapterToFragmentCallback

open class PageListRecyclerAdapter(
    context: Context,
    private val repository: PageListRepository,
    private val canvasContext: CanvasContext,
    private val adapterToFragmentCallback: AdapterToFragmentCallback<Page>,
    private var selectedPageTitle: String = FRONT_PAGE_DETERMINER // Page urls only specify the title, not the pageId
) : BaseListRecyclerAdapter<Page, RecyclerView.ViewHolder>(context, Page::class.java) {

    private var apiCall: WeaveJob? = null

    private var pages = emptyList<Page>()

    var searchQuery = ""
        set(value) {
            field = value
            clear()
            populateData()
        }

    private fun populateData() {
        addAll(pages.filterWithQuery(searchQuery, Page::title))
        adapterToFragmentCallback.onRefreshFinished()
        onCallbackFinished()
    }

    init {
        itemCallback = object : ItemComparableCallback<Page>() {
            override fun compare(page1: Page, page2: Page) = page1.compareTo(page2)
            override fun areContentsTheSame(item1: Page, item2: Page) = item1.title == item2.title
            override fun getUniqueItemId(page: Page) = page.id
        }
        @Suppress("LeakingThis") loadData()
    }

    override fun createViewHolder(v: View, viewType: Int) = when (viewType) {
        FRONT_PAGE -> FrontPageViewHolder(v)
        else -> PageViewHolder(v)
    }

    override fun bindHolder(page: Page, holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PageViewHolder -> holder.bind(context, page, canvasContext.textAndIconColor, adapterToFragmentCallback)
            else -> FrontPageViewHolder.bind(context, holder as FrontPageViewHolder, page, adapterToFragmentCallback)
        }
    }

    override fun itemLayoutResId(viewType: Int) = when (viewType) {
            FRONT_PAGE -> FrontPageViewHolder.HOLDER_RES_ID
            else -> PageViewHolder.HOLDER_RES_ID
        }

    override fun add(item: Page) {
        if (selectedPageTitle == item.url) {
            setSelectedItemId(item.id)
        } else if (selectedPageTitle == FRONT_PAGE_DETERMINER && item.frontPage) {
            setSelectedItemId(item.id)
        }
        super.add(item)
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItemAtPosition(position)?.frontPage == true) FRONT_PAGE else NORMAL_PAGE
    }


    override fun loadFirstPage() {
        apiCall = tryWeave {
            pages = repository.loadPages(canvasContext, isRefresh)
            populateData()
            isAllPagesLoaded = true
            adapterToFragmentCallback.onRefreshFinished()
            onCallbackFinished()
        } catch {
            if (itemCount == 0 || !APIHelper.hasNetworkConnection()) {
                adapterToRecyclerViewCallback?.setIsEmpty(true)
            } else {
                context.toast(R.string.errorOccurred)
            }
        }
    }

    override fun cancel() {
        apiCall?.cancel()
        super.cancel()
    }

    companion object {
        const val FRONT_PAGE_DETERMINER = ""
        private const val FRONT_PAGE = 0
        private const val NORMAL_PAGE = 1
    }

    // endregion
}

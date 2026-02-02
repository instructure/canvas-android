/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.teacher.fragments

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_PAGE_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.utils.ColorUtils
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.addSearch
import com.instructure.pandautils.utils.applyBottomSystemBarMargin
import com.instructure.pandautils.utils.applyBottomSystemBarInsets
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.closeSearch
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.getDrawableCompat
import com.instructure.pandautils.utils.nonNullArgs
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.adapters.PageListAdapter
import com.instructure.teacher.databinding.FragmentPageListBinding
import com.instructure.teacher.events.PageCreatedEvent
import com.instructure.teacher.events.PageDeletedEvent
import com.instructure.teacher.events.PageUpdatedEvent
import com.instructure.teacher.factory.PageListPresenterFactory
import com.instructure.teacher.holders.PageViewHolder
import com.instructure.teacher.presenters.PageListPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.RecyclerViewUtils
import com.instructure.teacher.utils.setupBackButton
import com.instructure.teacher.viewinterface.PageListView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@PageView(url = "{canvasContext}/pages")
@ScreenView(SCREEN_VIEW_PAGE_LIST)
class PageListFragment : BaseSyncFragment<Page, PageListPresenter, PageListView, PageViewHolder, PageListAdapter>(), PageListView {

    private val binding by viewBinding(FragmentPageListBinding::bind)

    @get:PageViewUrlParam("canvasContext")
    var canvasContext: CanvasContext by ParcelableArg(default = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, -1L, ""))

    private val linearLayoutManager by lazy { LinearLayoutManager(requireContext()) }
    private lateinit var mRecyclerView: RecyclerView

    private var needToForceNetwork = false

    override fun layoutResId(): Int = R.layout.fragment_page_list
    override val recyclerView: RecyclerView get() = binding.pageRecyclerView
    override fun getPresenterFactory() = PageListPresenterFactory(canvasContext)
    override fun onPresenterPrepared(presenter: PageListPresenter) = with(binding) {
        mRecyclerView = RecyclerViewUtils.buildRecyclerView(
            rootView = rootView,
            context = requireContext(),
            recyclerAdapter = adapter,
            presenter = presenter,
            swipeToRefreshLayoutResId = R.id.swipeRefreshLayout,
            recyclerViewResId = R.id.pageRecyclerView,
            emptyViewResId = R.id.emptyPandaView,
            emptyViewText = getString(R.string.noPages)
        )

        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && createNewPage.visibility == View.VISIBLE) {
                    createNewPage.hide()
                } else if (dy < 0 && createNewPage.visibility != View.VISIBLE) {
                    createNewPage.show()
                }
            }
        })

        setupViews()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        needToForceNetwork = savedInstanceState?.getBoolean(NEED_FORCE_REFRESH_KEY).orDefault()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(NEED_FORCE_REFRESH_KEY, needToForceNetwork)
    }

    override fun onCreateView(view: View) {
        linearLayoutManager.orientation = RecyclerView.VERTICAL
    }

    override fun onReadySetGo(presenter: PageListPresenter) {
        if(recyclerView.adapter == null) {
            mRecyclerView.adapter = adapter
        }
        presenter.loadData(needToForceNetwork)
    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun createAdapter(): PageListAdapter {
        return PageListAdapter(requireContext(), presenter, canvasContext.color) { page ->
            val args = PageDetailsFragment.makeBundle(page)
            RouteMatcher.route(requireActivity(), Route(null, PageDetailsFragment::class.java, canvasContext, args))
        }
    }


    override fun onRefreshStarted() = with(binding) {
        createNewPage.setGone()
        //this prevents two loading spinners from happening during pull to refresh
        if(!swipeRefreshLayout.isRefreshing) {
            emptyPandaView.visibility  = View.VISIBLE
        }
        emptyPandaView.setLoading()
    }

    override fun onRefreshFinished() {
        binding.swipeRefreshLayout.isRefreshing = false
        binding.createNewPage.setVisible()
    }

    override fun checkIfEmpty() = with(binding) {
        // We don't want to leave the fab hidden if the list is empty
        if(presenter.isEmpty) {
            createNewPage.show()
            emptyPandaView.setEmptyViewImage(requireContext().getDrawableCompat(R.drawable.ic_panda_nofiles))
            emptyPandaView.setMessageText(R.string.noPagesSubtext)
        }
        RecyclerViewUtils.checkIfEmpty(emptyPandaView, mRecyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
    }

    override fun perPageCount() = ApiPrefs.perPageCount

    private fun setupToolbar() = with(binding) {
        pageListToolbar.applyTopSystemBarInsets()
        pageListToolbar.title = getString(R.string.tab_pages)
        pageListToolbar.subtitle = canvasContext.name
        pageListToolbar.setupBackButton(this@PageListFragment)
        pageListToolbar.addSearch(getString(R.string.searchPagesHint)) { query ->
            if (query.isBlank()) {
                emptyPandaView.emptyViewText(R.string.no_items_to_display_short)
            } else {
                emptyPandaView.emptyViewText(getString(R.string.noItemsMatchingQuery, query))
            }
            presenter.searchQuery = query
        }
        ViewStyler.themeToolbarColored(requireActivity(), pageListToolbar, canvasContext.color, requireContext().getColor(R.color.textLightest))
    }

    private fun setupViews() = with(binding) {
        pageRecyclerView.applyBottomSystemBarInsets()
        pageRecyclerView.clipToPadding = false

        createNewPage.setGone()
        createNewPage.backgroundTintList = ViewStyler.makeColorStateListForButton()
        createNewPage.setImageDrawable(ColorUtils.colorIt(ThemePrefs.buttonTextColor, createNewPage.drawable))
        createNewPage.onClickWithRequireNetwork {
            val args = CreateOrEditPageDetailsFragment.newInstanceCreate(canvasContext).nonNullArgs
            RouteMatcher.route(requireActivity(), Route(CreateOrEditPageDetailsFragment::class.java, null, args))
        }
        createNewPage.applyBottomSystemBarMargin()
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onPageCreated(event: PageCreatedEvent) {
        event.once(javaClass.simpleName) {
            // need to set a flag here. Because we use the event bus in the fragment instead of the presenter for unit testing purposes,
            // when we come back to this fragment it will go through the life cycle events again and the cached data will immediately
            // overwrite the data from the network if we refresh the presenter from here.
            needToForceNetwork = true
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onPageUpdated(event: PageUpdatedEvent) {
        event.once(javaClass.simpleName) {
            // need to set a flag here. Because we use the event bus in the fragment instead of the presenter for unit testing purposes,
            // when we come back to this fragment it will go through the life cycle events again and the cached data will immediately
            // overwrite the data from the network if we refresh the presenter from here.
            needToForceNetwork = true
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onPageDeleted(event: PageDeletedEvent) {
        event.get {
            adapter.remove(it)
        }
    }

    override fun onHandleBackPressed() = binding.pageListToolbar.closeSearch()

    companion object {
        private const val NEED_FORCE_REFRESH_KEY = "need_force_refresh_key"

        fun newInstance(canvasContext: CanvasContext) = PageListFragment().apply {
            this.canvasContext = canvasContext
        }
    }
}

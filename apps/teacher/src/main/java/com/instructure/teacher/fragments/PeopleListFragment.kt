/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */

package com.instructure.teacher.fragments

import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.interactions.router.Route
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.adapters.PeopleListRecyclerAdapter
import com.instructure.teacher.adapters.StudentContextFragment
import com.instructure.teacher.dialog.PeopleListFilterDialog
import com.instructure.teacher.factory.PeopleListPresenterFactory
import com.instructure.teacher.holders.UserViewHolder
import com.instructure.teacher.interfaces.AdapterToFragmentCallback
import com.instructure.teacher.presenters.PeopleListPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.RecyclerViewUtils
import com.instructure.teacher.utils.setupBackButton
import com.instructure.teacher.viewinterface.PeopleListView
import instructure.androidblueprint.PresenterFactory
import kotlinx.android.synthetic.main.fragment_people_list_layout.*
import kotlinx.android.synthetic.main.recycler_swipe_refresh_layout.*
import java.util.*

class PeopleListFragment : BaseSyncFragment<User, PeopleListPresenter, PeopleListView, UserViewHolder, PeopleListRecyclerAdapter>(), PeopleListView, SearchView.OnQueryTextListener {

    private var mCanvasContextsSelected: ArrayList<CanvasContext>? = null

    internal var mRecyclerView: RecyclerView? = null

    override fun layoutResId(): Int = R.layout.fragment_people_list_layout

    override fun onCreateView(view: View) {}

    override fun onReadySetGo(presenter: PeopleListPresenter) {
        if (mRecyclerView?.adapter == null) {
            mRecyclerView?.adapter = adapter
        }

        setupViews()
        presenter.loadData(false)
    }

    override fun onHandleBackPressed() = peopleListToolbar.closeSearch()

    private fun setupViews() {
        val canvasContext = nonNullArgs.getParcelable<CanvasContext>(Const.CANVAS_CONTEXT)
        peopleListToolbar.setTitle(R.string.tab_people)
        peopleListToolbar.subtitle = canvasContext!!.name
        if (peopleListToolbar.menu.size() == 0) peopleListToolbar.inflateMenu(R.menu.menu_people_list)
        val searchView = peopleListToolbar.menu.findItem(R.id.search).actionView as SearchView

        peopleListToolbar.menu.findItem(R.id.search).setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                filterTitleWrapper.visibility = View.GONE
                swipeRefreshLayout.isEnabled = false
                if (peopleListToolbar.menu.findItem(R.id.peopleFilterMenuItem) != null) {
                    peopleListToolbar.menu.findItem(R.id.peopleFilterMenuItem).isVisible = false
                }
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                filterTitleWrapper.visibility = View.VISIBLE
                swipeRefreshLayout.isEnabled = true
                if (peopleListToolbar.menu.findItem(R.id.peopleFilterMenuItem) != null) {
                    peopleListToolbar.menu.findItem(R.id.peopleFilterMenuItem).isVisible = true
                }
                presenter.refresh(false)
                return true
            }
        })

        searchView.setOnQueryTextListener(this)

        peopleListToolbar.menu.findItem(R.id.peopleFilterMenuItem)?.setOnMenuItemClickListener {
            //let the user select the course/group they want to see
            PeopleListFilterDialog.getInstance(requireActivity().supportFragmentManager, presenter.canvasContextListIds, canvasContext, true) { canvasContexts ->
                mCanvasContextsSelected = ArrayList()
                mCanvasContextsSelected!!.addAll(canvasContexts)

                presenter.canvasContextList = mCanvasContextsSelected as ArrayList<CanvasContext>
                setupTitle(canvasContexts)
            }.show(requireActivity().supportFragmentManager, PeopleListFilterDialog::class.java.simpleName)
            false
        }

        clearFilterTextView.setOnClickListener {
            peopleFilter.setText(R.string.allPeople)
            presenter.clearCanvasContextList()
            clearFilterTextView.visibility = View.GONE
        }

        setupTitle(presenter.canvasContextList)
        ViewStyler.themeToolbar(requireActivity(), peopleListToolbar, ColorKeeper.getOrGenerateColor(canvasContext), Color.WHITE)
        peopleListToolbar.setupBackButton(this)
    }

    /**
     * Used to set up the title at the top of the recyclerview. If there are filters set it'll use the titles of the canvas requireContext()s
     * as the title. Otherwise it will say "All People"
     *
     * @param canvasContexts
     */
    private fun setupTitle(canvasContexts: ArrayList<CanvasContext>) {
        if (canvasContexts.isEmpty()) {
            peopleFilter.setText(R.string.allPeople)
            clearFilterTextView.visibility = View.GONE
            return
        }
        // Get the title based on Canvas Contexts selected (groups and/or sections)
        val title = StringBuilder()
        for (i in canvasContexts.indices) {
            title.append(canvasContexts[i].name)
            // Only add the comma if it's not the last in the list
            if (i + 1 < canvasContexts.size) {
                title.append(", ")
            }
        }

        peopleFilter.text = title.toString().trim { it <= ' ' }
        clearFilterTextView.visibility = View.VISIBLE
    }


    override fun getPresenterFactory(): PresenterFactory<PeopleListPresenter> =
            PeopleListPresenterFactory(nonNullArgs.getParcelable<Parcelable>(Const.CANVAS_CONTEXT) as CanvasContext)


    override fun onPresenterPrepared(presenter: PeopleListPresenter) {
        mRecyclerView = RecyclerViewUtils.buildRecyclerView(mRootView, requireContext(), adapter,
                presenter, R.id.swipeRefreshLayout, R.id.recyclerView, R.id.emptyPandaView, getString(R.string.no_items_to_display_short))
        addSwipeToRefresh(swipeRefreshLayout!!)
    }

    override fun getAdapter(): PeopleListRecyclerAdapter {
        if (mAdapter == null) {
            mAdapter = PeopleListRecyclerAdapter(requireContext(), presenter, AdapterToFragmentCallback { user, _ ->
                val canvasContext = nonNullArgs.getParcelable<CanvasContext>(Const.CANVAS_CONTEXT)!!
                if (canvasContext.isDesigner()) {
                    showToast(R.string.errorIsDesigner)
                    return@AdapterToFragmentCallback
                }
                val bundle = StudentContextFragment.makeBundle(user.id, canvasContext.id, true)
                RouteMatcher.route(requireContext(), Route(null, StudentContextFragment::class.java, canvasContext, bundle))
            })
        }
        return mAdapter
    }

    override fun onQueryTextSubmit(query: String): Boolean = false

    override fun onQueryTextChange(newText: String): Boolean {
        adapter.clear()
        presenter.searchPeopleList(newText)
        return true
    }

    override fun getRecyclerView(): RecyclerView? = mRecyclerView

    override fun withPagination(): Boolean = true

    override fun perPageCount(): Int = ApiPrefs.perPageCount

    override fun onRefreshFinished() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun onRefreshStarted() {
        //this prevents two loading spinners from happening during pull to refresh
        if(!swipeRefreshLayout.isRefreshing) {
            emptyPandaView.visibility  = View.VISIBLE
        }
        emptyPandaView.setLoading()
    }

    override fun checkIfEmpty() {
        RecyclerViewUtils.checkIfEmpty(emptyPandaView, mRecyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
    }

    companion object {
        fun newInstance(canvasContext: CanvasContext): PeopleListFragment {
            val args = Bundle()
            args.putParcelable(Const.CANVAS_CONTEXT, canvasContext)
            val fragment = PeopleListFragment()
            fragment.arguments = args
            return fragment
        }
    }
}

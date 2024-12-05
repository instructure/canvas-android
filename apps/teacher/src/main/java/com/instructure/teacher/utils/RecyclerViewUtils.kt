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
package com.instructure.teacher.utils

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.instructure.pandarecycler.interfaces.EmptyInterface
import com.instructure.pandautils.utils.Utils
import com.instructure.teacher.R
import com.instructure.pandautils.blueprint.*

object RecyclerViewUtils {
    fun buildRecyclerView(
        rootView: View,
        context: Context,
        recyclerAdapter: SyncRecyclerAdapter<*, *, *>?,
        presenter: SyncPresenter<*, *>,
        swipeToRefreshLayoutResId: Int,
        recyclerViewResId: Int,
        emptyViewResId: Int,
        emptyViewText: String?
    ): RecyclerView {
        val emptyInterface: EmptyInterface = rootView.findViewById<View>(emptyViewResId) as EmptyInterface
        val recyclerView: RecyclerView = rootView.findViewById(recyclerViewResId)
        emptyViewText?.let { emptyInterface.setTitleText(it) }
        emptyInterface.setNoConnectionText(context.getString(R.string.noConnection))
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = RecyclerView.VERTICAL
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = recyclerAdapter
        val swipeRefreshLayout: SwipeRefreshLayout = rootView.findViewById(swipeToRefreshLayoutResId)
        swipeRefreshLayout.setOnRefreshListener {
            if (!Utils.isNetworkAvailable(context)) {
                swipeRefreshLayout.isRefreshing = false
            } else {
                presenter.refresh(true)
            }
        }
        return recyclerView
    }

    fun buildRecyclerView(
        context: Context,
        recyclerAdapter: ListRecyclerAdapter<*, *, *>?,
        presenter: ListPresenter<*, *>,
        swipeRefreshLayout: SwipeRefreshLayout,
        recyclerView: RecyclerView,
        emptyInterface: EmptyInterface,
        emptyViewText: String?
    ): RecyclerView {
        emptyViewText?.let { emptyInterface.setTitleText(it) }
        emptyInterface.setNoConnectionText(context.getString(R.string.noConnection))
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = RecyclerView.VERTICAL
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = recyclerAdapter
        swipeRefreshLayout.setOnRefreshListener {
            if (!Utils.isNetworkAvailable(context)) {
                swipeRefreshLayout.isRefreshing = false
            } else {
                presenter.refresh(true)
            }
        }
        return recyclerView
    }

    fun buildRecyclerView(
        rootView: View,
        context: Context,
        recyclerAdapter: SyncExpandableRecyclerAdapter<*, *, *, *>?,
        presenter: SyncExpandablePresenter<*, *, *>,
        swipeToRefreshLayoutResId: Int,
        recyclerViewResId: Int,
        emptyViewResId: Int,
        emptyViewText: String?
    ): RecyclerView {
        val emptyInterface: EmptyInterface = rootView.findViewById<View>(emptyViewResId) as EmptyInterface
        val recyclerView: RecyclerView = rootView.findViewById(recyclerViewResId)
        emptyViewText?.let { emptyInterface.setTitleText(it) }
        emptyInterface.setNoConnectionText(context.getString(R.string.noConnection))
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = RecyclerView.VERTICAL
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = recyclerAdapter
        val swipeRefreshLayout: SwipeRefreshLayout = rootView.findViewById(swipeToRefreshLayoutResId)
        swipeRefreshLayout.setOnRefreshListener {
            if (!Utils.isNetworkAvailable(context)) {
                swipeRefreshLayout.isRefreshing = false
            } else {
                presenter.refresh(true)
            }
        }
        return recyclerView
    }

    fun checkIfEmpty(
        emptyPandaView: EmptyInterface?,
        recyclerView: RecyclerView?,
        swipeRefreshLayout: SwipeRefreshLayout,
        adapter: SyncRecyclerAdapter<*, *, *>?,
        isEmpty: Boolean
    ) {
        if (emptyPandaView != null && adapter != null && recyclerView != null) {
            if (adapter.size() == 0 && !swipeRefreshLayout.isRefreshing) {
                recyclerView.visibility = View.GONE
                emptyPandaView.setVisibility(View.VISIBLE)
                if (isEmpty) {
                    emptyPandaView.setListEmpty()
                } else {
                    emptyPandaView.setLoading()
                }
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyPandaView.setVisibility(View.GONE)
            }
        }
    }

    fun checkIfEmpty(
        emptyPandaView: EmptyInterface?,
        recyclerView: RecyclerView?,
        swipeRefreshLayout: SwipeRefreshLayout,
        adapter: ListRecyclerAdapter<*, *, *>?,
        isEmpty: Boolean
    ) {
        if (emptyPandaView != null && adapter != null && recyclerView != null) {
            if (adapter.size() == 0 && !swipeRefreshLayout.isRefreshing) {
                recyclerView.visibility = View.GONE
                emptyPandaView.setVisibility(View.VISIBLE)
                if (isEmpty) {
                    emptyPandaView.setListEmpty()
                } else {
                    emptyPandaView.setLoading()
                }
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyPandaView.setVisibility(View.GONE)
            }
        }
    }

    fun checkIfEmpty(
        emptyPandaView: EmptyInterface?,
        recyclerView: RecyclerView?,
        swipeRefreshLayout: SwipeRefreshLayout,
        adapter: SyncExpandableRecyclerAdapter<*, *, *, *>?,
        isEmpty: Boolean
    ) {
        if (emptyPandaView != null && adapter != null && recyclerView != null) {
            if (adapter.size() == 0 && !swipeRefreshLayout.isRefreshing) {
                recyclerView.visibility = View.GONE
                emptyPandaView.setVisibility(View.VISIBLE)
                if (isEmpty) {
                    emptyPandaView.setListEmpty()
                } else {
                    emptyPandaView.setLoading()
                }
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyPandaView.setVisibility(View.GONE)
            }
        }
    }
}

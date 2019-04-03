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

package com.instructure.student.interfaces

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View

import com.instructure.pandarecycler.BaseRecyclerAdapter
import com.instructure.pandarecycler.PandaRecyclerView

interface ConfigureRecyclerView {

    //As Grid
    fun configureRecyclerViewAsGrid(
            rootView: View,
            baseRecyclerAdapter: BaseRecyclerAdapter<*>,
            swipeRefreshLayoutResId: Int,
            emptyViewResId: Int,
            recyclerViewResId: Int)

    fun configureRecyclerViewAsGrid(
            rootView: View,
            baseRecyclerAdapter: BaseRecyclerAdapter<*>,
            swipeRefreshLayoutResId: Int,
            emptyViewResId: Int,
            recyclerViewResId: Int,
            emptyViewStringResId: Int,
            span: Int)

    fun configureRecyclerViewAsGrid(
            rootView: View,
            baseRecyclerAdapter: BaseRecyclerAdapter<*>,
            swipeRefreshLayoutResId: Int,
            emptyViewResId: Int,
            recyclerViewResId: Int,
            emptyViewStringResId: Int,
            vararg emptyImage: Drawable)

    fun configureRecyclerViewAsGrid(
            rootView: View,
            baseRecyclerAdapter: BaseRecyclerAdapter<*>,
            swipeRefreshLayoutResId: Int,
            emptyViewResId: Int,
            recyclerViewResId: Int,
            emptyViewStringResId: Int,
            emptyImageClickListener: View.OnClickListener?,
            vararg emptyImage: Drawable)

    fun configureRecyclerViewAsGrid(
            rootView: View,
            baseRecyclerAdapter: BaseRecyclerAdapter<*>,
            swipeRefreshLayoutResId: Int,
            emptyViewResId: Int,
            recyclerViewResId: Int,
            emptyViewStringResId: Int,
            span: Int,
            emptyImageClickListener: View.OnClickListener?,
            vararg emptyImage: Drawable)

    //As List
    fun configureRecyclerView(
            rootView: View,
            context: Context,
            baseRecyclerAdapter: BaseRecyclerAdapter<*>,
            swipeRefreshLayoutResId: Int,
            emptyViewResId: Int,
            recyclerViewResId: Int,
            emptyViewString: String,
            withDivider: Boolean): PandaRecyclerView

    fun configureRecyclerView(
            rootView: View,
            context: Context,
            baseRecyclerAdapter: BaseRecyclerAdapter<*>,
            swipeRefreshLayoutResId: Int,
            emptyViewResId: Int,
            recyclerViewResId: Int,
            withDividers: Boolean): PandaRecyclerView

    fun configureRecyclerView(
            rootView: View,
            context: Context,
            baseRecyclerAdapter: BaseRecyclerAdapter<*>,
            swipeRefreshLayoutResId: Int,
            emptyViewResId: Int,
            recyclerViewResId: Int,
            emptyViewString: String): PandaRecyclerView

    fun configureRecyclerView(
            rootView: View,
            context: Context,
            baseRecyclerAdapter: BaseRecyclerAdapter<*>,
            swipeRefreshLayoutResId: Int,
            emptyViewResId: Int,
            recyclerViewResId: Int,
            emptyViewStringResId: Int): PandaRecyclerView

    fun configureRecyclerView(
            rootView: View,
            context: Context,
            baseRecyclerAdapter: BaseRecyclerAdapter<*>,
            swipeRefreshLayoutResId: Int,
            emptyViewResId: Int,
            recyclerViewResId: Int): PandaRecyclerView
}

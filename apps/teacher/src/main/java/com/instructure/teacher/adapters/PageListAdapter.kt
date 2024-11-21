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

package com.instructure.teacher.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.instructure.canvasapi2.models.Page
import com.instructure.teacher.databinding.AdapterPageBinding
import com.instructure.teacher.holders.PageViewHolder
import com.instructure.teacher.presenters.PageListPresenter
import com.instructure.teacher.viewinterface.PageListView
import com.instructure.pandautils.blueprint.SyncRecyclerAdapter

class PageListAdapter(
    context: Context,
    presenter: PageListPresenter,
    private val iconColor: Int,
    private val mCallback: (Page) -> Unit
) : SyncRecyclerAdapter<Page, PageViewHolder, PageListView>(context, presenter) {

    override fun createViewHolder(binding: ViewBinding, viewType: Int) = PageViewHolder(binding as AdapterPageBinding)

    override fun bindingInflater(viewType: Int): (LayoutInflater, ViewGroup, Boolean) -> ViewBinding = AdapterPageBinding::inflate

    override fun bindHolder(model: Page, holder: PageViewHolder, position: Int) {
        context?.let { holder.bind(it, model, iconColor, mCallback) }
    }
}

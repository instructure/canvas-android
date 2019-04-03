/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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
package com.instructure.parentapp.adapter

import android.content.Context
import android.view.View

import com.instructure.canvasapi2.models.ObserverAlert
import com.instructure.parentapp.binders.AlertBinder
import com.instructure.parentapp.holders.AlertViewHolder
import com.instructure.parentapp.interfaces.AdapterToFragmentBadgeCallback
import com.instructure.parentapp.presenters.AlertPresenter

import instructure.androidblueprint.SyncRecyclerAdapter

class AlertListRecyclerAdapter(
        context: Context,
        presenter: AlertPresenter,
        private val adapterToFragmentCallback: AdapterToFragmentBadgeCallback<ObserverAlert>,
        private val mItemDismissedInterface: ItemDismissedInterface) : SyncRecyclerAdapter<ObserverAlert, AlertViewHolder>(context, presenter) {


    interface ItemDismissedInterface {
        fun itemDismissed(item: ObserverAlert, holder: AlertViewHolder)
    }

    //Sync

    override fun bindHolder(alert: ObserverAlert, holder: AlertViewHolder, position: Int) {
        AlertBinder.bind(context!!, alert, holder, adapterToFragmentCallback, mItemDismissedInterface)
    }

    override fun createViewHolder(v: View, viewType: Int): AlertViewHolder = AlertViewHolder(v)
    override fun itemLayoutResId(viewType: Int): Int = AlertViewHolder.holderResId()
}

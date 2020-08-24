/*
 * Copyright (C) 2019 - present  Instructure, Inc.
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

package com.instructure.teacher.adapters

import android.content.Context
import android.view.View
import com.instructure.canvasapi2.models.Conversation
import com.instructure.teacher.holders.InboxViewHolder
import com.instructure.teacher.interfaces.AdapterToFragmentCallback
import com.instructure.teacher.presenters.InboxPresenter
import com.instructure.teacher.viewinterface.InboxView
import instructure.androidblueprint.SyncRecyclerAdapter

class InboxAdapter(
    context: Context,
    presenter: InboxPresenter,
    private val mCallback: AdapterToFragmentCallback<Conversation>
) : SyncRecyclerAdapter<Conversation, InboxViewHolder, InboxView>(context, presenter) {

    override fun bindHolder(conversation: Conversation, holder: InboxViewHolder, position: Int) {
        holder.bind(conversation, mCallback)
    }

    override fun createViewHolder(v: View, viewType: Int): InboxViewHolder {
        return InboxViewHolder(v)
    }

    override fun itemLayoutResId(viewType: Int) = InboxViewHolder.holderResId()
}

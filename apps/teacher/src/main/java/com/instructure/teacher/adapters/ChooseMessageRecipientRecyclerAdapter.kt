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

package com.instructure.teacher.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.instructure.canvasapi2.models.Recipient
import com.instructure.teacher.databinding.ViewholderRecipientBinding
import com.instructure.teacher.holders.RecipientViewHolder
import com.instructure.teacher.interfaces.RecipientAdapterCallback
import com.instructure.teacher.presenters.ChooseRecipientsPresenter
import com.instructure.teacher.viewinterface.ChooseRecipientsView
import instructure.androidblueprint.SyncRecyclerAdapter

class ChooseMessageRecipientRecyclerAdapter(
    context: Context,
    presenter: ChooseRecipientsPresenter,
    private val mAdapterCallback: RecipientAdapterCallback
) : SyncRecyclerAdapter<Recipient, RecipientViewHolder, ChooseRecipientsView>(context, presenter) {

    override fun bindHolder(recipient: Recipient, holder: RecipientViewHolder, position: Int) {
        holder.bind(
            context!!,
            holder,
            recipient,
            mAdapterCallback,
            mAdapterCallback.isRecipientSelected(recipient),
            binding as ViewholderRecipientBinding
        )
    }

    override fun createViewHolder(v: View, viewType: Int): RecipientViewHolder = RecipientViewHolder(v)

    override fun bindingInflater(viewType: Int): (LayoutInflater, ViewGroup, Boolean) -> ViewBinding = ViewholderRecipientBinding::inflate
}

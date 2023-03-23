/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.pandautils.features.inbox.list.itemviewmodels

import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.instructure.pandautils.BR
import com.instructure.pandautils.R
import com.instructure.pandautils.features.inbox.list.InboxEntryViewData
import com.instructure.pandautils.mvvm.ItemViewModel

class InboxEntryItemViewModel(
    @get:Bindable
    var data: InboxEntryViewData,
    val openConversationCallback: (Boolean, Boolean) -> Unit,
    val selectionModeCallback: (View, Boolean) -> Unit,
    val avatarClickedCallback: (Boolean) -> Unit,
    var selectionModeActive: Boolean = false,
    @get:Bindable
    var selected: Boolean = false
) : ItemViewModel, BaseObservable() {

    override val layoutId: Int = R.layout.item_inbox_entry

    fun onClick(view: View) {
        if (selectionModeActive) {
            changeSelection(view)
        } else {
            openConversationCallback(data.starred, data.unread)
        }
    }

    fun onAvatarClick(view: View) {
        if (selectionModeActive) {
            changeSelection(view)
        } else {
            avatarClickedCallback(data.starred)
        }
    }

    fun onLongClick(view: View): Boolean {
        changeSelection(view)
        return true
    }

    private fun changeSelection(view: View) {
        selected = !selected
        notifyPropertyChanged(BR.selected)
        selectionModeCallback(view, selected)
    }

    override fun areContentsTheSame(other: ItemViewModel): Boolean {
        if (other is InboxEntryItemViewModel) {
            return data.id == other.data.id
        }

        return false
    }

    override fun areItemsTheSame(other: ItemViewModel): Boolean {
        return other === this
    }
}
/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.student.mobius.conferences.conference_list.ui

import com.instructure.pandautils.adapters.BasicItemBinder
import com.instructure.pandautils.adapters.BasicItemCallback
import com.instructure.pandautils.adapters.BasicRecyclerAdapter
import com.instructure.pandautils.utils.asStateList
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setTextForVisibility
import com.instructure.student.R
import com.instructure.student.databinding.AdapterConferenceHeaderBinding
import com.instructure.student.databinding.AdapterConferenceItemBinding
import com.instructure.student.databinding.AdapterConferenceListErrorBinding
import com.instructure.student.mobius.conferences.conference_list.ConferenceHeaderType

interface ConferenceListAdapterCallback : BasicItemCallback {
    fun onConferenceClicked(conferenceId: Long)
    fun reload()
    fun onHeaderClicked(headerType: ConferenceHeaderType)
}

class ConferenceListAdapter(callback: ConferenceListAdapterCallback) :
    BasicRecyclerAdapter<ConferenceListItemViewState, ConferenceListAdapterCallback>(callback) {
    override fun registerBinders() {
        register(ConferenceListEmptyBinder())
        register(ConferenceListErrorBinder())
        register(ConferenceListHeaderBinder())
        register(ConferenceListItemBinder())
    }
}

class ConferenceListEmptyBinder : BasicItemBinder<ConferenceListItemViewState.Empty, ConferenceListAdapterCallback>() {
    // TODO: Get correct image and messaging for empty view
    override val layoutResId = R.layout.adapter_conference_list_empty
    override val bindBehavior = NoBind()
}

class ConferenceListErrorBinder : BasicItemBinder<ConferenceListItemViewState.Error, ConferenceListAdapterCallback>() {
    // TODO: Get correct image and messaging for error view
    override val layoutResId = R.layout.adapter_conference_list_error
    override val bindBehavior = Item {_, callback, _ ->
        val binding = AdapterConferenceListErrorBinding.bind(this)
        binding.conferenceListRetry.onClick { callback.reload() }
    }
}

class ConferenceListHeaderBinder : BasicItemBinder<ConferenceListItemViewState.ConferenceHeader, ConferenceListAdapterCallback>() {
    override val layoutResId = R.layout.adapter_conference_header
    override val bindBehavior = Item {data, callback, _ ->
        val binding = AdapterConferenceHeaderBinding.bind(this)
        binding.title.text = data.title

        binding.expandIcon.animate()
            .rotation(if (data.isExpanded) 180f else 0f)
            .setDuration(200)
            .start()

        binding.headerContainer.onClick {
            callback.onHeaderClicked(data.headerType)
        }
    }
}

class ConferenceListItemBinder : BasicItemBinder<ConferenceListItemViewState.ConferenceItem, ConferenceListAdapterCallback>() {
    override val layoutResId = R.layout.adapter_conference_item
    override val bindBehavior = Item { data, callback, _ ->
        val binding = AdapterConferenceItemBinding.bind(this)
        with (binding) {
            icon.imageTintList = data.tint.asStateList()
            title.text = data.title
            subtitle.setTextForVisibility(data.subtitle)

            statusLabel.text = data.label
            statusLabel.setTextColor(data.labelTint)

            onClick { callback.onConferenceClicked(data.conferenceId) }
        }
    }
}

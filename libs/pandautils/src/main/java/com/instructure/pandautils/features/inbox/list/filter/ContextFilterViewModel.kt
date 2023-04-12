/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
package com.instructure.pandautils.features.inbox.list.filter

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.R
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.utils.isCourse
import com.instructure.pandautils.utils.isGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ContextFilterViewModel @Inject constructor(
    private val resources: Resources
) : ViewModel() {

    val itemViewModels: LiveData<List<ItemViewModel>>
        get() = _itemViewModels
    private val _itemViewModels = MutableLiveData<List<ItemViewModel>>(emptyList())

    val events: LiveData<Event<Long>>
        get() = _events
    private val _events = MutableLiveData<Event<Long>>()

    fun setFilterItems(canvasContexts: List<CanvasContext>) {
        val courses = canvasContexts.filter { it.isCourse }
        val groups = canvasContexts.filter { it.isGroup }

        val items = mutableListOf<ItemViewModel>()
        if (courses.isNotEmpty()) {
            items.add(ContextFilterHeaderItemViewModel(resources.getString(R.string.courses)))
            items.addAll(
                courses.map {
                    ContextFilterItemViewModel(it.name ?: "", it.id) {
                        _events.value = Event(it)
                    }
                }
            )
        }
        if (groups.isNotEmpty()) {
            items.add(ContextFilterHeaderItemViewModel(resources.getString(R.string.groups)))
            items.addAll(
                groups.map {
                    ContextFilterItemViewModel(it.name ?: "", it.id) {
                        _events.value = Event(it)
                    }
                }
            )
        }

        _itemViewModels.value = items
    }
}

enum class ContextFilterItemViewType(val viewType: Int) {
    FILTER_ITEM(0),
    HEADER(1)
}
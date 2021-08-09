/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.elementary.schedule.pager

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.features.elementary.schedule.ScheduleFragment
import com.instructure.pandautils.mvvm.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject


const val SCHEDULE_PAGE_COUNT = 53
const val THIS_WEEKS_POSITION = 27

@HiltViewModel
class SchedulePagerViewModel @Inject constructor() : ViewModel() {

    val data: LiveData<SchedulePagerViewData>
        get() = _data
    private val _data = MutableLiveData<SchedulePagerViewData>()

    val events: LiveData<Event<SchedulePagerAction>>
        get() = _events
    private val _events = MutableLiveData<Event<SchedulePagerAction>>()

    val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
        }
    }

    init {
        val calendar = Calendar.getInstance()
        calendar.roll(Calendar.WEEK_OF_YEAR, -28)
        val fragments = (0..SCHEDULE_PAGE_COUNT).map {
            calendar.roll(Calendar.WEEK_OF_YEAR, true)
            ScheduleFragment.newInstance(calendar.time.toApiString())
        }

        _data.postValue(SchedulePagerViewData(fragments))
        _events.postValue(Event(SchedulePagerAction.SelectPage(THIS_WEEKS_POSITION)))
    }

    fun onPreviousWeekClick() {
        _events.postValue(Event(SchedulePagerAction.MoveToPrevious))
    }

    fun onNextWeekClick() {
        _events.postValue(Event(SchedulePagerAction.MoveToNext))
    }
}
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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.instructure.pandautils.databinding.FragmentSchedulePagerBinding
import com.instructure.pandautils.features.elementary.schedule.ScheduleFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_schedule_pager.*

@AndroidEntryPoint
class SchedulePagerFragment : Fragment() {

    private val viewModel: SchedulePagerViewModel by viewModels()

    private val todayButtonLiveData = MutableLiveData<Boolean>()

    private var todayFragment: ScheduleFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSchedulePagerBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.data.observe(viewLifecycleOwner, { schedulePagerViewData ->
            schedulePagerViewData?.let {
                schedulePager.adapter = object : FragmentStateAdapter(this) {
                    override fun getItemCount(): Int = it.pageStartDates.size

                    override fun createFragment(position: Int): Fragment {
                        val fragment = ScheduleFragment.newInstance(it.pageStartDates[position])
                        if (position == THIS_WEEKS_POSITION) {
                            todayFragment = fragment
                        }
                        return fragment
                    }

                }
            }
        })

        viewModel.events.observe(viewLifecycleOwner, { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        })

        schedulePager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position != THIS_WEEKS_POSITION) {
                    setTodayButtonVisibility(true)
                }

                if (position == 0) {
                    previousWeekButton.visibility = View.GONE
                } else if (position == schedulePager.childCount - 1) {
                    nextWeekButton.visibility = View.GONE
                } else {
                    previousWeekButton.visibility = View.VISIBLE
                    nextWeekButton.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun handleAction(action: SchedulePagerAction) {
        when (action) {
            is SchedulePagerAction.SelectPage -> schedulePager.setCurrentItem(action.position, false)
            is SchedulePagerAction.MoveToNext -> schedulePager.setCurrentItem(schedulePager.currentItem + 1, true)
            is SchedulePagerAction.MoveToPrevious -> schedulePager.setCurrentItem(schedulePager.currentItem - 1, true)
        }
    }

    fun getTodayButtonVisibility(): LiveData<Boolean> {
        return todayButtonLiveData
    }

    fun setTodayButtonVisibility(visible: Boolean) {
        todayButtonLiveData.postValue(visible)
    }

    fun jumpToToday() {
        schedulePager.setCurrentItem(THIS_WEEKS_POSITION, true)
        todayFragment?.jumpToToday()
    }

    companion object {
        fun newInstance() = SchedulePagerFragment()
    }
}
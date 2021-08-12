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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.instructure.pandautils.databinding.FragmentSchedulePagerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_schedule_pager.*
import kotlin.math.roundToInt

@AndroidEntryPoint
class SchedulePagerFragment : Fragment() {

    private val viewModel: SchedulePagerViewModel by viewModels()

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

        viewModel.data.observe(viewLifecycleOwner, {
            it?.let {
                schedulePager.adapter = object : FragmentStateAdapter(this) {
                    override fun getItemCount(): Int = it.fragments.size

                    override fun createFragment(position: Int): Fragment = it.fragments[position]

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

    companion object {
        fun newInstance() = SchedulePagerFragment()
    }
}
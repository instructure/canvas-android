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
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.databinding.FragmentSchedulePagerBinding
import com.instructure.pandautils.features.elementary.schedule.ScheduleFragment
import com.instructure.pandautils.utils.isAccessibilityEnabled
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SchedulePagerFragment : BaseCanvasFragment() {

    private val viewModel: SchedulePagerViewModel by viewModels()

    private val todayButtonLiveData = MutableLiveData<Boolean>()

    private var todayFragment: ScheduleFragment? = null

    private lateinit var binding: FragmentSchedulePagerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSchedulePagerBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Consume bottom insets for schedulePager so child fragments don't apply them
        // The controls section handles bottom spacing
        ViewCompat.setOnApplyWindowInsetsListener(binding.schedulePager) { view, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            WindowInsetsCompat.Builder(insets)
                .setInsets(
                    WindowInsetsCompat.Type.navigationBars(),
                    androidx.core.graphics.Insets.of(
                        navigationBars.left,
                        navigationBars.top,
                        navigationBars.right,
                        0 // Consume bottom insets
                    )
                )
                .build()
        }

        // Apply bottom system bar insets to controls (Previous/Next Week buttons)
        ViewCompat.setOnApplyWindowInsetsListener(binding.controls) { view, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.updatePadding(bottom = navigationBars.bottom)
            insets
        }

        viewModel.data.observe(viewLifecycleOwner, { schedulePagerViewData ->
            schedulePagerViewData?.let {
                binding.schedulePager.adapter = object : FragmentStateAdapter(this@SchedulePagerFragment) {
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

        binding.schedulePager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position != THIS_WEEKS_POSITION) {
                    setTodayButtonVisibility(true)
                }

                if (position == 0) {
                    binding.previousWeekButton.visibility = View.GONE
                } else if (position == binding.schedulePager.childCount - 1) {
                    binding.nextWeekButton.visibility = View.GONE
                } else {
                    binding.previousWeekButton.visibility = View.VISIBLE
                    binding.nextWeekButton.visibility = View.VISIBLE
                }
            }
        })

        if (isAccessibilityEnabled(requireContext())) {
            movePagerControlToTop()
        }
    }

    private fun movePagerControlToTop() = with(binding) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(schedulePage)
        constraintSet.clear(controls.id, ConstraintSet.BOTTOM)
        constraintSet.clear(schedulePager.id, ConstraintSet.BOTTOM)
        constraintSet.clear(schedulePager.id, ConstraintSet.TOP)
        constraintSet.connect(controls.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet.connect(schedulePager.id, ConstraintSet.TOP, controls.id, ConstraintSet.BOTTOM)
        constraintSet.connect(schedulePager.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraintSet.applyTo(schedulePage)
    }

    private fun handleAction(action: SchedulePagerAction) = with(binding) {
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
        binding.schedulePager.setCurrentItem(THIS_WEEKS_POSITION, true)
        todayFragment?.jumpToToday()
    }

    companion object {
        fun newInstance() = SchedulePagerFragment()
    }
}
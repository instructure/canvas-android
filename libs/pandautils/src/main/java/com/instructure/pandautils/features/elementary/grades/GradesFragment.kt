/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.pandautils.features.elementary.grades

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.instructure.pandautils.base.BaseCanvasFragment
import androidx.fragment.app.viewModels
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrl
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_K5_GRADES
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.databinding.FragmentGradesBinding
import com.instructure.pandautils.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@PageView
@ScreenView(SCREEN_VIEW_K5_GRADES)
@AndroidEntryPoint
class GradesFragment : BaseCanvasFragment() {

    @Inject
    lateinit var gradesRouter: GradesRouter

    private val viewModel: GradesViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentGradesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.events.observe(viewLifecycleOwner, { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        })

        return binding.root
    }

    private fun handleAction(action: GradesAction) {
        when (action) {
            is GradesAction.OpenCourseGrades -> gradesRouter.openCourseGrades(action.course)
            is GradesAction.OpenGradingPeriodsDialog -> showGradingPeriodsDialog(action)
            GradesAction.ShowGradingPeriodError -> toast(R.string.failedToLoadGradesForGradingPeriod)
            GradesAction.ShowRefreshError -> toast(R.string.failedToRefreshGrades)
        }
    }

    private fun showGradingPeriodsDialog(action: GradesAction.OpenGradingPeriodsDialog) {
        val gradingPeriodNames = action.gradingPeriods
            .map { it.name }
            .toTypedArray()

        AlertDialog.Builder(requireContext(), R.style.AccentDialogTheme)
            .setTitle(R.string.selectGradingPeriod)
            .setSingleChoiceItems(gradingPeriodNames, action.selectedGradingPeriodIndex) { dialog, which -> sortOrderSelected(dialog, which, action.gradingPeriods) }
            .setNegativeButton(R.string.sortByDialogCancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun sortOrderSelected(dialog: DialogInterface?, index: Int, gradingPeriods: List<GradingPeriod>) {
        dialog?.dismiss()
        val selectedGradingPeriod = gradingPeriods[index]
        viewModel.gradingPeriodSelected(selectedGradingPeriod)
    }

    @PageViewUrl
    fun makePageViewUrl() = "${ApiPrefs.fullDomain}#grades"

    companion object {
        fun newInstance(): GradesFragment {
            return GradesFragment()
        }
    }
}
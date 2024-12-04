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


package com.instructure.pandautils.features.dashboard.edit

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.pandautils.base.BaseCanvasFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.router.Route
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_EDIT_DASHBOARD
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.databinding.FragmentEditDashboardBinding
import com.instructure.pandautils.utils.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@PageView(url = "courses")
@ScreenView(SCREEN_VIEW_EDIT_DASHBOARD)
@AndroidEntryPoint
class EditDashboardFragment : BaseCanvasFragment() {

    private val viewModel: EditDashboardViewModel by viewModels()

    @Inject
    lateinit var editDashboardRouter: EditDashboardRouter

    private lateinit var binding: FragmentEditDashboardBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentEditDashboardBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.events.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        setupToolbar()
    }

    private fun setupToolbar() {
        binding.toolbar.setTitle(R.string.allCoursesScreenHeader)
        binding.toolbar.setupAsBackButton(this)
        binding.toolbar.addSearch(color = ThemePrefs.primaryTextColor) {
            viewModel.queryItems(it)
        }
        ViewStyler.themeToolbarColored(requireActivity(), binding.toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
    }

    override fun onStop() {
        super.onStop()

        if (viewModel.hasChanges) {
            val intent = Intent(Const.COURSE_THING_CHANGED)
            intent.putExtras(Bundle().apply { putBoolean(Const.COURSE_FAVORITES, true) })
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }
    }

    private fun handleAction(action: EditDashboardItemAction) {
        when (action) {
            is EditDashboardItemAction.OpenItem -> {
                editDashboardRouter.routeCourse(action.canvasContext)
            }
            is EditDashboardItemAction.ShowSnackBar -> {
                Snackbar.make(requireView(), action.res, Snackbar.LENGTH_LONG).show()
                view?.announceForAccessibility(requireContext().getString(action.res))
            }
            else -> {}
        }
    }

    companion object {

        fun makeRoute() = Route(EditDashboardFragment::class.java, null)

        fun validRoute(route: Route) = route.primaryClass == EditDashboardFragment::class.java

        fun newInstance(route: Route): EditDashboardFragment? {
            if (!validRoute(route)) return null
            return EditDashboardFragment()
        }

    }

}
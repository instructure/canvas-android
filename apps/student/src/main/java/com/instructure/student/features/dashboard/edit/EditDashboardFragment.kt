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


package com.instructure.student.features.dashboard.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import com.instructure.interactions.router.Route
import com.instructure.student.databinding.FragmentEditDashboardBinding
import com.instructure.student.fragment.CourseBrowserFragment
import com.instructure.student.router.RouteMatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_edit_dashboard.*

@AndroidEntryPoint
class EditDashboardFragment : Fragment() {

    companion object {

        fun makeRoute() = Route(EditDashboardFragment::class.java, null)

        fun validRoute(route: Route) = route.primaryClass == EditDashboardFragment::class.java

        fun newInstance(route: Route): EditDashboardFragment? {
            if (!validRoute(route)) return null
            return EditDashboardFragment()
        }

    }

    private val viewModel: EditDashboardViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = FragmentEditDashboardBinding.inflate(inflater, container, false)
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

        recyclerView.apply {
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
    }

    private fun handleAction(action: EditDashboardItemAction) {
        when (action) {
            is EditDashboardItemAction.OpenItem -> {
                RouteMatcher.route(requireContext(), CourseBrowserFragment.makeRoute(action.canvasContext))
            }
        }
    }

}
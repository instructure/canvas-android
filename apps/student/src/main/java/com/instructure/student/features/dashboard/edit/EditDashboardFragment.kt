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
import com.instructure.interactions.router.Route
import com.instructure.student.databinding.FragmentEdigDashboardBinding
import dagger.hilt.android.AndroidEntryPoint

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
        val binding = FragmentEdigDashboardBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        return binding.root
    }

}
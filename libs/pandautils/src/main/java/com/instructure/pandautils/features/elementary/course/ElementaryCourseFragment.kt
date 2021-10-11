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

package com.instructure.pandautils.features.elementary.course

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.interactions.router.Route
import com.instructure.pandautils.R
import com.instructure.pandautils.databinding.FragmentElementaryCourseBinding
import com.instructure.pandautils.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_elementary_course.*

@AndroidEntryPoint
class ElementaryCourseFragment : Fragment() {

    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)

    private val viewModel: ElementaryCourseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentElementaryCourseBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyTheme()
        viewModel.getData(canvasContext)
    }

    companion object {
        fun newInstance(route: Route) =
            if (validateRoute(route)) ElementaryCourseFragment().apply {
                arguments = route.canvasContext!!.makeBundle(route.arguments)
            } else null

        private fun validateRoute(route: Route) = route.canvasContext != null

        fun makeRoute(canvasContext: CanvasContext?) = Route(ElementaryCourseFragment::class.java, canvasContext)
    }

    private fun applyTheme() {
        toolbar.title = canvasContext.name
        toolbar.setupAsBackButton(this)
        ViewStyler.themeToolbar(requireActivity(), toolbar, canvasContext)
    }
}
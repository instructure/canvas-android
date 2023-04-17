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

package com.instructure.pandautils.features.offline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.interactions.router.Route
import com.instructure.pandautils.R
import com.instructure.pandautils.databinding.FragmentOfflineContentBinding
import com.instructure.pandautils.utils.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OfflineContentFragment : Fragment(), FragmentInteractions {

    private val viewModel: OfflineContentViewModel by viewModels()

    private var canvasContext: CanvasContext? by NullableParcelableArg(key = Const.CANVAS_CONTEXT)

    private lateinit var binding: FragmentOfflineContentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOfflineContentBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@OfflineContentFragment
            viewModel = this@OfflineContentFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyTheme()
    }

    override val navigation: Navigation?
        get() = activity as? Navigation

    override fun title(): String = getString(R.string.offline_content_toolbar_title)

    override fun applyTheme() {
        ViewStyler.themeToolbarColored(requireActivity(), binding.toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
        binding.toolbar.apply {
            subtitle = canvasContext?.name ?: getString(R.string.offline_content_all_courses)
            setBackgroundColor(ThemePrefs.primaryColor)
            setupAsBackButton(this@OfflineContentFragment)
        }
    }

    override fun getFragment(): Fragment = this

    companion object {

        fun makeRoute(canvasContext: CanvasContext? = null) = Route(OfflineContentFragment::class.java, canvasContext)

        private fun validRoute(route: Route) = route.primaryClass == OfflineContentFragment::class.java

        fun newInstance(route: Route) = if (validRoute(route)) OfflineContentFragment().withArgs(route.argsWithContext) else null
    }
}
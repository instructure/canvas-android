/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.features.smartsearch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_SMART_SEARCH
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.makeBundle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

val QUERY = "query"

@PageView(url = "{canvasContext}/smartsearch")
@ScreenView(SCREEN_VIEW_SMART_SEARCH)
@AndroidEntryPoint
class SmartSearchFragment : BaseCanvasFragment() {

    @Inject
    lateinit var router: SmartSearchRouter

    private val viewModel: SmartSearchViewModel by viewModels()

    @get:PageViewUrlParam("canvasContext")
    var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ThemePrefs.reapplyCanvasTheme(requireActivity())
        ViewStyler.setStatusBarDark(requireActivity(), canvasContext.color)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                SmartSearchScreen(uiState) {
                    requireActivity().onBackPressed()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.collectOneOffEvents(viewModel.events, ::handleActions)
    }

    private fun handleActions(action: SmartSearchViewModelAction) {
        when (action) {
            is SmartSearchViewModelAction.Route -> {
                router.route(action.url)
            }
        }
    }

    companion object {

        fun newInstance(route: Route): SmartSearchFragment {
            return SmartSearchFragment().apply {
                arguments = route.canvasContext!!.makeBundle(route.arguments)
            }
        }

        fun makeRoute(canvasContext: CanvasContext, query: String) =
            Route(SmartSearchFragment::class.java, canvasContext, bundleOf(QUERY to query))
    }
}
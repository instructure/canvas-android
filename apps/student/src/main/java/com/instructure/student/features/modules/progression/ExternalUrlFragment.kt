/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.student.features.modules.progression

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.interactions.router.Route
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.features.modules.externalurl.ExternalUrlScreen
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.NullableStringArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.launchCustomTab
import com.instructure.student.fragment.ParentFragment

class ExternalUrlFragment : ParentFragment() {

    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)
    private var itemTitle: String by StringArg(key = Const.ACTION_BAR_TITLE)
    private var externalUrl: String? by NullableStringArg(key = Const.URL)

    override fun title(): String = itemTitle

    override fun applyTheme() {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CanvasTheme {
                    ExternalUrlScreen(
                        title = itemTitle,
                        onNavigationClick = { requireActivity().onBackPressedDispatcher.onBackPressed() },
                        courseColor = Color(canvasContext.color),
                        onOpenClick = externalUrl?.takeIf { it.isNotBlank() }?.let { url ->
                            { requireActivity().launchCustomTab(url, canvasContext.color) }
                        }
                    )
                }
            }
        }
    }

    companion object {
        fun makeRoute(canvasContext: CanvasContext, itemTitle: String, externalUrl: String?): Route {
            val bundle = Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putString(Const.ACTION_BAR_TITLE, itemTitle)
                putString(Const.URL, externalUrl)
            }
            return Route(ExternalUrlFragment::class.java, null, bundle)
        }

        fun newInstance(route: Route) = ExternalUrlFragment().apply {
            arguments = route.arguments
        }
    }
}

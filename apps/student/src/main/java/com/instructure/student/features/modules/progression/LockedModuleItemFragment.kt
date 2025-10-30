/*
 * Copyright (C) 2016 - present Instructure, Inc.
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
import android.webkit.WebView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_LOCKED_MODULE_ITEM
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyHorizontalSystemBarInsets
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.student.R
import com.instructure.student.databinding.FragmentLockedModuleItemBinding
import com.instructure.student.fragment.ParentFragment
import com.instructure.student.router.RouteMatcher

@ScreenView(SCREEN_VIEW_LOCKED_MODULE_ITEM)
class LockedModuleItemFragment : ParentFragment() {

    private var moduleItemName: String by StringArg(key = MODULE_ITEM_NAME)
    private var lockExplanation: String by StringArg(key = LOCK_EXPLANATION)
    private var course: Course by ParcelableArg(key = Const.COURSE)

    private val binding by viewBinding(FragmentLockedModuleItemBinding::bind)

    //region Fragment Lifecycle Overrides
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_locked_module_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.applyHorizontalSystemBarInsets()
        binding.toolbar.title = moduleItemName
        setupWebView(binding.explanationWebView)
        binding.explanationWebView.loadHtml(lockExplanation, "")

        binding.toolbar.setupAsBackButton(this)
        binding.toolbar.applyTopSystemBarInsets()
        ViewStyler.themeToolbarColored(requireActivity(), binding.toolbar, course)
    }
    //endregion

    //region Fragment Interaction Overrides
    override fun title(): String = getString(R.string.locked)

    override fun applyTheme() { }

    private fun setupWebView(canvasWebView: CanvasWebView) {
        canvasWebView.settings.loadWithOverviewMode = true
        canvasWebView.settings.displayZoomControls = false
        canvasWebView.settings.setSupportZoom(true)
        canvasWebView.addVideoClient(requireActivity())
        canvasWebView.setInitialScale(100)

        canvasWebView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                RouteMatcher.openMedia(requireActivity(), url)
            }

            override fun onPageFinishedCallback(webView: WebView, url: String) = Unit

            override fun onPageStartedCallback(webView: WebView, url: String) = Unit

            override fun canRouteInternallyDelegate(url: String): Boolean = RouteMatcher.canRouteInternally(requireActivity(), url, ApiPrefs.domain, routeIfPossible = false, allowUnsupported = false)

            override fun routeInternallyCallback(url: String) {
                RouteMatcher.canRouteInternally(requireActivity(), url, ApiPrefs.domain, routeIfPossible = true, allowUnsupported = false)
            }
        }
    }
    //endregion

    companion object {
        private const val MODULE_ITEM_NAME = "module_item_name"
        private const val LOCK_EXPLANATION = "lock_explanation"

        fun makeRoute(course: CanvasContext, moduleItemName: String, lockExplanation: String): Route {
            val bundle = Bundle().apply {
                putParcelable(Const.COURSE, course)
                putString(MODULE_ITEM_NAME, moduleItemName)
                putString(LOCK_EXPLANATION, lockExplanation)
            }
            return Route(LockedModuleItemFragment::class.java, null, bundle)
        }

        fun newInstance(route: Route) = if (validRoute(route)) { LockedModuleItemFragment().apply {
                arguments = route.arguments
            }
        } else null

        private fun validRoute(route: Route) = route.arguments.containsKey(MODULE_ITEM_NAME)
    }
}

/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.student.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_UNSUPPORTED_FEATURE
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.databinding.FragmentUnsupportedFeatureBinding
import com.instructure.student.util.Analytics

@ScreenView(SCREEN_VIEW_UNSUPPORTED_FEATURE)
open class UnsupportedFeatureFragment : ParentFragment() {

    private val binding by viewBinding(FragmentUnsupportedFeatureBinding::bind)

    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)
    private var featureName by NullableStringArg(key = Const.FEATURE_NAME)
    private var unsupportedDescription by NullableStringArg(key = Const.UNSUPPORTED_DESCRIPTION)
    private var url by NullableStringArg(key = Const.URL)

    override fun title(): String = getString(R.string.unsupported)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_unsupported_feature, container, false)
    }

    override fun applyTheme() {
        with (binding) {
            toolbar.title = title()
            toolbar.setupAsBackButton(this@UnsupportedFeatureFragment)
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, canvasContext)
            initViews()
        }
    }

    private fun initViews() = with(binding) {
        // Set the text
        when {
            unsupportedDescription != null -> featureText.text = unsupportedDescription
            featureName != null -> featureText.text = String.format(getString(R.string.isNotSupportedFeature), featureName)
            else -> featureText.text = getString(R.string.isNotSupported)
        }

        if (url.isNullOrEmpty()) {
            openInBrowser.setGone()
        }

        openInBrowser.setOnClickListener {
            if (featureName != null) {
                Analytics.trackUnsupportedFeature(requireActivity(), featureName)
            } else if (url != null) {
                Analytics.trackUnsupportedFeature(requireActivity(), url)
            }

            // The last parameter needs to be true so the web page will try to authenticate
            InternalWebviewFragment.loadInternalWebView(activity,
                    InternalWebviewFragment.makeRoute(canvasContext, url!!, true, true))
        }

        ViewStyler.themeButton(openInBrowser)
    }

    companion object {

        fun makeRoute(canvasContext: CanvasContext, featureName: String? = null, unsupportedDescription: String? = null, url: String? = null): Route {
            val bundle = Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putString(Const.FEATURE_NAME, featureName)
                putString(Const.UNSUPPORTED_DESCRIPTION, unsupportedDescription)
                putString(Const.URL, url)
            }
            return Route(UnsupportedFeatureFragment::class.java, canvasContext, bundle)
        }

        private fun validRoute(route: Route): Boolean {
            return route.canvasContext != null || (route.canvasContext != null && route.arguments.getString(Const.URL) != null)
        }

        fun newInstance(route: Route): UnsupportedFeatureFragment? {
            // Set external route Uri as bundle URL
            if (route.uri != null && !route.arguments.containsKey(Const.URL)) {
                val url = route.uri.toString().replaceFirst(Regex("canvas-(courses|student)://"), "https://")
                route.arguments.putString(Const.URL, url)
            }
            if (!validRoute(route)) return null
            return UnsupportedFeatureFragment().withArgs(
                route.arguments
            )
        }
    }
}

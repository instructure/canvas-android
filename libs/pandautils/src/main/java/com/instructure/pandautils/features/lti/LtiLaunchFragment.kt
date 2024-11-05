/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.lti

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrl
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.interactions.router.Route
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_LTI_LAUNCH
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.databinding.FragmentLtiLaunchBinding
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.NullableStringArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.argsWithContext
import com.instructure.pandautils.utils.asChooserExcludingInstructure
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.pandautils.utils.setTextForVisibility
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.utils.withArgs
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder

@AndroidEntryPoint
@ScreenView(SCREEN_VIEW_LTI_LAUNCH)
@PageView
class LtiLaunchFragment : Fragment() {

    private val binding by viewBinding(FragmentLtiLaunchBinding::bind)

    private val viewModel: LtiLaunchViewModel by viewModels()

    private var title: String? by NullableStringArg(key = LTI_TITLE)
    private var ltiTab: Tab? by NullableParcelableArg(key = LTI_TAB)
    private var canvasContext: CanvasContext by ParcelableArg(default = CanvasContext.emptyUserContext(), key = Const.CANVAS_CONTEXT)

    @Suppress("unused")
    @PageViewUrl
    private fun makePageViewUrl() = ltiTab?.externalUrl ?: (ApiPrefs.fullDomain + canvasContext.toAPIString() + "/external_tools")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_lti_launch, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.loadingView.setOverrideColor(ParentPrefs.currentStudent?.studentColor ?: ThemePrefs.primaryColor) TODO Set color
        binding.toolName.setTextForVisibility(title.validOrNull())

        lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)
    }

    private fun handleAction(action: LtiLaunchAction) {
        when (action) {
            is LtiLaunchAction.LaunchCustomTab -> {
                launchCustomTab(action.url)
            }
            is LtiLaunchAction.ShowError -> {
                toast(R.string.errorOccurred)
                if (activity != null) {
                    requireActivity().onBackPressed()
                }
            }
        }
    }

    private fun launchCustomTab(url: String) {
        val uri = Uri.parse(url)
            .buildUpon()
            .appendQueryParameter("display", "borderless")
            .appendQueryParameter("platform", "android")
            .build()

        val colorSchemeParams = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(ThemePrefs.primaryColor) // TODO set color
            .build()

        var intent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(colorSchemeParams)
            .setShowTitle(true)
            .build()
            .intent

        intent.data = uri

        // Exclude Instructure apps from chooser options
        intent = intent.asChooserExcludingInstructure()

        requireContext().startActivity(intent)
        Handler(Looper.getMainLooper()).postDelayed({
            if (activity == null) return@postDelayed
            requireActivity().onBackPressed()
        }, 500)
    }

    companion object {
        const val LTI_URL = "lti_url"
        const val LTI_TITLE = "lti_title"
        const val LTI_TAB = "lti_tab"
        const val LTI_TOOL = "lti_tool"
        const val SESSION_LESS_LAUNCH = "session_less_launch"
        const val IS_ASSIGNMENT_LTI = "is_assignment_lti"

        fun makeRoute(canvasContext: CanvasContext, ltiTab: Tab): Route {
            val bundle = Bundle().apply { putParcelable(LTI_TAB, ltiTab) }
            return Route(LtiLaunchFragment::class.java, canvasContext, bundle)
        }

        /**
         * The ltiTool param is used specifically for launching assignment based lti tools, where its possible to have
         * a tool "collision". As such, we need to pre-fetch the correct tool to use here.
         */
        fun makeRoute(
            canvasContext: CanvasContext,
            url: String,
            title: String? = null,
            sessionLessLaunch: Boolean = false,
            assignmentLti: Boolean = false,
            ltiTool: LTITool? = null
        ): Route {
            val bundle = Bundle().apply {
                putString(LTI_URL, url)
                putBoolean(SESSION_LESS_LAUNCH, sessionLessLaunch)
                putBoolean(IS_ASSIGNMENT_LTI, assignmentLti)
                putString(LTI_TITLE, title) // For 'title' property in InternalWebViewFragment
                putParcelable(LTI_TOOL, ltiTool)
            }
            return Route(LtiLaunchFragment::class.java, canvasContext, bundle)
        }

        private fun validateRoute(route: Route): Boolean {
            route.canvasContext ?: return false
            return route.arguments.getParcelable<Tab>(LTI_TAB) != null || route.arguments.getString(LTI_URL).isValid()
        }

        fun newInstance(route: Route): LtiLaunchFragment? {
            if (!validateRoute(route)) return null
            return LtiLaunchFragment().withArgs(route.argsWithContext)
        }

        fun makeSessionlessLtiUrlRoute(activity: FragmentActivity, canvasContext: CanvasContext?, ltiUrl: String): Route {
            val decodedUrl = URLDecoder.decode(ltiUrl, "utf-8")
            val title = activity.getString(R.string.utils_externalToolTitle)
            val args = Bundle()
            args.putString(LTI_URL, decodedUrl)
            args.putBoolean(SESSION_LESS_LAUNCH, true)
            args.putString(LTI_TITLE, title)

            return Route(LtiLaunchFragment::class.java, canvasContext, args)
        }
    }
}
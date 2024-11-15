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

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrl
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_LTI_LAUNCH
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.HtmlContentFormatter
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.NullableStringArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.argsWithContext
import com.instructure.pandautils.utils.asChooserExcludingInstructure
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.replaceWithURLQueryParameter
import com.instructure.pandautils.utils.setTextForVisibility
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.R
import com.instructure.student.databinding.FragmentLtiLaunchBinding
import com.instructure.student.router.RouteMatcher
import kotlinx.coroutines.Job
import java.net.URLDecoder

@ScreenView(SCREEN_VIEW_LTI_LAUNCH)
@PageView
class LtiLaunchFragment : ParentFragment() {

    private val binding by viewBinding(FragmentLtiLaunchBinding::bind)

    var canvasContext: CanvasContext by ParcelableArg(default = CanvasContext.emptyUserContext(), key = Const.CANVAS_CONTEXT)
    var title: String? by NullableStringArg(key = Const.ACTION_BAR_TITLE)
    private var ltiUrl: String by StringArg(key = LTI_URL)
    private var ltiTab: Tab? by NullableParcelableArg(key = Const.TAB)
    private var ltiTool: LTITool? by NullableParcelableArg(key = Const.LTI_TOOL, default = null)
    private var sessionLessLaunch: Boolean by BooleanArg(key = Const.SESSIONLESS_LAUNCH)
    private var isAssignmentLTI: Boolean by BooleanArg(key = Const.ASSIGNMENT_LTI)

    /* Tracks whether we have automatically started launching the LTI tool in a chrome custom tab. Because this fragment
    re-runs certain logic in onResume, tracking the launch helps us know to pop this fragment instead of erroneously
    launching again when the user returns to the app. */
    private var customTabLaunched: Boolean = false

    private var ltiUrlLaunchJob: Job? = null

    @Suppress("unused")
    @PageViewUrl
    fun makePageViewUrl() =
        ltiTab?.externalUrl ?: ApiPrefs.fullDomain + canvasContext.toAPIString() + "/external_tools"

    override fun title(): String = title.validOrNull() ?: ltiTab?.label?.validOrNull() ?: ltiUrl.validOrNull() ?: ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_lti_launch, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loadingView.setOverrideColor(canvasContext.color)
        binding.toolName.setTextForVisibility(title().validOrNull())
    }

    override fun applyTheme() = Unit

    override fun onResume() {
        super.onResume()
        // If onResume() is called after the custom tab has launched, it means the user is returning and we should close this fragment
        if (customTabLaunched) {
            activity?.supportFragmentManager?.popBackStack()
            return
        }

        try {
            when {
                ltiTab != null -> loadSessionlessLtiUrl(ltiTab!!.ltiUrl)
                ltiUrl.isNotBlank() -> {
                    var url = ltiUrl // Replace deep link scheme
                        .replaceFirst("canvas-courses://", "${ApiPrefs.protocol}://")
                        .replaceFirst("canvas-student://", "${ApiPrefs.protocol}://")
                        .replaceWithURLQueryParameter(HtmlContentFormatter.hasKalturaUrl(ltiUrl))

                    when {
                        sessionLessLaunch -> {
                            // This is specific for Studio and Gauge
                            val id = url.substringAfterLast("/external_tools/").substringBefore("?")
                            url = when {
                                (id.toIntOrNull() != null) -> when (canvasContext) {
                                    is Course -> "${ApiPrefs.fullDomain}/api/v1/courses/${canvasContext.id}/external_tools/sessionless_launch?id=$id"
                                    is Group -> "${ApiPrefs.fullDomain}/api/v1/groups/${canvasContext.id}/external_tools/sessionless_launch?id=$id"
                                    else -> "${ApiPrefs.fullDomain}/api/v1/accounts/self/external_tools/sessionless_launch?id=$id"
                                }
                                else -> when (canvasContext) {
                                    is Course -> "${ApiPrefs.fullDomain}/api/v1/courses/${canvasContext.id}/external_tools/sessionless_launch?url=$url"
                                    is Group -> "${ApiPrefs.fullDomain}/api/v1/groups/${canvasContext.id}/external_tools/sessionless_launch?url=$url"
                                    else -> "${ApiPrefs.fullDomain}/api/v1/accounts/self/external_tools/sessionless_launch?url=$url"
                                }
                            }
                            loadSessionlessLtiUrl(url)
                        }
                        isAssignmentLTI -> loadSessionlessLtiUrl(url)
                        else -> launchCustomTab(url)
                    }
                }
                else -> displayError()
            }
        } catch (e: Exception) {
            // If it gets here we're in trouble and won't know what the tab is, so just display an error message
            displayError()
        }
    }

    private fun loadSessionlessLtiUrl(ltiUrl: String) {
        ltiUrlLaunchJob = weave {
            val tool = getLtiTool(ltiUrl)
            tool?.url?.let { launchCustomTab(it) } ?: displayError()
        }
    }

    private fun launchCustomTab(url: String) {
        val uri = Uri.parse(url)
            .buildUpon()
            .appendQueryParameter("display", "borderless")
            .appendQueryParameter("platform", "android")
            .build()

        val colorSchemeParams = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(canvasContext.color)
            .build()

        var intent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(colorSchemeParams)
            .setShowTitle(true)
            .build()
            .intent

        intent.data = uri

        // Exclude Instructure apps from chooser options
        intent = intent.asChooserExcludingInstructure()

        context?.startActivity(intent)

        customTabLaunched = true
    }

    private fun displayError() {
        toast(R.string.errorOccurred)
        if (activity != null) {
            requireActivity().onBackPressed()
        }
    }

    private suspend fun getLtiTool(url: String): LTITool? {
        return ltiTool?.let {
            AssignmentManager.getExternalToolLaunchUrlAsync(it.courseId, it.id, it.assignmentId).await().dataOrNull
        } ?: SubmissionManager.getLtiFromAuthenticationUrlAsync(url, true).await().dataOrNull
    }

    override fun onDestroy() {
        super.onDestroy()
        ltiUrlLaunchJob?.cancel()
    }

    companion object {
        const val LTI_URL = "ltiUrl"

        fun makeLTIBundle(ltiUrl: String, title: String, sessionLessLaunch: Boolean): Bundle {
            val args = Bundle()
            args.putString(LTI_URL, ltiUrl)
            args.putBoolean(Const.SESSIONLESS_LAUNCH, sessionLessLaunch)
            args.putString(Const.ACTION_BAR_TITLE, title)
            return args
        }

        fun makeRoute(canvasContext: CanvasContext, ltiTab: Tab): Route {
            val bundle = Bundle().apply { putParcelable(Const.TAB, ltiTab) }
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
            isAssignmentLTI: Boolean = false,
            ltiTool: LTITool? = null
        ): Route {
            val bundle = Bundle().apply {
                putString(LTI_URL, url)
                putBoolean(Const.SESSIONLESS_LAUNCH, sessionLessLaunch)
                putBoolean(Const.ASSIGNMENT_LTI, isAssignmentLTI)
                putString(Const.ACTION_BAR_TITLE, title) // For 'title' property in InternalWebViewFragment
                putParcelable(Const.LTI_TOOL, ltiTool)
            }
            return Route(LtiLaunchFragment::class.java, canvasContext, bundle)
        }

        fun validateRoute(route: Route): Boolean {
            route.canvasContext ?: return false
            return route.arguments.getParcelable<Tab>(Const.TAB) != null || route.arguments.getString(LTI_URL).isValid()
        }

        fun newInstance(route: Route): LtiLaunchFragment? {
            if (!validateRoute(route)) return null
            return LtiLaunchFragment().withArgs(route.argsWithContext)
        }

        fun routeLtiLaunchFragment(activity: FragmentActivity, canvasContext: CanvasContext?, url: String) {
            val args = makeLTIBundle(URLDecoder.decode(url, "utf-8"), activity.getString(R.string.utils_externalToolTitle), true)
            RouteMatcher.route(activity, Route(LtiLaunchFragment::class.java, canvasContext, args))
        }
    }
}

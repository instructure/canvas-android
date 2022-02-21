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
package com.instructure.teacher.fragments

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.pandautils.analytics.SCREEN_VIEW_LTI_LAUNCH
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.fragments.BaseFragment
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import kotlinx.android.synthetic.main.fragment_lti_launch.*
import kotlinx.coroutines.Job

@ScreenView(SCREEN_VIEW_LTI_LAUNCH)
class LtiLaunchFragment : BaseFragment() {

    private var title: String? by NullableStringArg(key = Const.TITLE)
    private var ltiUrl: String by StringArg(key = LTI_URL)
    private var ltiTab: Tab? by NullableParcelableArg(key = TAB)
    private var sessionLessLaunch: Boolean by BooleanArg(key = SESSION_LESS)

    /* Tracks whether we have automatically started launching the LTI tool in a chrome custom tab. Because this fragment
    re-runs certain logic in onResume, tracking the launch helps us know to pop this fragment instead of erroneously
    launching again when the user returns to the app. */
    private var customTabLaunched: Boolean = false

    private var ltiUrlLaunchJob: Job? = null

    override fun layoutResId(): Int = R.layout.fragment_lti_launch

    override fun onCreateView(view: View) = Unit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val color = canvasContext?.color ?: ThemePrefs.primaryColor
        ViewStyler.setStatusBarDark(requireActivity(), color)
        loadingView.setOverrideColor(color)
        toolName.setTextForVisibility(title.validOrNull() ?: ltiTab?.label?.validOrNull() ?: ltiUrl.validOrNull())
    }

    override fun onResume() {
        super.onResume()
        // If onResume() is called after the custom tab has launched, it means the user is returning and we should close this fragment
        if (customTabLaunched) {
            /* Due to how fragment management is set up, attempting to pop this fragment directly from onResume can
            result in a crash. We'll work around this by posting the action to the main thread message queue. */
            Handler().post { activity?.onBackPressed() }
            return
        }

        try {
            when {
                ltiTab != null -> getSessionlessLtiUrl(ltiTab!!.ltiUrl)
                ltiUrl.isNotBlank() -> {
                    var url = ltiUrl // Replace deep link scheme
                        .replaceFirst("canvas-courses://", "${ApiPrefs.protocol}://")
                        .replaceFirst("canvas-student://", "${ApiPrefs.protocol}://")
                    if (sessionLessLaunch) {
                        if (url.contains("api/v1/")) {
                            getSessionlessLtiUrl(url)
                        } else {
                            // This is specific for Studio and Gauge
                            url = "${ApiPrefs.fullDomain}/api/v1/accounts/self/external_tools/sessionless_launch?url=$url"
                            getSessionlessLtiUrl(url)
                        }
                    } else {
                        launchCustomTab(url)
                    }
                }
                else -> displayError()
            }
        } catch (e: Exception) {
            // If it gets here we're in trouble and won't know what the tab is, so just display an error message
            displayError()
        }
    }

    private fun getSessionlessLtiUrl(url: String) {
        ltiUrlLaunchJob = weave {
            val tool = SubmissionManager.getLtiFromAuthenticationUrlAsync(url, true).await().dataOrNull
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
            .setToolbarColor(canvasContext?.color ?: ThemePrefs.primaryColor)
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
        (requireContext() as? Activity)?.onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ltiUrlLaunchJob?.cancel()
    }

    companion object {
        private const val TAB = "tab"
        private const val LTI_URL = "lti_url"
        private const val SESSION_LESS = "session_less"

        fun makeTabBundle(canvasContext: CanvasContext, ltiTab: Tab): Bundle {
            val args = createBundle(canvasContext)
            args.putParcelable(TAB, ltiTab)
            return args
        }

        fun makeBundle(canvasContext: CanvasContext?, url: String, title: String, sessionLessLaunch: Boolean): Bundle {
            val args = createBundle(canvasContext)
            args.putString(LTI_URL, url)
            args.putBoolean(SESSION_LESS, sessionLessLaunch)
            args.putString(Const.TITLE, title)
            return args
        }

        fun newInstance(args: Bundle) = LtiLaunchFragment().apply { arguments = args }
    }
}

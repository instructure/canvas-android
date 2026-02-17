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

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
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
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.databinding.FragmentLtiLaunchBinding
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.navigation.WebViewRouter
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.NullableStringArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.PermissionRequester
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyBottomSystemBarInsets
import com.instructure.pandautils.utils.applyDisplayCutoutInsets
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.argsWithContext
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.pandautils.utils.enableAlgorithmicDarkening
import com.instructure.pandautils.utils.launchCustomTab
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setTextForVisibility
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.utils.withArgs
import com.instructure.pandautils.views.CanvasWebView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.net.URLDecoder
import javax.inject.Inject

@AndroidEntryPoint
@ScreenView(SCREEN_VIEW_LTI_LAUNCH)
@PageView
class LtiLaunchFragment : BaseCanvasFragment(), NavigationCallbacks {

    private val binding by viewBinding(FragmentLtiLaunchBinding::bind)

    private val viewModel: LtiLaunchViewModel by viewModels()

    private var title: String? by NullableStringArg(key = LTI_TITLE)
    private var ltiTab: Tab? by NullableParcelableArg(key = LTI_TAB)
    private var ltiUrl: String? by NullableStringArg(key = LTI_URL)
    private var canvasContext: CanvasContext by ParcelableArg(default = CanvasContext.emptyUserContext(), key = Const.CANVAS_CONTEXT)

    private var customTabLaunched = false

    @Inject
    lateinit var ltiLaunchFragmentBehavior: LtiLaunchFragmentBehavior

    @Inject
    lateinit var webViewRouter: WebViewRouter

    @Suppress("unused")
    @PageViewUrl
    fun makePageViewUrl() = ltiTab?.externalUrl ?: (ApiPrefs.fullDomain + canvasContext.toAPIString() + "/external_tools")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_lti_launch, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply display cutout insets to root view to prevent content from extending behind camera cutout
        binding.root.applyDisplayCutoutInsets()

        binding.toolbar.applyTopSystemBarInsets()
        binding.webView.applyBottomSystemBarInsets()
        binding.loadingView.setOverrideColor(ltiLaunchFragmentBehavior.toolbarColor)
        binding.toolName.setTextForVisibility(title.validOrNull())
        binding.toolbar.setupAsBackButton {
            ltiLaunchFragmentBehavior.closeLtiLaunchFragment(requireActivity())
        }
        binding.toolbar.title = title
        ViewStyler.themeToolbarColored(requireActivity(), binding.toolbar, ltiLaunchFragmentBehavior.toolbarColor, ltiLaunchFragmentBehavior.toolbarTextColor)

        lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)
        lifecycleScope.launch {
            viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collectLatest {
                when (it) {
                    is ViewState.Loading -> binding.loadingLayout.setVisible()
                    else -> binding.loadingLayout.setGone()
                }
            }
        }

        savedInstanceState?.let {
            customTabLaunched = it.getBoolean(KEY_CUSTOM_TAB_LAUNCHED, false)
            binding.webView.restoreState(it)
        }
    }

    override fun onResume() {
        super.onResume()
        if (customTabLaunched) {
            customTabLaunched = false
            ltiLaunchFragmentBehavior.closeLtiLaunchFragment(requireActivity())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_CUSTOM_TAB_LAUNCHED, customTabLaunched)
        binding.webView.saveState(outState)
    }

    private fun handleAction(action: LtiLaunchAction) {
        when (action) {
            is LtiLaunchAction.LaunchCustomTab -> launchCustomTab(action.url)
            is LtiLaunchAction.ShowError -> {
                toast(R.string.errorOccurred)
                if (activity != null) {
                    requireActivity().onBackPressed()
                }
            }

            is LtiLaunchAction.LoadLtiWebView -> loadLtiToolIntoWebView(action.url)
        }
    }

    private fun launchCustomTab(url: String) {
        activity?.let {
            customTabLaunched = true
            it.launchCustomTab(url, ltiLaunchFragmentBehavior.toolbarColor)
        }
    }

    private fun loadLtiToolIntoWebView(url: String) {
        setupFilePicker()
        binding.webView.enableAlgorithmicDarkening()
        binding.webView.setZoomSettings(false)
        binding.webView.addVideoClient(requireActivity())
        binding.webView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) = Unit

            override fun onPageStartedCallback(webView: WebView, url: String) {
                if (isAdded) binding.webViewProgress.setVisible()
            }

            override fun onPageFinishedCallback(webView: WebView, url: String) {
                if (isAdded) binding.webViewProgress.setGone()
            }

            override fun canRouteInternallyDelegate(url: String): Boolean {
                // Handle return button in external tools. Links to course homepage should close the tool.
                return url == contextLink() || canRouteInternally(url)
            }

            private fun canRouteInternally(url: String) =
                webViewRouter.canRouteInternally(url)
                    && ltiUrl?.substringBefore("?") != url.substringBefore("?")
                    && !url.contains("sessionless_launch")

            override fun routeInternallyCallback(url: String) {
                // Handle return button in external tools. Links to course homepage should close the tool.
                if (isAdded) {
                    if (url == contextLink()) {
                        ltiLaunchFragmentBehavior.closeLtiLaunchFragment(requireActivity())
                    } else {
                        webViewRouter.routeInternally(url)
                    }
                }
            }
        }
        binding.loadingLayout.setGone()
        binding.webView.setVisible()
        binding.webView.loadUrl(url)
    }

    fun contextLink() = "${ApiPrefs.fullDomain}${canvasContext.toAPIString()}"

    private fun setupFilePicker() {
        binding.webView.setCanvasWebChromeClientShowFilePickerCallback(object : CanvasWebView.VideoPickerCallback {
            override fun requestStartActivityForResult(intent: Intent, requestCode: Int) {
                startActivityForResult(intent, requestCode)
            }

            override fun permissionsGranted(): Boolean {
                return if (PermissionUtils.hasPermissions(requireActivity(), PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
                    true
                } else {
                    requestFilePermissions()
                    false
                }
            }
        })
    }

    private fun requestFilePermissions() {
        requestPermissions(
            PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.CAMERA),
            PermissionUtils.PERMISSION_REQUEST_CODE
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRequestPermissionsResult(result: PermissionRequester.PermissionResult) {
        if (PermissionUtils.allPermissionsGrantedResultSummary(result.grantResults)) {
            binding.webView.clearPickerCallback()
            Toast.makeText(requireContext(), R.string.pleaseTryAgain, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!binding.webView.handleOnActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        private const val KEY_CUSTOM_TAB_LAUNCHED = "key_custom_tab_launched"

        const val LTI_URL = "lti_url"
        const val LTI_TITLE = "lti_title"
        const val LTI_TAB = "lti_tab"
        const val LTI_TOOL = "lti_tool"
        const val SESSION_LESS_LAUNCH = "session_less_launch"
        const val IS_ASSIGNMENT_LTI = "is_assignment_lti"
        const val OPEN_INTERNALLY = "open_internally"

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
            ltiTool: LTITool? = null,
            openInternally: Boolean = false
        ): Route {
            val bundle = Bundle().apply {
                putString(LTI_URL, url)
                putBoolean(SESSION_LESS_LAUNCH, sessionLessLaunch)
                putBoolean(IS_ASSIGNMENT_LTI, assignmentLti)
                putString(LTI_TITLE, title) // For 'title' property in InternalWebViewFragment
                putParcelable(LTI_TOOL, ltiTool)
                putBoolean(OPEN_INTERNALLY, openInternally)
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

    override fun onHandleBackPressed(): Boolean {
        return binding.webView.handleGoBack()
    }
}
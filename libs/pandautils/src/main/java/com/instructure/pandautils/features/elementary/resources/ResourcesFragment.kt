/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.pandautils.features.elementary.resources

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import com.instructure.pandautils.blueprint.BaseCanvasFragment
import androidx.fragment.app.viewModels
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrl
import com.instructure.pandautils.BuildConfig
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_K5_RESOURCES
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.databinding.FragmentResourcesBinding
import com.instructure.pandautils.discussions.DiscussionUtils
import com.instructure.pandautils.features.elementary.resources.itemviewmodels.ResourcesRouter
import com.instructure.pandautils.navigation.WebViewRouter
import com.instructure.pandautils.utils.children
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.pandautils.views.CanvasWebViewWrapper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@PageView
@ScreenView(SCREEN_VIEW_K5_RESOURCES)
@AndroidEntryPoint
class ResourcesFragment : BaseCanvasFragment() {

    @Inject
    lateinit var resourcesRouter: ResourcesRouter

    @Inject
    lateinit var webViewRouter: WebViewRouter

    private val viewModel: ResourcesViewModel by viewModels()

    private lateinit var binding: FragmentResourcesBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentResourcesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.events.observe(viewLifecycleOwner, { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        })

        return binding.root
    }

    private fun handleAction(action: ResourcesAction) {
        when (action) {
            is ResourcesAction.OpenLtiApp -> showCourseSelectorDialog(action.ltiTools)
            is ResourcesAction.OpenComposeMessage -> resourcesRouter.openComposeMessage(action.recipient)
            ResourcesAction.ImportantLinksViewsReady -> setupWebViews()
            ResourcesAction.ShowRefreshError -> toast(R.string.failedToRefreshResources)
            is ResourcesAction.WebLtiButtonPressed -> DiscussionUtils.launchIntent(requireContext(), action.url)
        }
    }

    private fun showCourseSelectorDialog(ltiTools: List<LTITool>) {
        val dialogEntries = ltiTools
            .map { it.contextName }
            .toTypedArray()

        AlertDialog.Builder(requireContext(), R.style.AccentDialogTheme)
            .setTitle(R.string.chooseACourse)
            .setItems(dialogEntries) { dialog, which -> openSelectedLti(dialog, which, ltiTools) }
            .setNegativeButton(R.string.sortByDialogCancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun openSelectedLti(dialog: DialogInterface?, index: Int, ltiTools: List<LTITool>) {
        dialog?.dismiss()
        val ltiTool = ltiTools[index]
        resourcesRouter.openLti(ltiTool)
    }

    private fun setupWebViews() {
        binding.importantLinksContainer.children.forEach {
            val webView = it.findViewById<CanvasWebViewWrapper>(R.id.importantLinksWebViewWrapper)
            if (webView != null) {
                setupWebView(webView.webView)
            }
        }
    }

    private fun setupWebView(webView: CanvasWebView) {
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        webView.setBackgroundColor(requireContext().getColor(R.color.backgroundLightest))
        webView.settings.loadWithOverviewMode = true
        webView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun routeInternallyCallback(url: String) {
                webViewRouter.routeInternally(url)
            }

            override fun canRouteInternallyDelegate(url: String): Boolean = webViewRouter.canRouteInternally(url)

            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                webViewRouter.openMedia(url)
            }

            override fun onPageStartedCallback(webView: WebView, url: String) = Unit
            override fun onPageFinishedCallback(webView: WebView, url: String) = Unit
        }

        webView.addVideoClient(requireActivity())
    }

    @PageViewUrl
    fun makePageViewUrl() = "${ApiPrefs.fullDomain}#resources"

    companion object {
        fun newInstance(): ResourcesFragment {
            return ResourcesFragment()
        }
    }
}
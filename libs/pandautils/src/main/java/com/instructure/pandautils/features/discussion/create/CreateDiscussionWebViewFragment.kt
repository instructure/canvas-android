/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.features.discussion.create

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.interactions.router.Route
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_CREATE_DISCUSSION_REDESIGN
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.databinding.FragmentDiscussionCreateWebViewBinding
import com.instructure.pandautils.features.discussion.DiscussionSharedAction
import com.instructure.pandautils.features.discussion.DiscussionSharedEvents
import com.instructure.pandautils.navigation.WebViewRouter
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.Const.CANVAS_CONTEXT
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.PermissionRequester
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.enableAlgorithmicDarkening
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.views.CanvasWebView
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@PageView(url = "{canvasContext}/discussion_topics/new")
@ScreenView(SCREEN_VIEW_CREATE_DISCUSSION_REDESIGN)
@AndroidEntryPoint
class CreateDiscussionWebViewFragment : BaseCanvasFragment() {

    @Inject
    lateinit var webViewRouter: WebViewRouter

    @Inject
    lateinit var discussionSharedEvents: DiscussionSharedEvents

    @get:PageViewUrlParam("canvasContext")
    var canvasContext: CanvasContext by ParcelableArg(key = CANVAS_CONTEXT)

    var isAnnouncement: Boolean by BooleanArg(key = IS_ANNOUNCEMENT)

    private val viewModel: CreateDiscussionWebViewViewModel by viewModels()

    private lateinit var binding: FragmentDiscussionCreateWebViewBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentDiscussionCreateWebViewBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.loadData(canvasContext, isAnnouncement)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = getString(if (isAnnouncement) R.string.newAnnouncement else R.string.newDiscussion)
        setupToolbar(title)
        setupFilePicker()
        binding.discussionWebView.addVideoClient(requireActivity())
        binding.discussionWebView.enableAlgorithmicDarkening()
        binding.discussionWebView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                webViewRouter.openMedia(url)
            }

            override fun onPageStartedCallback(webView: WebView, url: String) {
                viewModel.setLoading(true)
            }

            override fun onPageFinishedCallback(webView: WebView, url: String) {
                viewModel.setLoading(false)
            }

            override fun routeInternallyCallback(url: String) {
                if (url.contains("discussion_topics") || url.contains("announcements")) {
                    discussionSharedEvents.sendEvent(lifecycleScope, DiscussionSharedAction.RefreshListScreen)
                    requireActivity().onBackPressed()
                } else if (!webViewRouter.canRouteInternally(url, routeIfPossible = true)) {
                    webViewRouter.routeExternally(url)
                }
            }

            override fun canRouteInternallyDelegate(url: String): Boolean {
                return viewModel.data.value?.url?.substringBefore("?") != url.substringBefore("?")
            }
        }
    }

    private fun setupFilePicker() {
        binding.discussionWebView.setCanvasWebChromeClientShowFilePickerCallback(object : CanvasWebView.VideoPickerCallback {
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
            binding.discussionWebView.clearPickerCallback()
            Toast.makeText(requireContext(), R.string.pleaseTryAgain, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!binding.discussionWebView.handleOnActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun setupToolbar(title: String) = with(binding) {
        toolbar.title = title

        toolbar.setupAsBackButton(this@CreateDiscussionWebViewFragment)

        ViewStyler.themeToolbarColored(requireActivity(), toolbar, canvasContext)
    }

    companion object {
        val IS_ANNOUNCEMENT = "isAnnouncement"

        fun makeBundle(canvasContext: CanvasContext, isAnnouncement: Boolean = false): Bundle =
            Bundle().apply {
                putParcelable(CANVAS_CONTEXT, canvasContext)
                putBoolean(IS_ANNOUNCEMENT, isAnnouncement)
            }

        fun makeRoute(canvasContext: CanvasContext, isAnnouncement: Boolean = false): Route {
            return Route(null, CreateDiscussionWebViewFragment::class.java, canvasContext, makeBundle(canvasContext, isAnnouncement))
        }

        fun newInstance(route: Route) = CreateDiscussionWebViewFragment().apply {
            arguments = route.canvasContext!!.makeBundle(route.arguments)
        }
    }

}
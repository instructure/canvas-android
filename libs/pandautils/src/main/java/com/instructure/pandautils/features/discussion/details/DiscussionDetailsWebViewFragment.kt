/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.discussion.details

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_DISCUSSION_DETAILS_REDESIGN
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.databinding.FragmentDiscussionDetailsWebViewBinding
import com.instructure.pandautils.features.discussion.router.DiscussionRouter
import com.instructure.pandautils.navigation.WebViewRouter
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.PermissionRequester
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.enableAlgorithmicDarkening
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.setMenu
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.views.CanvasWebView
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@PageView(url = "{canvasContext}/discussion_topics/{topicId}")
@ScreenView(SCREEN_VIEW_DISCUSSION_DETAILS_REDESIGN)
@AndroidEntryPoint
class DiscussionDetailsWebViewFragment : BaseCanvasFragment() {

    @Inject
    lateinit var webViewRouter: WebViewRouter

    @Inject
    lateinit var discussionRouter: DiscussionRouter

    @Inject
    lateinit var discussionDetailsWebViewFragmentBehavior: DiscussionDetailsWebViewFragmentBehavior

    @get:PageViewUrlParam("canvasContext")
    var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)
    private var discussionTopicHeader: DiscussionTopicHeader? by NullableParcelableArg(key = DISCUSSION_TOPIC_HEADER)
    private var discussionTopicHeaderId: Long by LongArg(default = 0L, key = DISCUSSION_TOPIC_HEADER_ID)

    private val viewModel: DiscussionDetailsWebViewViewModel by viewModels()

    private lateinit var binding: FragmentDiscussionDetailsWebViewBinding

    @PageViewUrlParam("topicId")
    fun getTopicId() = discussionTopicHeader?.id ?: discussionTopicHeaderId

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentDiscussionDetailsWebViewBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.loadData(canvasContext, discussionTopicHeader, discussionTopicHeaderId)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.data.observe(viewLifecycleOwner) {
            setupToolbar(it.title)
        }
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
                    binding.discussionWebView.loadUrl(url)
                } else if (url.contains("speed_grader")) {
                    val uri = url.toUri()
                    val assignmentId = uri.getQueryParameter("assignment_id")?.toLongOrNull() ?: 0
                    discussionRouter.routeToNativeSpeedGrader(
                        canvasContext.id,
                        assignmentId,
                        emptyList(),
                        0,
                        null
                    )

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

        if (discussionDetailsWebViewFragmentBehavior.showBackButton) {
            toolbar.setupAsBackButton(this@DiscussionDetailsWebViewFragment)
        }

        binding.toolbar.setMenu(R.menu.menu_discussion_details) {
            when (it.itemId) {
                R.id.refresh -> binding.discussionWebView.reload()
            }
        }

        ViewStyler.themeToolbarColored(requireActivity(), toolbar, canvasContext)
    }

    companion object {

        const val DISCUSSION_TOPIC_HEADER = "discussion_topic_header"
        const val DISCUSSION_TOPIC_HEADER_ID = "discussion_topic_header_id"
        const val DISCUSSION_TOPIC = "discussion_topic"

        fun makeRoute(canvasContext: CanvasContext, discussionTopicHeader: DiscussionTopicHeader): Route {
            val bundle = Bundle().apply {
                putParcelable(DISCUSSION_TOPIC_HEADER, discussionTopicHeader)
                putLong(DISCUSSION_TOPIC_HEADER_ID, discussionTopicHeader.id)
            }

            return Route(null, DiscussionDetailsWebViewFragment::class.java, canvasContext, bundle)
        }

        fun makeRoute(canvasContext: CanvasContext, discussionTopicHeaderId: Long): Route {
            val bundle = Bundle().apply {
                putLong(DISCUSSION_TOPIC_HEADER_ID, discussionTopicHeaderId)
            }

            return Route(null, DiscussionDetailsWebViewFragment::class.java, canvasContext, bundle)
        }

        fun newInstance(route: Route) = if (validRoute(route)) {
            DiscussionDetailsWebViewFragment().apply {
                arguments = route.canvasContext!!.makeBundle(route.arguments)

                // For routing
                if (route.paramsHash.containsKey(RouterParams.MESSAGE_ID))
                    discussionTopicHeaderId = route.paramsHash[RouterParams.MESSAGE_ID]?.toLong() ?: 0L
            }
        } else null

        fun validRoute(route: Route) = route.canvasContext != null &&
                (route.arguments.containsKey(DISCUSSION_TOPIC_HEADER) ||
                        route.arguments.containsKey(DISCUSSION_TOPIC_HEADER_ID) ||
                        route.paramsHash.containsKey(RouterParams.MESSAGE_ID))
    }

}
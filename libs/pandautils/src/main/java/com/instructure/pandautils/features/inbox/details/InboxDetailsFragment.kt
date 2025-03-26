/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.features.inbox.details

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_INBOX_CONVERSATION
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.features.inbox.details.composables.InboxDetailsScreen
import com.instructure.pandautils.features.inbox.list.InboxRouter
import com.instructure.pandautils.features.inbox.utils.InboxSharedAction
import com.instructure.pandautils.features.inbox.utils.InboxSharedEvents
import com.instructure.pandautils.navigation.WebViewRouter
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.utils.withArgs
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@ScreenView(SCREEN_VIEW_INBOX_CONVERSATION)
@PageView(url = "conversations")
@AndroidEntryPoint
class InboxDetailsFragment : BaseCanvasFragment(), FragmentInteractions {

    private val viewModel: InboxDetailsViewModel by viewModels()

    @Inject
    lateinit var inboxRouter: InboxRouter

    @Inject
    lateinit var sharedEvents: InboxSharedEvents

    @Inject
    lateinit var webViewRouter: WebViewRouter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        applyTheme()
        viewLifecycleOwner.lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)
        lifecycleScope.collectOneOffEvents(sharedEvents.events, ::handleSharedViewModelAction)

        return ComposeView(requireActivity()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()

                InboxDetailsScreen(title(), uiState,  viewModel::messageActionHandler, viewModel::handleAction)
            }
        }
    }

    override val navigation: Navigation?
        get() = activity as? Navigation

    override fun title(): String = getString(R.string.message)

    override fun applyTheme() {
        ViewStyler.setStatusBarDark(requireActivity(), ThemePrefs.primaryColor)
    }

    override fun getFragment(): Fragment {
        return this
    }

    private fun handleAction(action: InboxDetailsFragmentAction) {
        when (action) {
            is InboxDetailsFragmentAction.CloseFragment -> {
                inboxRouter.popDetailsScreen(activity)
            }
            is InboxDetailsFragmentAction.ShowScreenResult -> {
                Toast.makeText(requireContext(), action.message, Toast.LENGTH_SHORT).show()
            }
            is InboxDetailsFragmentAction.UrlSelected -> {
                try {
                    if (webViewRouter.canRouteInternally(action.url, true)) return
                    val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(action.url))
                    activity?.startActivity(urlIntent)
                } catch (e: ActivityNotFoundException) {
                    toast(R.string.inboxMessageFailedToOpenUrl)
                }
            }
            is InboxDetailsFragmentAction.OpenAttachment -> {
                inboxRouter.routeToAttachment(action.attachment)
            }
            is InboxDetailsFragmentAction.UpdateParentFragment -> {
                sharedEvents.sendEvent(lifecycleScope, InboxSharedAction.RefreshListScreen)
            }
            is InboxDetailsFragmentAction.NavigateToCompose -> {
                inboxRouter.routeToCompose(action.options)
            }
        }
    }

    private fun handleSharedViewModelAction(action: InboxSharedAction) {
        when (action) {
            is InboxSharedAction.RefreshDetailsScreen -> {
                viewModel.refreshConversation()
            }
            else -> {}
        }
    }

    companion object {
        const val CONVERSATION_ID = "conversation_id"
        const val UNREAD = "unread"

        fun newInstance(): InboxDetailsFragment {
            return InboxDetailsFragment()
        }

        fun newInstance(route: Route): InboxDetailsFragment {
            route.paramsHash[RouterParams.CONVERSATION_ID]?.let {
                route.arguments.putLong(Const.CONVERSATION_ID, it.toLong())
            }
            return InboxDetailsFragment().withArgs(route.arguments)
        }

        fun makeRoute(conversationId: Long, unread: Boolean = false): Route {
            val bundle = bundleOf().apply {
                putLong(Const.CONVERSATION_ID, conversationId)
                putBoolean(UNREAD, unread)
            }
            return Route(null, InboxDetailsFragment::class.java, null, bundle)
        }
    }
}
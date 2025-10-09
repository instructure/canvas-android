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
package com.instructure.pandautils.features.inbox.compose

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
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkInfo
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.interactions.router.Route
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_INBOX_COMPOSE
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.features.file.upload.FileUploadDialogFragment
import com.instructure.pandautils.features.file.upload.FileUploadDialogParent
import com.instructure.pandautils.features.inbox.compose.composables.InboxComposeScreenWrapper
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsMode.FORWARD
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsMode.NEW_MESSAGE
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsMode.REPLY
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsMode.REPLY_ALL
import com.instructure.pandautils.features.inbox.utils.InboxSharedAction
import com.instructure.pandautils.features.inbox.utils.InboxSharedEvents
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.utils.withArgs
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import javax.inject.Inject

@PageView(url = "conversations/compose")
@ScreenView(SCREEN_VIEW_INBOX_COMPOSE)
@AndroidEntryPoint
class InboxComposeFragment : BaseCanvasFragment(), FragmentInteractions, FileUploadDialogParent,
    NavigationCallbacks {

    private val viewModel: InboxComposeViewModel by viewModels()

    @Inject
    lateinit var sharedEvents: InboxSharedEvents

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        applyTheme()
        viewLifecycleOwner.lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)

        return ComposeView(requireActivity()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()

                InboxComposeScreenWrapper(title(), uiState, viewModel::handleAction, viewModel::handleAction, viewModel::handleAction)
            }
        }
    }

    override val navigation: Navigation?
        get() = activity as? Navigation

    override fun title(): String {
        return when(viewModel.uiState.value.inboxComposeMode) {
            NEW_MESSAGE -> getString(R.string.newMessage)
            REPLY -> getString(R.string.reply)
            REPLY_ALL -> getString(R.string.replyAll)
            FORWARD -> getString(R.string.forward)
        }
    }

    override fun applyTheme() {
        ViewStyler.themeStatusBar(requireActivity())
    }

    override fun getFragment(): Fragment {
        return this
    }

    override fun selectedUriStringsCallback(filePaths: List<String>) {
        viewModel.addUploadingAttachments(filePaths)
    }

    override fun workInfoLiveDataCallback(uuid: UUID?, workInfoLiveData: LiveData<WorkInfo?>) {
        workInfoLiveData.observe(viewLifecycleOwner) { workInfo ->
            workInfo?.let {
                viewModel.updateAttachments(uuid, workInfo)
            }
        }
    }

    private fun handleAction(action: InboxComposeViewModelAction) {
        when (action) {
            is InboxComposeViewModelAction.NavigateBack -> {
                requireActivity().onBackPressed()
            }
            is InboxComposeViewModelAction.OpenAttachmentPicker -> {
                val bundle = FileUploadDialogFragment.createMessageAttachmentsBundle(arrayListOf())
                FileUploadDialogFragment.newInstance(bundle)
                    .show(childFragmentManager, FileUploadDialogFragment.TAG)
            }
            is InboxComposeViewModelAction.ShowScreenResult -> {
                Toast.makeText(requireContext(), action.message, Toast.LENGTH_SHORT).show()
            }
            is InboxComposeViewModelAction.UpdateParentFragment -> {
                sharedEvents.sendEvent(lifecycleScope, InboxSharedAction.RefreshDetailsScreen)
                sharedEvents.sendEvent(lifecycleScope, InboxSharedAction.RefreshListScreen)
            }
            is InboxComposeViewModelAction.UrlSelected -> {
                try {
                    val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(action.url))
                    activity?.startActivity(urlIntent)
                } catch (e: ActivityNotFoundException) {
                    toast(R.string.inboxMessageFailedToOpenUrl)
                }
            }
        }
    }

    override fun onHandleBackPressed(): Boolean {
        val uiState = viewModel.uiState.value
        if (shouldAllowExit()) return false

        when (uiState.screenOption) {
            is InboxComposeScreenOptions.None -> {
                viewModel.cancelDismissDialog(true)
            }

            is InboxComposeScreenOptions.ContextPicker -> {
                viewModel.closeContextPicker()
            }

            is InboxComposeScreenOptions.RecipientPicker -> {
                when (uiState.recipientPickerUiState.screenOption) {
                    RecipientPickerScreenOption.Recipients -> {
                        viewModel.recipientPickerBackToRoles()
                    }
                    RecipientPickerScreenOption.Roles -> {
                        viewModel.recipientPickerDone()
                    }
                }
            }
        }

        return true
    }

    // For Teacher BottomSheetDialog handling
    fun shouldAllowExit(): Boolean {
        return !viewModel.uiState.value.enableCustomBackHandler || !viewModel.composeContentHasChanged()
    }

    fun handleExit() {
        onHandleBackPressed()
    }

    companion object {
        const val TAG = "InboxComposeFragment"

        fun newInstance(route: Route): InboxComposeFragment {
            return InboxComposeFragment().withArgs(route.arguments)
        }

        fun makeRoute(options: InboxComposeOptions): Route {
            val bundle = bundleOf().apply {
                putParcelable(InboxComposeOptions.COMPOSE_PARAMETERS, options)
            }
            return Route(InboxComposeFragment::class.java, null, bundle)
        }
    }
}
package com.instructure.pandautils.features.inbox.compose

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.pandautils.R
import com.instructure.pandautils.features.file.upload.FileUploadDialogFragment
import com.instructure.pandautils.features.file.upload.FileUploadDialogParent
import com.instructure.pandautils.features.inbox.compose.composables.InboxComposeScreenWrapper
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.collectOneOffEvents
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InboxComposeFragment : Fragment(), FragmentInteractions, FileUploadDialogParent {

    private val viewModel: InboxComposeViewModel by viewModels()

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

                InboxComposeScreenWrapper(uiState, viewModel::handleAction, viewModel::handleAction, viewModel::handleAction)
            }
        }
    }

    override val navigation: Navigation?
        get() = activity as? Navigation

    override fun title(): String = getString(R.string.newMessage)

    override fun applyTheme() {
        ViewStyler.setStatusBarLight(requireActivity())
    }

    override fun getFragment(): Fragment {
        return this
    }

    override fun attachmentCallback(event: Int, attachment: FileSubmitObject?) {
        Log.d("InboxComposeFragment", "attachmentCallback: ${attachment?.name}")
    }

    private fun handleAction(action: InboxComposeViewModelAction) {
        when (action) {
            is InboxComposeViewModelAction.NavigateBack -> {
                activity?.supportFragmentManager?.popBackStack()
            }
            is InboxComposeViewModelAction.OpenAttachmentPicker -> {
                val bundle = FileUploadDialogFragment.createMessageAttachmentsBundle(arrayListOf())
                FileUploadDialogFragment.newInstance(bundle)
                    .show(childFragmentManager, FileUploadDialogFragment.TAG)
            }
        }
    }
}
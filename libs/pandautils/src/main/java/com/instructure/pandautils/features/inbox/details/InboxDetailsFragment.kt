package com.instructure.pandautils.features.inbox.details

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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.pandautils.R
import com.instructure.pandautils.features.inbox.details.composables.InboxDetailsScreen
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.collectOneOffEvents
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class InboxDetailsFragment : Fragment(), FragmentInteractions {

    private val viewModel: InboxDetailsViewModel by viewModels()

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

                InboxDetailsScreen(title(), uiState,  viewModel::messageActionHandler, viewModel::handleAction)
            }
        }
    }

    override val navigation: Navigation?
        get() = activity as? Navigation

    override fun title(): String = getString(R.string.message)

    override fun applyTheme() {
        ViewStyler.setStatusBarLight(requireActivity())
    }

    override fun getFragment(): Fragment {
        return this
    }

    private fun handleAction(action: InboxDetailsFragmentAction) {
        when (action) {
            is InboxDetailsFragmentAction.CloseFragment -> {
                activity?.supportFragmentManager?.popBackStack()
            }
            is InboxDetailsFragmentAction.ShowScreenResult -> {
                Toast.makeText(requireContext(), action.message, Toast.LENGTH_SHORT).show()
            }
            is InboxDetailsFragmentAction.UrlSelected -> {
                val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(action.url))
                activity?.startActivity(urlIntent)
            }
        }
    }

    companion object {
        const val CONVERSATION_ID = "conversation_id"

        fun newInstance(): InboxDetailsFragment {
            return InboxDetailsFragment()
        }
    }
}
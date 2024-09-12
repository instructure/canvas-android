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
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.pandautils.R
import com.instructure.pandautils.features.inbox.compose.InboxComposeFragment
import com.instructure.pandautils.features.inbox.details.composables.InboxDetailsScreen
import com.instructure.pandautils.features.inbox.list.InboxRouter
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.collectOneOffEvents
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class InboxDetailsFragment : Fragment(), FragmentInteractions {

    private val viewModel: InboxDetailsViewModel by viewModels()

    @Inject
    lateinit var inboxRouter: InboxRouter

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFragmentResultListener()
    }

    private fun setupFragmentResultListener() {
        setFragmentResultListener(InboxComposeFragment.FRAGMENT_RESULT_KEY) { key, bundle ->
            if (key == InboxComposeFragment.FRAGMENT_RESULT_KEY) {
                viewModel.handleAction(InboxDetailsAction.RefreshCalled)
                viewModel.refreshParentFragment()
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
            is InboxDetailsFragmentAction.UpdateParentFragment -> {
                setFragmentResult(FRAGMENT_RESULT_KEY, bundleOf())
            }
            is InboxDetailsFragmentAction.NavigateToCompose -> {
                inboxRouter.routeToCompose(action.options)
            }
        }
    }

    companion object {
        const val CONVERSATION_ID = "conversation_id"
        const val FRAGMENT_RESULT_KEY = "InboxDetailsFragmentResultKey"

        fun newInstance(): InboxDetailsFragment {
            return InboxDetailsFragment()
        }
    }
}
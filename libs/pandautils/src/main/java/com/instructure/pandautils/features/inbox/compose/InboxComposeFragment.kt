package com.instructure.pandautils.features.inbox.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.pandautils.R
import com.instructure.pandautils.features.inbox.compose.composables.InboxComposeScreen
import com.instructure.pandautils.features.inbox.compose.coursepicker.ContextPickerScreen
import com.instructure.pandautils.features.inbox.compose.recipientpicker.RecipientPickerScreen
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InboxComposeFragment : Fragment(), FragmentInteractions {

    private val viewModel: InboxComposeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireActivity()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                val contextPickerState by viewModel.contextPickerUiState.collectAsState()
                val recipientPickerState by viewModel.recipientPickerUiState.collectAsState()

                when (uiState.screenOption) {
                    InboxComposeScreenOptions.None -> {
                        InboxComposeScreen(
                            title = "New Message",
                            uiState = uiState,
                            actionHandler = { action ->
                                when (action) {
                                    InboxComposeActionHandler.CancelClicked -> {
                                        activity?.supportFragmentManager?.popBackStack()
                                    }
                                    InboxComposeActionHandler.OpenContextPicker -> {
                                        viewModel.updateUiState(uiState.copy(screenOption = InboxComposeScreenOptions.ContextPicker))
                                    }
                                    InboxComposeActionHandler.OpenRecipientPicker -> {
                                        viewModel.updateUiState(uiState.copy(screenOption = InboxComposeScreenOptions.RecipientPicker))
                                    }

                                    is InboxComposeActionHandler.BodyChanged -> TODO()
                                    is InboxComposeActionHandler.SendClicked -> TODO()
                                    is InboxComposeActionHandler.SubjectChanged -> TODO()
                                }
                            }
                        )
                    }
                    InboxComposeScreenOptions.ContextPicker -> {
                        ContextPickerScreen(title = "Select a Team", uiState = contextPickerState) { action ->
                            when (action) {
                                ContextPickerActionHandler.DoneClicked -> {
                                    viewModel.updateUiState(uiState.copy(screenOption = InboxComposeScreenOptions.None))
                                }
                                ContextPickerActionHandler.RefreshCalled -> {
                                    viewModel.loadContexts()
                                }
                                is ContextPickerActionHandler.ContextClicked -> {
                                    viewModel.updateUiState(
                                        uiState.copy(selectedContext = action.context, screenOption = InboxComposeScreenOptions.None)
                                    )

                                    viewModel.loadRecipients("", action.context)
                                }
                            }
                            
                        }
                    }
                    InboxComposeScreenOptions.RecipientPicker -> {
                        RecipientPickerScreen(title = "Select Recipients", uiState = recipientPickerState) { action ->
                            when (action) {
                                RecipientPickerActionHandler.DoneClicked -> {
                                    viewModel.updateUiState(uiState.copy(screenOption = InboxComposeScreenOptions.None))
                                }
                                is RecipientPickerActionHandler.RecipientClicked -> {
                                    viewModel.updateUiState(uiState.copy(selectedRecipients = uiState.selectedRecipients + action.recipient))
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    override val navigation: Navigation?
        get() = activity as? Navigation

    override fun title(): String = getString(R.string.new_message)

    override fun applyTheme() {
        ViewStyler.setStatusBarDark(requireActivity(), ThemePrefs.primaryColor)
    }

    override fun getFragment(): Fragment {
        return this
    }
}
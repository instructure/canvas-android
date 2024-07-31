package com.instructure.pandautils.features.inbox.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.animations.ScreenSlideBackTransition
import com.instructure.pandautils.compose.animations.ScreenSlideTransition
import com.instructure.pandautils.features.inbox.compose.composables.InboxComposeScreen
import com.instructure.pandautils.features.inbox.compose.contextpicker.ContextPickerScreen
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
        val animationLabel = "ScreenSlideTransition"
        return ComposeView(requireActivity()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                val contextPickerState by viewModel.contextPickerUiState.collectAsState()
                val recipientPickerState by viewModel.recipientPickerUiState.collectAsState()

                AnimatedContent(
                    label = animationLabel,
                    targetState = uiState.screenOption,
                    transitionSpec = {
                        when(uiState.screenOption) {
                            is InboxComposeScreenOptions.None -> {
                                ScreenSlideBackTransition
                            }
                            is InboxComposeScreenOptions.ContextPicker -> {
                                ScreenSlideTransition
                            }
                            is InboxComposeScreenOptions.RecipientPicker -> {
                                ScreenSlideTransition
                            }
                        }
                    }
                ) { screenOption ->
                    when (screenOption) {
                        InboxComposeScreenOptions.None -> {
                            InboxComposeScreen(
                                title = stringResource(id = R.string.new_message),
                                uiState = uiState
                            ) { action ->
                                viewModel.handleAction(action)
                            }
                        }

                        InboxComposeScreenOptions.ContextPicker -> {
                            ContextPickerScreen(
                                uiState = contextPickerState
                            ) { action ->
                                viewModel.handleAction(action)
                            }
                        }

                        InboxComposeScreenOptions.RecipientPicker -> {
                            RecipientPickerScreen(
                                title = stringResource(id = R.string.select_recipients),
                                uiState = recipientPickerState
                            ) { action ->
                                viewModel.handleAction(action)
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
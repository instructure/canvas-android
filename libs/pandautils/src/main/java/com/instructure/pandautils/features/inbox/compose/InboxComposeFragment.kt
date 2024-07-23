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
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Recipient
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.pandautils.R
import com.instructure.pandautils.features.inbox.compose.composables.InboxComposeScreen
import com.instructure.pandautils.features.inbox.coursepicker.CoursePickerFragment
import com.instructure.pandautils.features.inbox.recipientpicker.RecipientPickerFragment
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler

class InboxComposeFragment : Fragment(), FragmentInteractions, CoursePickerFragment.CoursePickerListener, RecipientPickerFragment.RecipientPickerListener {

    private val viewModel: InboxComposeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireActivity()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()

                InboxComposeScreen(
                    uiState = uiState,
                    openCoursePickerSelected = {
                        val fragment = CoursePickerFragment(this@InboxComposeFragment)
                        fragment.show(requireActivity().supportFragmentManager, CoursePickerFragment::javaClass.name)
                    },
                    openRecipientPickerSelected = {
                        val fragment = RecipientPickerFragment(this@InboxComposeFragment)
                        fragment.show(requireActivity().supportFragmentManager, CoursePickerFragment::javaClass.name)
                    },
                    onDismiss = { activity?.supportFragmentManager?.popBackStack() }
                )
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

    override fun onCourseSelected(context: CanvasContext) {
        Log.d("InboxComposeFragment", "onCourseSelected: $context")
    }

    override fun selectedRecipientsChanged(recipients: List<Recipient>) {
        Log.d("InboxComposeFragment", "selectedRecipientsChanged: $recipients")
    }
}
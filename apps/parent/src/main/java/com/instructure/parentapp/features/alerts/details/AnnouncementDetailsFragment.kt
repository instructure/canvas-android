package com.instructure.parentapp.features.alerts.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.instructure.pandautils.utils.ViewStyler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnnouncementDetailsFragment : Fragment() {

    private val viewModel: AnnouncementDetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireActivity()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                ViewStyler.setStatusBarDark(requireActivity(), uiState.studentColor)
                AnnouncementDetailsScreen(
                    uiState,
                    viewModel::handleAction,
                    navigationActionClick = {
                        findNavController().popBackStack()
                    }
                )
            }
        }
    }

    companion object {
        const val COURSE_ID = "course-id"
        const val ANNOUNCEMENT_ID = "announcement-id"
    }
}

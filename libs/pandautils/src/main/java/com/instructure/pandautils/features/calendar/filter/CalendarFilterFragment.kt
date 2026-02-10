package com.instructure.pandautils.features.calendar.filter

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.instructure.pandautils.base.BaseCanvasBottomSheetDialogFragment
import com.instructure.pandautils.features.calendar.CalendarSharedEvents
import com.instructure.pandautils.features.calendar.filter.composables.CalendarFiltersScreen
import com.instructure.pandautils.utils.WindowInsetsHelper
import com.instructure.pandautils.utils.collectOneOffEvents
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CalendarFilterFragment : BaseCanvasBottomSheetDialogFragment() {

    private val viewModel: CalendarFilterViewModel by viewModels()

    @Inject
    lateinit var sharedEvents: CalendarSharedEvents

    override fun isFullScreen() = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewLifecycleOwner.lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)
        return ComposeView(requireActivity()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                val actionHandler = { action: CalendarFilterAction -> viewModel.handleAction(action) }
                CalendarFiltersScreen(uiState = uiState, actionHandler = actionHandler, navigationActionClick = {
                    dismiss()
                })
            }
        }
    }

    private fun handleAction(action: CalendarFilterViewModelAction) {
        when (action) {
            is CalendarFilterViewModelAction.FiltersClosed -> {
                sharedEvents.filtersClosed(lifecycleScope, action.changed)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set status bar icons to light (white) for visibility over the dialog
        WindowInsetsHelper.setStatusBarAppearance(requireActivity().window, isLightStatusBar = false)
        setFullScreen()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.filtersClosed()
    }

    private fun setFullScreen() {
        val sheetContainer = requireView().parent as? ViewGroup ?: return
        sheetContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        (dialog as? BottomSheetDialog)?.behavior?.isDraggable = false
    }

    companion object {

        fun newInstance(): CalendarFilterFragment {
            return CalendarFilterFragment()
        }
    }
}
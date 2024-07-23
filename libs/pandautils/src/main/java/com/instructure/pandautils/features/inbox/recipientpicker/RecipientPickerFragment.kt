package com.instructure.pandautils.features.inbox.recipientpicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.instructure.canvasapi2.models.Recipient
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecipientPickerFragment(
    private val listener: RecipientPickerListener? = null
): BottomSheetDialogFragment(), FragmentInteractions {
    private val viewModel: RecipientPickerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (listener != null) {
            viewModel.listener = listener
        }
        return ComposeView(requireActivity()).apply {
            setContent {
                RecipientPickerScreen(
                    title = title(),
                    onNavigateBack = { dismiss() },
                    viewModel = viewModel,
                    selectedRecipientsChanged = { recipients ->
                        viewModel.listener?.selectedRecipientsChanged(recipients)
                    }
                )
            }
        }
    }

    override val navigation: Navigation?
        get() = activity as? Navigation

    override fun title(): String = getString(R.string.select_recipients)

    override fun applyTheme() {
        ViewStyler.setStatusBarDark(requireActivity(), ThemePrefs.primaryColor)
    }

    override fun getFragment(): Fragment {
        return this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewStyler.setStatusBarLight(requireActivity())
        setFullScreen()
    }

    private fun setFullScreen() {
        val sheetContainer = requireView().parent as? ViewGroup ?: return
        sheetContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        (dialog as? BottomSheetDialog)?.behavior?.isDraggable = false
    }

    interface RecipientPickerListener {
        fun selectedRecipientsChanged(recipients: List<Recipient>)
    }
}
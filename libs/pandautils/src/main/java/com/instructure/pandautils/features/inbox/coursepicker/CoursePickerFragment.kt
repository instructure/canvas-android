package com.instructure.pandautils.features.inbox.coursepicker

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
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CoursePickerFragment: BottomSheetDialogFragment(), FragmentInteractions {
    private val viewModel: CoursePickerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireActivity()).apply {
            setContent {
                CoursePickerScreen(
                    coursePickerViewModel = viewModel,
                    onContextSelected = { context ->
                        // Do something with the selected context
                    }
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
}
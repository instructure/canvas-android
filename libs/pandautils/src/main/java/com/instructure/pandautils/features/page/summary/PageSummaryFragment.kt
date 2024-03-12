package com.instructure.pandautils.features.page.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.utils.StringArg
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PageSummaryFragment : BottomSheetDialogFragment() {

    private val pageName by StringArg("pageName")
    private val pageSummary by StringArg("pageSummary")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SummaryScreen(pageName, pageSummary) {
                    dismiss()
                }
            }
        }
    }

    companion object {
        fun newInstance(pageName: String, pageSummary: String): PageSummaryFragment {
            return PageSummaryFragment().apply {
                arguments = Bundle().apply {
                    putString("pageName", pageName)
                    putString("pageSummary", pageSummary)
                }
            }
        }
    }
}
/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.pandautils.features.inbox.list.filter

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_CANVAS_CONTEXT_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.databinding.FragmentContextFilterBinding
import com.instructure.pandautils.features.inbox.list.InboxSharedViewModel
import com.instructure.pandautils.utils.ParcelableArrayListArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.setMenu
import com.instructure.pandautils.utils.setupAsCloseButton
import com.instructure.pandautils.utils.withArgs
import dagger.hilt.android.AndroidEntryPoint

private const val CANVAS_CONTEXTS = "canvasContexts"

@AndroidEntryPoint
@ScreenView(SCREEN_VIEW_CANVAS_CONTEXT_LIST)
class ContextFilterFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentContextFilterBinding

    private val viewModel: ContextFilterViewModel by viewModels()

    private val sharedViewModel: InboxSharedViewModel by activityViewModels()

    private val canvasContexts by ParcelableArrayListArg<CanvasContext>(key = CANVAS_CONTEXTS)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentContextFilterBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        viewModel.setFilterItems(canvasContexts)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setupAsCloseButton {
            dismiss()
        }

        binding.toolbar.setMenu(R.menu.menu_clear_filter) {
            if (it.itemId == R.id.clear) {
                sharedViewModel.clearFilter()
                dismiss()
            }
        }

        ViewStyler.themeToolbarLight(requireActivity(), binding.toolbar)
        setFullScreen()

        viewModel.events.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                sharedViewModel.selectContextId(it)
                dismiss()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        sharedViewModel.filterDialogDismissed() // We need to react to dialog dismiss to set the status bar color correctly
    }

    private fun setFullScreen() {
        val sheetContainer = requireView().parent as? ViewGroup ?: return
        sheetContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        (dialog as? BottomSheetDialog)?.behavior?.isDraggable = false
    }

    companion object {

        fun newInstance(canvasContexts: List<CanvasContext>): ContextFilterFragment {
            val bundle = Bundle().apply {
                putParcelableArrayList(CANVAS_CONTEXTS, ArrayList(canvasContexts))
            }
            return ContextFilterFragment().withArgs(bundle)
        }
    }
}
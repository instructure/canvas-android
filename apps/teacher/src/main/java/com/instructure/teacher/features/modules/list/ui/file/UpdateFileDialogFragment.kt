/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 */

package com.instructure.teacher.features.modules.list.ui.file

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.widget.CompoundButtonCompat
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.pandautils.base.BaseCanvasBottomSheetDialogFragment
import com.instructure.pandautils.dialogs.DatePickerDialogFragment
import com.instructure.pandautils.dialogs.TimePickerDialogFragment
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.children
import com.instructure.pandautils.utils.color
import com.instructure.teacher.databinding.FragmentDialogUpdateFileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpdateFileDialogFragment : BaseCanvasBottomSheetDialogFragment() {

    private val canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)

    private lateinit var binding: FragmentDialogUpdateFileBinding

    private val viewModel: UpdateFileViewModel by viewModels()

    override fun isFullScreen() = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDialogUpdateFileBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.events.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        }

        setRadioButtonColors()

        binding.updateButton.setTextColor(canvasContext.color)
    }

    private fun setRadioButtonColors() = with(binding) {
        val radioButtonColor = ViewStyler.makeColorStateListForRadioGroup(
            requireContext().getColor(com.instructure.pandautils.R.color.textDarkest), canvasContext.color
        )

        val radioButtons =
            availabilityRadioGroup.children.filterIsInstance<CompoundButton>() + visibilityRadioGroup.children.filterIsInstance<CompoundButton>()

        radioButtons.forEach {
            CompoundButtonCompat.setButtonTintList(it, radioButtonColor)
        }
    }

    private fun handleAction(event: UpdateFileEvent) {
        when (event) {
            is UpdateFileEvent.Close -> dismiss()
            is UpdateFileEvent.ShowDatePicker -> {
                val dialog = DatePickerDialogFragment.getInstance(
                    manager = childFragmentManager,
                    defaultDate = event.selectedDate,
                    minDate = event.minDate,
                    maxDate = event.maxDate,
                    callback = event.callback
                )
                dialog.show(childFragmentManager, "datePicker")
            }

            is UpdateFileEvent.ShowTimePicker -> {
                val dialog = TimePickerDialogFragment.getInstance(
                    childFragmentManager,
                    event.selectedDate,
                    event.callback
                )
                dialog.show(childFragmentManager, "timePicker")
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setOnShowListener {
                val bottomSheet = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                val behavior = BottomSheetBehavior.from(bottomSheet)
                bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                behavior.skipCollapsed = true
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.peekHeight = 0
                bottomSheet.parent.requestLayout()
            }
        }
    }

    companion object {

        fun newInstance(
            contentId: Long,
            contentDetails: ModuleContentDetails?,
            canvasContext: CanvasContext
        ): UpdateFileDialogFragment {
            return UpdateFileDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong("contentId", contentId)
                    putParcelable("contentDetails", contentDetails)
                    putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                }
            }

        }
    }
}
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
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentDialogUpdateFileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpdateFileDialogFragment : BottomSheetDialogFragment() {

    private val moduleId: Long by LongArg(key = "moduleId")
    private val moduleItemId: Long by LongArg(key = "moduleItemId")
    private val contentId: Long by LongArg(key = "contentId")
    private val contentDetails: ModuleContentDetails? by NullableParcelableArg(key = "contentDetails")

    private lateinit var binding: FragmentDialogUpdateFileBinding

    private val viewModel: UpdateFileViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDialogUpdateFileBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contentDetails?.let {
            viewModel.loadData(it, contentId)
        }

        viewModel.events.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        }
    }

    private fun handleAction(event: UpdateFileEvent) {
        when (event) {
            is UpdateFileEvent.Close -> dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setOnShowListener {
                val bottomSheet = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                val behavior = BottomSheetBehavior.from(bottomSheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    companion object {

        fun newInstance(
            moduleId: Long,
            moduleItemId: Long,
            contentId: Long,
            contentDetails: ModuleContentDetails?
        ): UpdateFileDialogFragment {
            return UpdateFileDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong("moduleId", moduleId)
                    putLong("moduleItemId", moduleItemId)
                    putLong("contentId", contentId)
                    putParcelable("contentDetails", contentDetails)
                }
            }

        }
    }
}
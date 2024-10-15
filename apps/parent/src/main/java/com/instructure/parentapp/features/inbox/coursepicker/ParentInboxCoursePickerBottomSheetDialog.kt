/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.features.inbox.coursepicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.parentapp.features.inbox.coursepicker.composables.ParentInboxCoursePickerScreen
import com.instructure.parentapp.util.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ParentInboxCoursePickerBottomSheetDialog: BottomSheetDialogFragment() {
    @Inject
    lateinit var navigation: Navigation

    private val viewModel: ParentInboxCoursePickerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                ParentInboxCoursePickerScreen(uiState = uiState) { action ->
                    when (action) {
                        is ParentInboxCoursePickerAction.StudentContextSelected -> {
                            var options = InboxComposeOptions.buildNewMessage()
                            options = options.copy(
                                defaultValues = options.defaultValues.copy(
                                    contextCode = action.studentContextItem.course.courseCode,
                                    contextName = action.studentContextItem.course.name,
                                ),
                                disabledFields = options.disabledFields.copy(
                                    isContextDisabled = true
                                )
                            )
                            val route = navigation.inboxComposeRoute(options)
                            navigation.navigate(activity, route)
                        }
                    }

                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Landscape fix, make sure the bottom sheet is fully expanded
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val dialog = dialog as? BottomSheetDialog
            dialog?.let {
                val bottomSheet =
                    dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as? FrameLayout
                bottomSheet?.let {
                    val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    behavior.peekHeight = 0
                }
            }
        }
    }
}
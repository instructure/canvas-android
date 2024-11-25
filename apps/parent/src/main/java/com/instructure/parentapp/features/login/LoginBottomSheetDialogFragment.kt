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
package com.instructure.parentapp.features.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.instructure.parentapp.features.login.createaccount.CreateAccountActivity
import com.instructure.parentapp.util.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginBottomSheetDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var navigation: Navigation

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                LoginBottomSheetDialogScreen(
                    onHaveAccountClick = ::onHaveAccountClick,
                    onNotHaveAccountClick = ::onNotHaveAccountClick
                )
            }
        }
    }

    private fun onHaveAccountClick() {
        requireActivity().startActivity(
            Intent(
                requireActivity(),
                ParentLoginWithQRActivity::class.java
            )
        )
    }

    private fun onNotHaveAccountClick() {
        requireActivity().startActivity(
            Intent(
                requireActivity(),
                CreateAccountActivity::class.java
            )
        )
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
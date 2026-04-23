/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.pandautils.features.cookieconsent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.instructure.pandautils.base.BaseCanvasBottomSheetDialogFragment
import com.instructure.pandautils.compose.CanvasTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CookieConsentBottomSheetDialogFragment : BaseCanvasBottomSheetDialogFragment() {

    private val viewModel: CookieConsentViewModel by viewModels()

    private val fromSettings: Boolean
        get() = arguments?.getBoolean(ARG_FROM_SETTINGS, false) ?: false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = fromSettings
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                CanvasTheme {
                    CookieConsentContent(uiState = uiState)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        expandBottomSheet(view)

        if (fromSettings) {
            viewModel.showFromSettings()
        } else {
            viewModel.checkAndShowIfNeeded()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    state.consentResult?.let {
                        state.onConsentResultHandled()
                        dismissAllowingStateLoss()
                    }
                }
            }
        }
    }

    private fun expandBottomSheet(view: View) {
        val listener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val dialog = dialog as? BottomSheetDialog ?: return
                val bottomSheet = dialog.findViewById<FrameLayout>(
                    R.id.design_bottom_sheet
                ) ?: return
                val behavior = BottomSheetBehavior.from(bottomSheet)
                behavior.skipCollapsed = true
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.peekHeight = 0
            }
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
    }

    companion object {
        const val TAG = "CookieConsentBottomSheet"
        private const val ARG_FROM_SETTINGS = "fromSettings"

        fun newInstance(fromSettings: Boolean = false): CookieConsentBottomSheetDialogFragment {
            return CookieConsentBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_FROM_SETTINGS, fromSettings)
                }
            }
        }
    }
}
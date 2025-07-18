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
package com.instructure.parentapp.features.addstudent.qr

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.instructure.loginapi.login.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_QR_PAIRING
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.parentapp.features.addstudent.AddStudentAction
import com.instructure.parentapp.features.addstudent.AddStudentViewModel
import com.instructure.parentapp.features.addstudent.AddStudentViewModelAction
import com.instructure.parentapp.features.login.createaccount.CreateAccountActivity
import com.instructure.parentapp.util.navigation.Navigation
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@ScreenView(SCREEN_VIEW_QR_PAIRING)
@AndroidEntryPoint
class QrPairingFragment : BaseCanvasFragment() {

    @Inject
    lateinit var navigation: Navigation

    private val viewModel: AddStudentViewModel by activityViewModels()

    private val barcodeLauncher: ActivityResultLauncher<ScanOptions> =
        registerForActivityResult(ScanContract()) {
            if (it.contents == null) return@registerForActivityResult

            val uri = Uri.parse(it.contents)
            val code = uri.getQueryParameter("code")
            if (code != null) {
                if (requireActivity() is CreateAccountActivity) {
                    val domain = uri.host
                    val accountId = uri.getQueryParameter("account_id")
                    if (!domain.isNullOrBlank() && !accountId.isNullOrBlank()) {
                        navigation.navigate(
                            requireActivity(),
                            navigation.createAccount(domain, accountId, code)
                        )
                    }
                } else {
                    lifecycleScope.launch {
                        viewModel.handleAction(AddStudentAction.PairStudent(code))
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                QrPairingScreen(
                    uiState = uiState,
                    onNextClicked = this@QrPairingFragment::onNextClicked,
                    onBackClicked = { requireActivity().onBackPressed() })
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAddStudentAction)

        ViewStyler.themeStatusBar(requireActivity())
    }

    private fun handleAddStudentAction(action: AddStudentViewModelAction) {
        when (action) {
            is AddStudentViewModelAction.PairStudentSuccess -> {
                requireActivity().onBackPressed()
            }
            else -> {}
        }
    }

    private fun onNextClicked() {
        barcodeLauncher.launch(
            ScanOptions()
                .setPrompt(getString(R.string.qrCodePairingPromptCanvas))
                .setOrientationLocked(true)
                .setBeepEnabled(false)
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.handleAction(AddStudentAction.ResetError)
    }
}
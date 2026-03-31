/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.loginapi.login.features.cookieconsent

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.activity.compose.setContent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.instructure.pandautils.base.BaseCanvasActivity
import com.instructure.pandautils.features.cookieconsent.AnalyticsConsentHandler
import com.instructure.pandautils.features.cookieconsent.CookieConsentContent
import com.instructure.pandautils.features.cookieconsent.CookieConsentViewModel
import com.instructure.pandautils.utils.EdgeToEdgeHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CookieConsentActivity : BaseCanvasActivity() {

    private val viewModel: CookieConsentViewModel by viewModels()

    @Inject
    lateinit var router: CookieConsentRouter

    @Inject
    lateinit var analyticsConsentHandler: AnalyticsConsentHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EdgeToEdgeHelper.enableEdgeToEdge(this)

        viewModel.checkAndShowIfNeeded()

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            CookieConsentContent(
                uiState = uiState,
            )
        }

        observeUiState()
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    state.errorMessage?.let {
                        Toast.makeText(this@CookieConsentActivity, it, Toast.LENGTH_SHORT).show()
                        state.onErrorDismissed()
                    }

                    state.consentResult?.let { result ->
                        if (result.needed) {
                            if (result.consentGiven) {
                                analyticsConsentHandler.onConsentGranted()
                            } else {
                                analyticsConsentHandler.onConsentRevoked()
                            }
                        }
                        state.onConsentResultHandled()
                        proceedToApp()
                    }
                }
            }
        }
    }

    private fun proceedToApp() {
        router.startApp()
        finish()
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing - consent is required before proceeding
    }
}
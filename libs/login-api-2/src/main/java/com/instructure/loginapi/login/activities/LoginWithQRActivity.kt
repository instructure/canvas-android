/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.loginapi.login.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.instructure.pandautils.base.BaseCanvasActivity
import androidx.core.content.ContextCompat
import com.google.zxing.integration.android.IntentIntegrator
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.loginapi.login.R
import com.instructure.loginapi.login.databinding.ActivityLoginWithQrBinding
import com.instructure.loginapi.login.dialog.NoInternetConnectionDialog
import com.instructure.loginapi.login.util.QRLogin
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.setMenu
import com.instructure.pandautils.utils.setupAsBackButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

abstract class LoginWithQRActivity : BaseCanvasActivity() {

    protected abstract fun launchApplicationWithQRLogin(loginUri: Uri)

    private val binding by viewBinding(ActivityLoginWithQrBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        bindViews()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        // Capture the results from the QR scanner
        if (result?.contents != null) {
            val loginUri = Uri.parse(result.contents)
            if(QRLogin.verifySSOLoginUri(loginUri)) {
                // Valid link, let's launch it
                launchApplicationWithQRLogin(loginUri)
            } else {
                Toast.makeText(this, R.string.invalidQRCodeError, Toast.LENGTH_LONG).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun bindViews() = with(binding) {
        toolbar.apply {
            applyTopSystemBarInsets()
            title = getString(R.string.locateQRCode)
            setupAsBackButton { finish() }
            navigationIcon?.isAutoMirrored = true
            setMenu(R.menu.menu_next) {
                if (APIHelper.hasNetworkConnection()) {
                    val integrator = IntentIntegrator(this@LoginWithQRActivity)
                    integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                    integrator.setOrientationLocked(true)
                    integrator.setPrompt(getString(R.string.qrCodeScanningPrompt))
                    integrator.setBeepEnabled(false)
                    integrator.initiateScan()
                } else {
                    NoInternetConnectionDialog.show(supportFragmentManager)
                }
            }

            // Configure a11y for toolbar
            setNavigationContentDescription(R.string.close)
            ViewStyler.themeStatusBar(this@LoginWithQRActivity)
        }

        val nextText: TextView = findViewById(R.id.next)
        nextText.setTextColor(ContextCompat.getColor(this@LoginWithQRActivity, R.color.textInfo))

        // Apply bottom insets to the image (last element in scrollable content)
        ViewCompat.setOnApplyWindowInsetsListener(image) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = insets.bottom)
            windowInsets
        }
    }
}
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
package com.instructure.student.mobius.settings.pairobserver.ui

import android.app.Activity
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.google.zxing.BarcodeFormat
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.loginapi.login.dialog.NoInternetConnectionDialog
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.utils.applyHorizontalSystemBarInsets
import com.instructure.student.R
import com.instructure.student.databinding.FragmentPairObserverBinding
import com.instructure.student.mobius.common.ui.MobiusView
import com.instructure.student.mobius.settings.pairobserver.PairObserverEvent
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.spotify.mobius.functions.Consumer

class PairObserverView(inflater: LayoutInflater, parent: ViewGroup) :
    MobiusView<PairObserverViewState, PairObserverEvent, FragmentPairObserverBinding>(
        inflater,
        FragmentPairObserverBinding::inflate,
        parent
    ) {

    init {
        binding.root.applyHorizontalSystemBarInsets()
        binding.toolbar.setupAsBackButton { (context as? Activity)?.onBackPressed() }
        binding.toolbar.applyTopSystemBarInsets()
    }

    override fun applyTheme() {
        ViewStyler.themeToolbarColored(
            context as Activity,
            binding.toolbar,
            ThemePrefs.primaryColor,
            ThemePrefs.primaryTextColor
        )
    }

    override fun onConnect(output: Consumer<PairObserverEvent>) {
        binding.pairObserverRefresh.setOnClickListener {
            if (APIHelper.hasNetworkConnection()) {
                output.accept(PairObserverEvent.RefreshCode)
            } else {
                (context as? FragmentActivity)?.supportFragmentManager?.let { NoInternetConnectionDialog.show(it) }
            }
        }
    }

    override fun render(state: PairObserverViewState) {
        when (state) {
            is PairObserverViewState.Loading -> renderLoading()
            is PairObserverViewState.Failed -> renderFailed()
            is PairObserverViewState.Loaded -> renderPairingCode(state.domain, state.pairingCode, state.accountId)
        }.exhaustive
    }

    override fun onDispose() {}

    private fun renderLoading() {
        binding.pairObserverLoading.setVisible()
        binding.pairObserverContent.setGone()
        binding.errorContainer.setGone()
    }

    private fun renderFailed() {
        binding.pairObserverLoading.setGone()
        binding.pairObserverContent.setGone()
        binding.errorContainer.setVisible()
    }

    private fun renderPairingCode(domain: String, pairingCode: String, accountId: Long) {
        binding.pairObserverLoading.setGone()
        binding.pairObserverContent.setVisible()
        binding.errorContainer.setGone()
        binding.pairObserverCode.text = pairingCode

        try {
            // Open the Parent App with relevant data so it can pair the student
            val content = "canvas-parent://$domain/pair?code=$pairingCode&account_id=$accountId"
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 100, 100)
            binding.pairObserverQrCode.setImageBitmap(bitmap)
        } catch (e: Exception) {
            binding.pairObserverQrCode.setImageResource(R.drawable.ic_warning)
        }
    }
}

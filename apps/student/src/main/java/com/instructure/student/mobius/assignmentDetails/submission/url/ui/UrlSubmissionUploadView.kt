/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.mobius.assignmentDetails.submission.url.ui

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebViewClient
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyBottomSystemBarInsets
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.onChangeDebounce
import com.instructure.pandautils.utils.setMenu
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.student.R
import com.instructure.student.databinding.FragmentUrlSubmissionUploadBinding
import com.instructure.student.mobius.assignmentDetails.submission.url.UrlSubmissionUploadEvent
import com.instructure.student.mobius.common.ui.MobiusView
import com.spotify.mobius.functions.Consumer

class UrlSubmissionUploadView(inflater: LayoutInflater, parent: ViewGroup) : MobiusView<UrlSubmissionUploadViewState, UrlSubmissionUploadEvent, FragmentUrlSubmissionUploadBinding>(
    inflater,
    FragmentUrlSubmissionUploadBinding::inflate,
    parent) {

    init {
        binding.toolbar.setupAsBackButton { (context as? Activity)?.onBackPressed() }
        binding.toolbar.title = context.getString(R.string.websiteUrl)
        binding.toolbar.applyTopSystemBarInsets()
        binding.urlPreviewWebView.applyBottomSystemBarInsets()

        binding.urlPreviewWebView.webViewClient = WebViewClient()
        binding.urlPreviewWebView.settings.javaScriptEnabled = true
        binding.urlPreviewWebView.setOnTouchListener { _, _ -> true } // Prevent the user from interacting with the WebView
        binding.urlPreviewWebView.isVerticalScrollBarEnabled = false
    }

    override fun onConnect(output: Consumer<UrlSubmissionUploadEvent>) = with(binding) {
        toolbar.setMenu(R.menu.menu_submit_generic) {
            when (it.itemId) {
                R.id.menuSubmit -> {
                    output.accept(UrlSubmissionUploadEvent.SubmitClicked(editUrl.text.toString()))
                }
            }
        }

        toolbar.menu.findItem(R.id.menuSubmit).isEnabled = false

        editUrl.onChangeDebounce(URL_MINIMUM_LENGTH, DELAY) {
            output.accept(UrlSubmissionUploadEvent.UrlChanged(it))
        }
    }

    override fun render(state: UrlSubmissionUploadViewState) {
        binding.editUrl.hint = state.urlHint
        binding.toolbar.menu.findItem(R.id.menuSubmit).isEnabled = state.submitEnabled
        binding.errorMsg.setVisible(state.isFailure).text = state.failureText
        binding.divider.setVisible(state.isFailure)
    }

    override fun onDispose() {}

    override fun applyTheme() {
        ViewStyler.themeToolbarLight(context as Activity, binding.toolbar)
    }

    fun showPreviewUrl(url: String) {
        binding.urlPreviewWebView.setVisible()
        binding.urlPreviewWebView.loadUrl(url)
    }

    fun setInitialUrl(url: String?) {
        binding.editUrl.setText(url ?: "")
    }

    fun goBack() {
        (context as? Activity)?.onBackPressed()
    }

    companion object {
        private const val DELAY = 400L
        private const val URL_MINIMUM_LENGTH = 3
    }
}
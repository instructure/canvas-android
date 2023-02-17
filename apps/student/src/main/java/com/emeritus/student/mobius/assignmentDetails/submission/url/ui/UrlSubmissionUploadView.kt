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
package com.emeritus.student.mobius.assignmentDetails.submission.url.ui

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebViewClient
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.utils.*
import com.emeritus.student.R
import com.emeritus.student.mobius.assignmentDetails.submission.url.UrlSubmissionUploadEvent
import com.emeritus.student.mobius.common.ui.MobiusView
import com.emeritus.student.mobius.common.ui.SubmissionService
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.fragment_url_submission_upload.*
import kotlinx.android.synthetic.main.fragment_url_submission_upload.toolbar

class UrlSubmissionUploadView(inflater: LayoutInflater, parent: ViewGroup) : MobiusView<UrlSubmissionUploadViewState, UrlSubmissionUploadEvent>(R.layout.fragment_url_submission_upload, inflater, parent) {

    init {
        toolbar.setupAsBackButton { (context as? Activity)?.onBackPressed() }
        toolbar.title = context.getString(R.string.websiteUrl)

        urlPreviewWebView.webViewClient = WebViewClient()
        urlPreviewWebView.settings.javaScriptEnabled = true
        urlPreviewWebView.setOnTouchListener { _, _ -> true } // Prevent the user from interacting with the WebView
        urlPreviewWebView.isVerticalScrollBarEnabled = false
    }

    override fun onConnect(output: Consumer<UrlSubmissionUploadEvent>) {
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
        editUrl.hint = state.urlHint
        toolbar.menu.findItem(R.id.menuSubmit).isEnabled = state.submitEnabled
        errorMsg.setVisible(state.isFailure).text = state.failureText
        errorDivider.setVisible(state.isFailure)
    }

    override fun onDispose() {}
    override fun applyTheme() {}

    fun showPreviewUrl(url: String) {
        urlPreviewWebView?.setVisible()
        urlPreviewWebView.loadUrl(url)
    }

    fun setInitialUrl(url: String?) {
        editUrl.setText(url ?: "")
    }

    fun onSubmitUrl(course: CanvasContext, assignmentId: Long, assignmentName: String?, url: String) {
        SubmissionService.startUrlSubmission(context, course, assignmentId, assignmentName, url)

        (context as? Activity)?.onBackPressed()
    }

    companion object {
        private const val DELAY = 400L
        private const val URL_MINIMUM_LENGTH = 3
    }
}
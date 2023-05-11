/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.emeritus.student.mobius.assignmentDetails.submissionDetails.content

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.instructure.pandautils.analytics.SCREEN_VIEW_QUIZ_SUBMISSION_VIEW
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.setDarkModeSupport
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setInvisible
import com.instructure.pandautils.utils.setVisible
import com.emeritus.student.R
import com.emeritus.student.fragment.InternalWebviewFragment
import kotlinx.android.synthetic.main.fragment_webview.*
import kotlinx.android.synthetic.main.fragment_webview.view.*

@ScreenView(SCREEN_VIEW_QUIZ_SUBMISSION_VIEW)
class QuizSubmissionViewFragment : InternalWebviewFragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        getCanvasLoading()?.setVisible() // Set visible so we can test it
        canvasWebViewWrapper.webView.setDarkModeSupport()
        canvasWebViewWrapper.webView.setInitialScale(100)
        canvasWebViewWrapper.setInvisible() // Set invisible so we can test it
        canvasWebViewWrapper.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (!isAdded) return

                // Update visibilities
                if (newProgress >= 100) {
                    getCanvasLoading()?.setGone()
                    canvasWebViewWrapper.setVisible()
                } else {
                    getCanvasLoading()?.announceForAccessibility(getString(R.string.loading))
                }
            }
        }
        super.onActivityCreated(savedInstanceState)
    }

    companion object {
        fun newInstance(quizUrl: String): QuizSubmissionViewFragment {
            return QuizSubmissionViewFragment().apply {
                arguments = Bundle().apply {
                    putString(Const.INTERNAL_URL, quizUrl)
                    putBoolean(Const.HIDDEN_TOOLBAR, true)
                    putBoolean(Const.AUTHENTICATE, true)
                    putBoolean(SHOULD_ROUTE_INTERNALLY, false)
                }
            }
        }
    }
}
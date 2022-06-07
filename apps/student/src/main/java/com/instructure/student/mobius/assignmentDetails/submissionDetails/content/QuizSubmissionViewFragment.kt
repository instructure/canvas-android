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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.content

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
import com.instructure.student.R
import com.instructure.student.fragment.InternalWebviewFragment
import kotlinx.android.synthetic.main.fragment_webview.*

@ScreenView(SCREEN_VIEW_QUIZ_SUBMISSION_VIEW)
class QuizSubmissionViewFragment : InternalWebviewFragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        getCanvasLoading()?.setVisible() // Set visible so we can test it
        canvasWebView?.setDarkModeSupport()
        canvasWebView.setInitialScale(100)
        canvasWebView.setInvisible() // Set invisible so we can test it
        canvasWebView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (!isAdded) return

                // Update visibilities
                if (newProgress >= 100) {
                    getCanvasLoading()?.setGone()
                    canvasWebView.setVisible()
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
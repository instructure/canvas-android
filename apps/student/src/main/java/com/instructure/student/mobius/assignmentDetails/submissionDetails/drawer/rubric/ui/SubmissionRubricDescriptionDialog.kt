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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.student.R
import com.instructure.student.activity.InternalWebViewActivity
import com.instructure.student.router.RouteMatcher
import kotlinx.android.synthetic.main.dialog_submission_rubric_description.view.*

class SubmissionRubricDescriptionDialog : DialogFragment() {

    var mDescription by StringArg()
    var mLongDescription by StringArg()

    init {
        retainInstance = true
    }

    companion object {
        @JvmStatic
        fun show(manager: FragmentManager, description: String, longDescription: String) =
            SubmissionRubricDescriptionDialog().apply {
                manager.dismissExisting<SubmissionRubricDescriptionDialog>()
                mDescription = description
                mLongDescription = longDescription
                show(manager, javaClass.simpleName)
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val content = View.inflate(requireContext(), R.layout.dialog_submission_rubric_description, null)

        with(content) {
            // Show progress bar while loading description
            progressBar.setVisible()
            progressBar.announceForAccessibility(getString(R.string.loading))
            webView.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    if (newProgress >= 100) {
                        progressBar?.setGone()
                        webView?.setVisible()
                    }
                }
            }

            webView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
                override fun openMediaFromWebView(mime: String?, url: String?, filename: String?) {}
                override fun onPageStartedCallback(webView: WebView?, url: String?) {}
                override fun onPageFinishedCallback(webView: WebView?, url: String?) {}
                override fun canRouteInternallyDelegate(url: String?): Boolean =
                    RouteMatcher.canRouteInternally(requireContext(), url!!, ApiPrefs.domain, false)

                override fun routeInternallyCallback(url: String?) {
                    RouteMatcher.canRouteInternally(requireContext(), url!!, ApiPrefs.domain, true)
                }
            }

            webView.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
                override fun launchInternalWebViewFragment(url: String) = requireActivity().startActivity(
                    InternalWebViewActivity.createIntent(requireActivity(), url, "", true)
                )

                override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
            }

            // make the WebView background transparent
            setBackgroundResource(android.R.color.transparent)

            // Load description
            webView.loadHtml(mLongDescription, mDescription)
        }

        return AlertDialog.Builder(requireContext())
            .setCancelable(true)
            .setTitle(mDescription)
            .setView(content)
            .setPositiveButton(getString(android.R.string.ok).toUpperCase(), null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ThemePrefs.buttonColor)
                }
            }
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }

}

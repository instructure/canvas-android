/*
* Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.teacher.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import com.instructure.pandautils.blueprint.BaseCanvasDialogFragment
import androidx.fragment.app.FragmentManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.analytics.SCREEN_VIEW_CRITERION_LONG_DESCRIPTION
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.pandautils.views.HtmlFormatColors
import com.instructure.teacher.R
import com.instructure.teacher.activities.InternalWebViewActivity
import com.instructure.teacher.databinding.DialogCriterionLongDescriptionBinding
import com.instructure.teacher.router.RouteMatcher
import java.util.*

@ScreenView(SCREEN_VIEW_CRITERION_LONG_DESCRIPTION)
class CriterionLongDescriptionDialog : BaseCanvasDialogFragment() {

    var mDescription by StringArg()
    var mLongDescription by StringArg()

    init {
        retainInstance = true
    }

    companion object {
        fun show(manager: FragmentManager, description: String, longDescription: String) = CriterionLongDescriptionDialog().apply {
            manager.dismissExisting<CriterionLongDescriptionDialog>()
            mDescription = description
            mLongDescription = longDescription
            show(manager, javaClass.simpleName)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogCriterionLongDescriptionBinding.inflate(layoutInflater)

        with(binding) {
            // Show progress bar while loading description
            progressBar.setVisible()
            progressBar.announceForAccessibility(getString(R.string.loading))
            webView.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    if (newProgress >= 100) {
                        progressBar.setGone()
                        webView.setVisible()
                    }
                }
            }

            webView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
                override fun openMediaFromWebView(mime: String, url: String, filename: String) {}
                override fun onPageStartedCallback(webView: WebView, url: String) {}
                override fun onPageFinishedCallback(webView: WebView, url: String) {}
                override fun canRouteInternallyDelegate(url: String): Boolean = RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, false)
                override fun routeInternallyCallback(url: String) {
                    RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, true)
                }
            }

            webView.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
                override fun launchInternalWebViewFragment(url: String) = requireActivity().startActivity(InternalWebViewActivity.createIntent(requireActivity(), url, "", true))
                override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
            }

            // make the WebView background transparent
            root.setBackgroundResource(android.R.color.transparent)

            // Load description
            webView.loadHtml(mLongDescription, mDescription, htmlFormatColors = HtmlFormatColors(backgroundColorRes = R.color.backgroundLightestElevated))
        }

        return AlertDialog.Builder(requireContext())
                .setCancelable(true)
                .setTitle(mDescription)
                .setView(binding.root)
                .setPositiveButton(getString(android.R.string.ok).uppercase(Locale.getDefault()), null)
                .create()
                .apply {
                    setOnShowListener {
                        getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
                    }
                }
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }
}

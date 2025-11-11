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

package com.instructure.parentapp.features.webview

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.instructure.pandautils.analytics.SCREEN_VIEW_SIMPLE_WEB_VIEW
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.NullableStringArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.pandautils.utils.enableAlgorithmicDarkening
import com.instructure.pandautils.utils.launchCustomTab
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.studentColor
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.parentapp.R
import com.instructure.parentapp.databinding.FragmentSimpleWebviewBinding
import com.instructure.parentapp.util.ParentPrefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@ScreenView(SCREEN_VIEW_SIMPLE_WEB_VIEW)
@AndroidEntryPoint
class SimpleWebViewFragment : BaseCanvasFragment(), NavigationCallbacks {

    @Inject
    lateinit var parentPrefs: ParentPrefs

    private val binding by viewBinding(FragmentSimpleWebviewBinding::bind)

    private val viewModel: SimpleWebViewViewModel by viewModels()

    private var title: String? by NullableStringArg(key = Const.TITLE)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_simple_webview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyTheme()
        lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)
        lifecycleScope.launch {
            viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collectLatest {
                when (it) {
                    is ViewState.Loading -> binding.loading.setVisible()
                    else -> binding.loading.setGone()
                }
            }
        }

        savedInstanceState?.let {
            binding.webView.restoreState(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.webView.saveState(outState)
    }

    override fun onHandleBackPressed() = binding.webView.handleGoBack()

    private fun handleAction(action: SimpleWebViewAction) {
        when (action) {
            is SimpleWebViewAction.LoadWebView -> {
                loadWebView(action.url, action.limitWebAccess)
            }

            is SimpleWebViewAction.LaunchCustomTab -> {
                launchCustomTab(action.url)
            }

            is SimpleWebViewAction.ShowError -> {
                toast(com.instructure.pandautils.R.string.errorOccurred)
                activity?.onBackPressed()
            }
        }
    }

    private fun applyTheme() = with(binding) {
        webView.setPadding(0, 0, 0, 0)
        toolbar.applyTopSystemBarInsets()
        toolbar.title = title.orEmpty()
        toolbar.setupAsBackButton(this@SimpleWebViewFragment)
        ViewStyler.themeToolbarColored(
            requireActivity(),
            toolbar,
            parentPrefs.currentStudent.studentColor,
            requireActivity().getColor(R.color.textLightest)
        )
    }

    private fun loadWebView(mainUrl: String, limitWebAccess: Boolean) = with(binding) {
        webViewProgress.indeterminateTintList = ColorStateList.valueOf(parentPrefs.currentStudent.studentColor)
        webView.enableAlgorithmicDarkening()
        webView.setInitialScale(100)
        webView.addVideoClient(requireActivity())
        webView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                if (!limitWebAccess) {
                    viewModel.downloadFile(mime, url, filename)
                }
            }

            override fun onPageFinishedCallback(webView: WebView, url: String) {
                webViewProgress.setGone()
                if (limitWebAccess) {
                    showAlertJavascript(webView)
                }
            }

            override fun onPageStartedCallback(webView: WebView, url: String) {
                webViewProgress.setVisible()
            }

            override fun canRouteInternallyDelegate(url: String): Boolean = true

            override fun routeInternallyCallback(url: String) {
                if (mainUrl.startsWith(url) || !limitWebAccess) {
                    webView.loadUrl(url)
                }
            }
        }

        webView.loadUrl(mainUrl)
    }

    private fun launchCustomTab(url: String) {
        activity?.let {
            it.launchCustomTab(url, parentPrefs.currentStudent.studentColor)
            Handler(Looper.getMainLooper()).postDelayed({
                it.onBackPressed()
            }, 500)
        }
    }

    private fun showAlertJavascript(webView: WebView, infoText: String = getString(R.string.webAccessLimitedMessage)) {
        val showAlertJavaScrip = """
            const floatNode = `<div id="flash_message_holder_mobile" style="z-index: 10000; position: fixed; bottom: 0; left: 0; right: 0; margin: 16px; width: auto;">
                <div class="ic-flash-info" aria-hidden="true" style="width: unset; max-width: 475px">
                    <div class="ic-flash__icon">
                        <i class="icon-info"></i>
                    </div>
                    $infoText
                    <button type="button" class="Button Button--icon-action close_link">
                        <i class="icon-x"></i>
                    </button>
                </div>
            </div>`
    
            $(floatNode)
                .appendTo($("body")[0])
                .on("click", "button", event => {
                    $("#flash_message_holder_mobile").remove()
                })
            """

        webView.evaluateJavascript(showAlertJavaScrip, null)
    }
}

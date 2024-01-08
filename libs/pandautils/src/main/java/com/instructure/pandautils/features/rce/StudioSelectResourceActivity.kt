/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 *
 */

package com.instructure.pandautils.features.rce

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.managers.ExternalToolManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.databinding.ActivityStudioSelectResourceBinding
import com.instructure.pandautils.views.CanvasWebView.CanvasWebViewClientCallback
import kotlinx.coroutines.launch
import java.net.URLEncoder

class StudioSelectResourceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudioSelectResourceBinding
    private val canvasContext: CanvasContext? by lazy { intent.getParcelableExtra(CANVAS_CONTEXT) }
    private val toolId: Long by lazy { intent.getLongExtra(TOOL_ID, -1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudioSelectResourceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWebView()
        launchLTI()
    }

    override fun onResume() {
        super.onResume()
        binding.studioWebView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.studioWebView.onPause()
    }

    private fun launchLTI() {
        val url = "${ApiPrefs.fullDomain}/${canvasContext?.apiContext()}/${canvasContext?.id}/external_tools/$toolId/resource_selection"
        binding.studioWebView.loadUrl(url, getReferer())
    }

    private fun setupWebView() {
        binding.studioWebView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.displayZoomControls = false
            settings.setSupportZoom(true)
            settings.userAgentString = ApiPrefs.userAgent
            addVideoClient(this@StudioSelectResourceActivity)
            setInitialScale(100)

            canvasWebViewClientCallback = object : CanvasWebViewClientCallback {
                override fun openMediaFromWebView(mime: String, url: String, filename: String) {}

                override fun onPageStartedCallback(webView: WebView, url: String) {}

                override fun onPageFinishedCallback(webView: WebView, url: String) {
                    if (url.contains("success/external_tool_dialog")) {
                        var title = ""
                        var retrievedUrl = ""
                        webView.evaluateJavascript("ENV.retrieved_data[0].url") {
                            Log.d("RETRIEVED_DATA", "onPageFinishedCallback: $it")
                            if (it.isNotEmpty()) {
                                val encodedUrl = URLEncoder.encode(it.substring(1, it.length-1), "UTF-8")
                                val embedUrl = "${ApiPrefs.fullDomain}/${canvasContext?.apiContext()}/${canvasContext?.id}/external_tools/retrieve?display=borderless&amp;url=" + encodedUrl
                                setResult(RESULT_OK, Intent().apply {
                                    putExtra(RESULT_TITLE, title)
                                    putExtra(RESULT_URL, embedUrl)
                                })
                                finish()
                            }
                        }

                    }
                }

                override fun routeInternallyCallback(url: String) {}

                override fun canRouteInternallyDelegate(url: String): Boolean {
                    return false
                }
            }
        }
    }

    private fun getReferer(): Map<String, String> = mutableMapOf(Pair("Referer", ApiPrefs.domain))

    companion object {
        const val RESULT_TITLE = "result.title"
        const val RESULT_URL = "result.url"
        const val CANVAS_CONTEXT = "canvasContext"
        const val TOOL_ID = "toolId"

        fun createIntent(context: Context, canvasContext: CanvasContext, toolId: Long): Intent {
            return Intent(context, StudioSelectResourceActivity::class.java).apply {
                putExtra(CANVAS_CONTEXT, canvasContext)
                putExtra(TOOL_ID, toolId)
            }
        }
    }
}
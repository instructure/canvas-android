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

package com.instructure.teacher.features.rce

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.teacher.fragments.InternalWebViewFragment

class StudioResourceSelectFragment : InternalWebViewFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.canvasWebView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {

            override fun openMediaFromWebView(mime: String, url: String, filename: String) {

            }

            override fun onPageStartedCallback(webView: WebView, url: String) {
                Log.d("ASDFASDF", "onPageStartedCallback: $url")
            }

            override fun onPageFinishedCallback(webView: WebView, url: String) {
                Log.d("ASDFASDF", "onPageStartedCallback: $url")
            }

            override fun routeInternallyCallback(url: String) {
            }

            override fun canRouteInternallyDelegate(url: String): Boolean {
                return false
            }
        }
    }
}
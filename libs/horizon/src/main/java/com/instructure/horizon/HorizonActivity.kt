/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.instructure.horizon.horizonui.HorizonTheme
import com.instructure.horizon.navigation.HorizonNavigation
import com.instructure.pandautils.base.BaseCanvasActivity
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.WebViewAuthenticator
import com.instructure.pandautils.utils.getActivityOrNull
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HorizonActivity : BaseCanvasActivity() {

    @Inject
    lateinit var webViewAuthenticator: WebViewAuthenticator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val activity = LocalContext.current.getActivityOrNull()
            if (activity != null) ViewStyler.setStatusBarColor(activity, ContextCompat.getColor(activity, R.color.surface_pagePrimary))
            HorizonTheme {
                HorizonNavigation(rememberNavController())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        webViewAuthenticator.authenticateWebViews(lifecycleScope, this)
    }
}
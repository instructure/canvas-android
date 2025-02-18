/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.base

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.instructure.pandautils.analytics.ScreenViewAnnotationProcessor
import com.instructure.pandautils.analytics.pageview.PageViewAnnotationProcessor
import com.instructure.pandautils.analytics.pageview.PageViewUtils
import com.instructure.pandautils.utils.LocaleUtils
import com.instructure.pandautils.utils.showMasqueradeNotification
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Base activity for all Canvas activities that contains cross-cutting concerns like analytics, locale and masquerading.
 * All activities should extend this class to ensure that these concerns are handled consistently across the app.
 */
open class BaseCanvasActivity : AppCompatActivity() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PageViewFragmentDelegateEntryPoint {
        fun pageViewUtils(): PageViewUtils
    }

    private val pageViewAnnotationProcessor by lazy {
        val pageViewUtils = EntryPoints.get(
            applicationContext,
            PageViewFragmentDelegateEntryPoint::class.java
        ).pageViewUtils()
        PageViewAnnotationProcessor(this::class.java, this, pageViewUtils)
    }

    override fun attachBaseContext(base: Context?) {
        val newBase = if (base != null) LocaleUtils.wrapContext(base) else base
        super.attachBaseContext(newBase)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        showMasqueradeNotification()
    }

    override fun onResume() {
        pageViewAnnotationProcessor.startEvent()
        ScreenViewAnnotationProcessor.processScreenView(this::class.java)

        super.onResume()
    }

    override fun onPause() {
        pageViewAnnotationProcessor.stopEvent()
        super.onPause()
    }
}
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
package com.instructure.pandautils.blueprint

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.instructure.pandautils.utils.ScreenViewAnnotationProcessor
import com.instructure.pandautils.utils.showMasqueradeNotification

open class BaseCanvasActivity : AppCompatActivity() {

    override fun attachBaseContext(base: Context?) {
        val newBase = if (base != null) LocaleUtils.wrapContext(base) else base
        super.attachBaseContext(newBase)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        showMasqueradeNotification()
    }

    override fun onResume() {
        super.onResume()
        ScreenViewAnnotationProcessor.processScreenView(this::class.java)
    }
}
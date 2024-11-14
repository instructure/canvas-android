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
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import com.instructure.pandautils.utils.AppConfigProvider
import com.instructure.pandautils.utils.ScreenViewAnnotationProcessor
import com.instructure.pandautils.utils.showMasqueradeNotification

open class BaseCanvasAppCompatDialogFragment : AppCompatDialogFragment() {

    override fun onStart() {
        super.onStart()
        showMasqueradeNotification()
    }

    override fun onAttach(context: Context) {
        if (AppConfigProvider.appConfig?.appName == "teacher") {
            ScreenViewAnnotationProcessor.processScreenView(this::class.java)
        }
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (AppConfigProvider.appConfig?.appName == "student" && isAdded && isVisible && userVisibleHint) {
            ScreenViewAnnotationProcessor.processScreenView(this::class.java)
        }
        super.onViewCreated(view, savedInstanceState)
    }
}
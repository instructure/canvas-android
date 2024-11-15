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
import androidx.fragment.app.DialogFragment
import com.instructure.canvasapi2.utils.pageview.PageViewVisibilityTracker
import com.instructure.canvasapi2.utils.pageview.PageViewWindowFocus
import com.instructure.canvasapi2.utils.pageview.PageViewWindowFocusListener
import com.instructure.pandautils.utils.AppConfigProvider
import com.instructure.pandautils.utils.PageViewAnnotationProcessor
import com.instructure.pandautils.utils.ScreenViewAnnotationProcessor
import com.instructure.pandautils.utils.showMasqueradeNotification

open class BaseCanvasDialogFragment : DialogFragment(), PageViewWindowFocus {

    override fun onStart() {
        super.onStart()
        showMasqueradeNotification()
    }

    private val visibilityTracker = PageViewVisibilityTracker()
    private val pageViewAnnotationProcessor = PageViewAnnotationProcessor(this::class.java, this)

    open fun beforePageViewPrerequisites(): List<String> = emptyList()

    protected fun completePageViewPrerequisite(prerequisite: String) {
        if (visibilityTracker.trackCustom(prerequisite, true, this)) {
            pageViewAnnotationProcessor.startEvent()
        }
    }

    override fun onAttach(context: Context) {
        visibilityTracker.addCustomConditions(beforePageViewPrerequisites())
        if (AppConfigProvider.appConfig?.appName == "teacher") {
            ScreenViewAnnotationProcessor.processScreenView(this::class.java)
        }
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (AppConfigProvider.appConfig?.appName == "student" && isAdded && isVisible && userVisibleHint) {
            ScreenViewAnnotationProcessor.processScreenView(this::class.java)
        }

        view.viewTreeObserver.addOnWindowFocusChangeListener(PageViewWindowFocusListener(this))
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (visibilityTracker.trackHidden(hidden, this)) {
            pageViewAnnotationProcessor.startEvent()
        } else {
            pageViewAnnotationProcessor.stopEvent()
        }
        super.onHiddenChanged(hidden)
    }

    @Deprecated("See description in superclass")
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (visibilityTracker.trackHidden(isVisibleToUser, this)) {
            pageViewAnnotationProcessor.startEvent()
        } else {
            pageViewAnnotationProcessor.stopEvent()
        }
        super.setUserVisibleHint(isVisibleToUser)
    }

    override fun onResume() {
        if (visibilityTracker.trackResume(true, this)) {
            pageViewAnnotationProcessor.startEvent()
        }
        super.onResume()
    }

    override fun onPause() {
        visibilityTracker.trackResume(false, this)
        pageViewAnnotationProcessor.stopEvent()
        super.onPause()
    }

    override fun onPageViewWindowFocusChanged(hasFocus: Boolean) {
        if (visibilityTracker.trackCustom("windowFocus", hasFocus, this)) {
            pageViewAnnotationProcessor.startEvent()
        } else {
            pageViewAnnotationProcessor.stopEvent()
        }
    }
}
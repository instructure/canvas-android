/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use fragment file except in compliance with the License.
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
import android.view.View
import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.utils.pageview.PageViewVisibilityTracker
import com.instructure.canvasapi2.utils.pageview.PageViewWindowFocus
import com.instructure.canvasapi2.utils.pageview.PageViewWindowFocusListener
import com.instructure.pandautils.analytics.PageViewAnnotationProcessor
import com.instructure.pandautils.analytics.ScreenViewAnnotationProcessor

class PageViewFragmentDelegate<T>(
    private val fragment: T
) where T : Fragment, T : PageViewWindowFocus, T : PageViewPrerequisites {

    private val visibilityTracker = PageViewVisibilityTracker()
    private val pageViewAnnotationProcessor = PageViewAnnotationProcessor(fragment::class.java, fragment)

    fun completePageViewPrerequisite(prerequisite: String) {
        if (visibilityTracker.trackCustom(prerequisite, true, fragment)) {
            pageViewAnnotationProcessor.startEvent()
        }
    }

    fun onAttach(context: Context) {
        visibilityTracker.addCustomConditions(fragment.beforePageViewPrerequisites())
        if (AppConfigProvider.appConfig?.appName == "teacher") {
            ScreenViewAnnotationProcessor.processScreenView(fragment::class.java)
        }
    }

    fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (AppConfigProvider.appConfig?.appName == "student" && fragment.isAdded && fragment.isVisible && fragment.userVisibleHint) {
            ScreenViewAnnotationProcessor.processScreenView(fragment::class.java)
        }

        view.viewTreeObserver.addOnWindowFocusChangeListener(PageViewWindowFocusListener(fragment))
    }

    fun onHiddenChanged(hidden: Boolean) {
        if (visibilityTracker.trackHidden(hidden, fragment)) {
            pageViewAnnotationProcessor.startEvent()
        } else {
            pageViewAnnotationProcessor.stopEvent()
        }
    }

    @Deprecated("See description in superclass")
    fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (visibilityTracker.trackHidden(isVisibleToUser, fragment)) {
            pageViewAnnotationProcessor.startEvent()
        } else {
            pageViewAnnotationProcessor.stopEvent()
        }
    }

    fun onResume() {
        if (visibilityTracker.trackResume(true, fragment)) {
            pageViewAnnotationProcessor.startEvent()
        }
    }

    fun onPause() {
        visibilityTracker.trackResume(false, fragment)
        pageViewAnnotationProcessor.stopEvent()
    }

    fun onPageViewWindowFocusChanged(hasFocus: Boolean) {
        if (visibilityTracker.trackCustom("windowFocus", hasFocus, fragment)) {
            pageViewAnnotationProcessor.startEvent()
        } else {
            pageViewAnnotationProcessor.stopEvent()
        }
    }
}
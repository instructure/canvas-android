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

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.utils.pageview.PageViewWindowFocus
import com.instructure.pandautils.analytics.pageview.PageViewUtils
import javax.inject.Inject

/**
 * Base class for all Canvas Fragments that contains cross-cutting concerns like analytics.
 * All classes should extend this that would normally extend [Fragment] to ensure that these concerns are handled consistently across the app.
 */
open class BaseCanvasFragment : Fragment(), PageViewWindowFocus, PageViewPrerequisites {

    @Inject
    lateinit var pageViewUtils: PageViewUtils

    private val delegate by lazy { PageViewFragmentDelegate(this, pageViewUtils) }

    override fun beforePageViewPrerequisites(): List<String> = emptyList()

    protected fun completePageViewPrerequisite(prerequisite: String) {
        delegate.completePageViewPrerequisite(prerequisite)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        delegate.onViewCreated(view, savedInstanceState)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        delegate.onHiddenChanged(hidden)
        super.onHiddenChanged(hidden)
    }

    @Deprecated("See description in superclass")
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        delegate.setUserVisibleHint(isVisibleToUser)
        super.setUserVisibleHint(isVisibleToUser)
    }

    override fun onResume() {
        delegate.onResume()
        super.onResume()
    }

    override fun onPause() {
        delegate.onPause()
        super.onPause()
    }

    override fun onPageViewWindowFocusChanged(hasFocus: Boolean) {
        delegate.onPageViewWindowFocusChanged(hasFocus)
    }
}
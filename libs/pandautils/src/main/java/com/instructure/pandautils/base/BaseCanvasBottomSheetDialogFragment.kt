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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.instructure.pandautils.analytics.pageview.PageViewWindowFocus
import com.instructure.pandautils.utils.showMasqueradeNotification

open class BaseCanvasBottomSheetDialogFragment : BottomSheetDialogFragment(), PageViewWindowFocus, PageViewPrerequisites {

    open fun isFullScreen() = false

    override fun onStart() {
        super.onStart()
        showMasqueradeNotification()
    }

    private val delegate by lazy { PageViewFragmentDelegate(this) }

    override fun beforePageViewPrerequisites(): List<String> = emptyList()

    protected fun completePageViewPrerequisite(prerequisite: String) {
        delegate.completePageViewPrerequisite(prerequisite)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        delegate.onCreate()
        super.onCreate(savedInstanceState)
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
/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.pandautils.features.notes.webview

import android.content.Context
import android.util.AttributeSet
import android.view.ActionMode
import android.view.View
import com.instructure.pandautils.views.CanvasWebViewWrapper

class NotesHighlightingCanvasWebViewWrapper(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
): CanvasWebViewWrapper(context, attrs, defStyleAttr) {

    private var callback: ActionMode.Callback? = null

    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        callback: ActionMode.Callback? = null
    ): this(context, attrs, defStyleAttr) {
        this.callback = callback
    }

    override fun startActionMode(callback: ActionMode.Callback?, type: Int): ActionMode {
        return super.startActionMode(this.callback, type)
    }

    override fun startActionMode(callback: ActionMode.Callback?): ActionMode {
        return super.startActionMode(this.callback)
    }

    override fun startActionModeForChild(
        originalView: View?,
        callback: ActionMode.Callback?
    ): ActionMode {
        return super.startActionModeForChild(originalView, this.callback)
    }

    override fun startActionModeForChild(
        originalView: View?,
        callback: ActionMode.Callback?,
        type: Int
    ): ActionMode {
        return super.startActionModeForChild(originalView, this.callback, type)
    }
}

/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.features.notebook.common.webview

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
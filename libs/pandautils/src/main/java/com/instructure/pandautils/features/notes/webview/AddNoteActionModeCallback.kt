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

import android.graphics.Rect
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.instructure.pandautils.utils.toPx
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AddNoteActionModeCallback(
    lifecycleOwner: LifecycleOwner,
    private val selectionLocation: Flow<SelectionLocation>,
    private val menuItems: () -> List<ActionMenuItem>
): ActionMode.Callback2() {

    private var menuLocation: SelectionLocation? = null

    init {
        lifecycleOwner.lifecycleScope.launch {
            selectionLocation.collectLatest {
                menuLocation = it
            }
        }
    }


    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        menuItems().forEach {
            menu?.add(Menu.NONE, it.id, Menu.NONE, it.label)
        }
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        menuItems().firstOrNull { it.id == item?.itemId }?.onClick?.invoke()
        mode?.finish()
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {}

    override fun onGetContentRect(mode: ActionMode?, view: View?, outRect: Rect?) {
        menuLocation?.let {
            outRect?.set(
                it.left.toInt().toPx,
                it.top.toInt().toPx,
                it.right.toInt().toPx,
                it.bottom.toInt().toPx
            )
        }
    }
}

data class ActionMenuItem(
    val id: Int,
    val label: String,
    val onClick: () -> Unit
)

data class SelectionLocation(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

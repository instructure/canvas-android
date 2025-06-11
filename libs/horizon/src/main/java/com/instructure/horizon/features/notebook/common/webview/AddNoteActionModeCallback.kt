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
    private val menuItems: List<ActionMenuItem>
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
        menuItems.forEach {
            menu?.add(Menu.NONE, it.id, Menu.NONE, it.label)
        }
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        menuItems.firstOrNull { it.id == item?.itemId }?.onClick?.invoke()
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
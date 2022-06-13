/*
 * Copyright (C) 2020 - present Instructure, Inc.
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

package com.instructure.annotations

import android.content.Context
import com.pspdfkit.ui.toolbar.grouping.presets.MenuItem
import com.pspdfkit.ui.toolbar.grouping.presets.PresetMenuItemGroupingRule

class AnnotationCreationGroupingRule(context: Context) : PresetMenuItemGroupingRule(context) {
    override fun getGroupPreset(capacity: Int, itemsCount: Int): List<MenuItem> {
        return when {
            capacity >= 8 -> eightItemGrouping
            capacity == 7 -> sevenItemGrouping
            //if all else fails, return the smallest grouping we can
            else -> minItemGrouping
        }
    }

    override fun areGeneratedGroupItemsSelectable(): Boolean = true

    private val eightItemGrouping = listOf(
        MenuItem(R.id.grab_annotation),
        MenuItem(com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_stamp),
        MenuItem(com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_freetext),
        MenuItem(com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_group_markup,
            intArrayOf(
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_highlight,
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_strikeout)),
        MenuItem(com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_group_drawing,
            intArrayOf(
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_ink_pen,
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_square)),
        MenuItem(com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_eraser),
        MenuItem(com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_picker),
        MenuItem(com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_group_undo_redo,
            intArrayOf(
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_undo,
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_redo))
    )
    private val sevenItemGrouping = listOf(
        MenuItem(R.id.grab_annotation),
        MenuItem(com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_stamp),
        MenuItem(com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_group_markup,
            intArrayOf(
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_freetext,
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_highlight,
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_strikeout)),
        MenuItem(com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_group_drawing,
            intArrayOf(
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_ink_pen,
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_square)),
        MenuItem(com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_eraser),
        MenuItem(com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_picker),
        MenuItem(com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_group_undo_redo,
            intArrayOf(
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_undo,
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_redo))
    )
    private val minItemGrouping = listOf(
        MenuItem(R.id.grab_annotation),
        MenuItem(com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_group_markup,
            intArrayOf(
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_freetext,
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_stamp,
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_highlight,
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_strikeout)),
        MenuItem(com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_group_drawing,
            intArrayOf(
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_ink_pen,
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_square,
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_eraser)),
        MenuItem(com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_picker),
        MenuItem(com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_group_undo_redo,
            intArrayOf(
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_undo,
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_redo))
    )
}
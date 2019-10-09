/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
 */    package com.instructure.annotations

import android.content.Context
import android.util.LayoutDirection
import android.view.Menu
import android.view.View
import androidx.core.text.TextUtilsCompat
import com.pspdfkit.ui.toolbar.ContextualToolbarMenuItem
import com.pspdfkit.ui.toolbar.grouping.presets.MenuItem
import com.pspdfkit.ui.toolbar.grouping.presets.PresetMenuItemGroupingRule
import java.util.*

/**
 * This is only here to override the defaulting grouping for RTL, specifically undo/redo, once
 * PSPDFKit supports RTL this can be deleted
 */
class CanvasPdfMenuGrouping(context: Context) : PresetMenuItemGroupingRule(context) {

    override fun getGroupPreset(p0: Int, p1: Int): List<MenuItem> {
        return emptyList()
    }


    override fun groupMenuItems(toolbarMenuItems: MutableList<ContextualToolbarMenuItem>, capacity: Int): MutableList<ContextualToolbarMenuItem> {
        val isRTL = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == LayoutDirection.RTL
        //There are 7 items total, and always need to leave room for the color, it has to show.
        //First we need to get all of the items and store them in variables for readability.... rip
        var freeText: ContextualToolbarMenuItem? = null
        var stamp: ContextualToolbarMenuItem? = null
        var strikeOut: ContextualToolbarMenuItem? = null
        var highlight: ContextualToolbarMenuItem? = null
        var ink: ContextualToolbarMenuItem? = null
        var rectangle: ContextualToolbarMenuItem? = null
        var color: ContextualToolbarMenuItem? = null
        var undo: ContextualToolbarMenuItem? = null
        var redo: ContextualToolbarMenuItem? = null
        var eraser: ContextualToolbarMenuItem? = null

        for (item in toolbarMenuItems) {
            when (item.id) {
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_freetext -> {
                    freeText = item
                }
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_note -> {
                    stamp = item
                }
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_strikeout -> {
                    strikeOut = item
                }
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_highlight -> {
                    highlight = item
                }
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_ink_pen -> {
                    ink = item
                }
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_square -> {
                    rectangle = item
                }
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_picker -> {
                    color = item
                }
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_undo -> {
                    // There are two menu items called undo, we want the first one.
                    if (undo == null) undo = item
                }
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_redo -> {
                    redo = item
                }
                com.pspdfkit.R.id.pspdf__annotation_creation_toolbar_item_eraser -> {
                    eraser = item
                }
            }
        }

        //check to make sure we have all of our items
        if (freeText != null && stamp != null && strikeOut != null && highlight != null
                && ink != null && rectangle != null && undo != null && redo != null && color != null && eraser != null) {
            when {
                capacity >= 8 -> {
                    val inkGroup = ContextualToolbarMenuItem.createGroupItem(View.generateViewId(), ink.position, true, mutableListOf(ink, rectangle), ink)
                    return if (isRTL) {
                        mutableListOf(redo, undo, color, eraser, inkGroup, strikeOut, freeText, highlight, stamp)
                    } else {
                        mutableListOf(stamp, highlight, freeText, strikeOut, inkGroup, eraser, color, undo, redo)
                    }
                }
                capacity == 7 || capacity == 6  -> {
                    val inkGroup = ContextualToolbarMenuItem.createGroupItem(View.generateViewId(), ink.position, true, mutableListOf(ink, rectangle), ink)
                    val highlightGroup = ContextualToolbarMenuItem.createGroupItem(View.generateViewId(), highlight.position, true, mutableListOf(highlight, strikeOut), highlight)
                    return if (isRTL) {
                        mutableListOf(redo, undo, color, eraser, inkGroup, highlightGroup, freeText, stamp)
                    } else {
                        mutableListOf(stamp, freeText, highlightGroup, inkGroup, eraser, color, undo, redo)
                    }

                }
                capacity == 5 -> {
                    val inkGroup = ContextualToolbarMenuItem.createGroupItem(View.generateViewId(), ink.position, true, mutableListOf(ink, rectangle), ink)
                    val textGroup = ContextualToolbarMenuItem.createGroupItem(View.generateViewId(), freeText.position, true, mutableListOf(freeText, stamp, highlight, strikeOut), freeText)
                    return if (isRTL) {
                        mutableListOf(redo, undo, color, eraser, inkGroup, textGroup)
                    } else {
                        mutableListOf(textGroup, inkGroup, eraser, color, undo, redo)
                    }
                }
                capacity == 4 -> {
                    val inkGroup = ContextualToolbarMenuItem.createGroupItem(View.generateViewId(), ink.position, true, mutableListOf(ink, rectangle), ink)
                    val textGroup = ContextualToolbarMenuItem.createGroupItem(View.generateViewId(), freeText.position, true, mutableListOf(freeText, stamp, highlight, strikeOut), freeText)
                    val undoGroupList = if (isRTL) {
                        mutableListOf(redo, undo)
                    } else {
                        mutableListOf(undo, redo)
                    }
                    val undoGroup = ContextualToolbarMenuItem.createGroupItem(View.generateViewId(), undo.position, true, undoGroupList, if(isRTL) redo else undo)
                    return if (isRTL) {
                        mutableListOf(undoGroup, color, eraser, inkGroup, textGroup)
                    } else {
                        return mutableListOf(textGroup, inkGroup, eraser, color, undoGroup)
                    }
                }
                //if all else fails, return default grouping unchanged
                else -> {
                    return toolbarMenuItems
                }
            }
        } else {
            //if we dont have all items, just return the default that we have
            return toolbarMenuItems
        }
    }

}
/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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

package com.instructure.androidpolling.app.delegate

import android.view.View

/**
 * Generic type G is the type of the model object used to create group views
 * Generic type I is the type of the model object used to create row views
 * These must implement Comparable so the adapter can sort the rows/groups
 */
interface ExpandableListDelegate<in G, in I> {
    fun getChildCount(): Int
    fun isShowFirstItem(): Boolean
    fun getGroupViewForItem(groupItem: G, convertView: View?, groupPosition: Int, isExpanded: Boolean): View
    fun getRowViewForItem(item: I, convertView: View?, groupPosition: Int, childPosition: Int, isLastRowInGroup: Boolean, isLastRow: Boolean): View
    fun getChildType(item: I): Int
    fun showFirstItem(item: I)
}

/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.pandautils.binding

import com.instructure.pandautils.R
import com.instructure.pandautils.mvvm.ItemViewModel

private const val LOADING_ITEM_VIEW_TYPE = 1000

class LoadingItemViewModel : ItemViewModel {

    override val layoutId: Int = R.layout.item_loading

    override val viewType: Int = LOADING_ITEM_VIEW_TYPE
}
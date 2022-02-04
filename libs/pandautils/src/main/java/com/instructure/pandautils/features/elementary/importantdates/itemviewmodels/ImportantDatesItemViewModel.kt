/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.elementary.importantdates.itemviewmodels

import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.importantdates.ImportantDatesItemViewData
import com.instructure.pandautils.features.elementary.importantdates.ImportantDatesViewType
import com.instructure.pandautils.mvvm.ItemViewModel

class ImportantDatesItemViewModel(
        val data: ImportantDatesItemViewData,
        val open: (scheduleItemId: Long) -> Unit
) : ItemViewModel {
    override val layoutId: Int = R.layout.item_important_dates

    override val viewType: Int = ImportantDatesViewType.ITEM.viewType

    fun open() {
        open.invoke(data.scheduleItemId)
    }

}
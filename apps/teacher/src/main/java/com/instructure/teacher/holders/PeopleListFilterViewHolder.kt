/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.teacher.holders

import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.databinding.PeopleFilterAdapterItemBinding
import com.instructure.teacher.utils.getColorCompat

class PeopleListFilterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(
        canvasContext: CanvasContext,
        canvasContextIdList: ArrayList<Long>,
        callback: (canvasContext: CanvasContext, isChecked: Boolean) -> Unit,
        binding: PeopleFilterAdapterItemBinding
    ): Unit = with(binding) {
        val context = binding.root.context

        title.text = canvasContext.name
        checkbox.setOnCheckedChangeListener { _, isChecked: Boolean ->

            if (canvasContext.id != -1L) {
                callback.invoke(canvasContext, isChecked)
            }
        }

        checkbox.isChecked = canvasContextIdList.contains(canvasContext.id)
        checkbox.setVisible(canvasContext.id != -1L)
        ViewStyler.themeCheckBox(context, checkbox, ThemePrefs.brandColor)
        if (canvasContext.id == -1L) {
            title.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            //this is a header, so we want it a gray color
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            title.setTextColor(context.getColorCompat(R.color.textDark))
        } else {
            title.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            checkbox.contentDescription = context.getString(R.string.contentDescriptionFilterSection, canvasContext.name)
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            title.setTextColor(context.getColorCompat(R.color.textDarkest))
        }
    }
}

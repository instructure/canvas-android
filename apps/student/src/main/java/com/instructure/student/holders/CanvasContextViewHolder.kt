/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.student.holders

import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.utils.onClick
import com.instructure.student.R
import kotlinx.android.synthetic.main.viewholder_canvas_context_dialog.view.*

class CanvasContextViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {
        const val HOLDER_RES_ID = R.layout.viewholder_canvas_context_dialog
    }

    fun bind(canvasContext: CanvasContext, callback: (canvasContext: CanvasContext) -> Unit) = with(itemView) {
        title.text = canvasContext.name

        if (canvasContext.id == -1L) {
            // This is a header, so we want it a gray color
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            title.setTextColor(ContextCompat.getColor(context, R.color.defaultTextGray))
        } else {
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            title.setTextColor(ContextCompat.getColor(context, R.color.textDarkest))
        }

        onClick {
            // The headers have an id of -1
            if (canvasContext.id != -1L) callback(canvasContext)
        }
    }
}

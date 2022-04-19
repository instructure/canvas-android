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
import com.instructure.teacher.R
import com.instructure.teacher.dialog.CanvasContextListDialog
import com.instructure.teacher.utils.getColorCompat
import kotlinx.android.synthetic.main.canvas_context_spinner_adapter_item.view.*

class CanvasContextViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {
        const val HOLDER_RES_ID = R.layout.canvas_context_spinner_adapter_item
    }

    fun bind(canvasContext: CanvasContext, canvasContextListDialog: CanvasContextListDialog, callback: (canvasContext: CanvasContext) -> Unit) = with(itemView) {
        title.text = canvasContext.name

        if(canvasContext.id == -1L) {
            //this is a header, so we want it a gray color
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            title.setTextColor(context.getColorCompat(R.color.defaultTextGray))
        } else {
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            title.setTextColor(context.getColorCompat(R.color.textDarkest))
        }
        itemView.setOnClickListener {
            // The headers have an id of -1
            if(canvasContext.id != -1L) {
                callback.invoke(canvasContext)
                canvasContextListDialog.dismiss()
            }
        }
    }
}

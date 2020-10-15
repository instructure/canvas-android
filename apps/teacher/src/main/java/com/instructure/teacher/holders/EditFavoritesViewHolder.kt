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

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.utils.ColorUtils
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.teacher.R
import com.instructure.teacher.interfaces.AdapterToEditFavoriteCoursesCallback
import com.instructure.canvasapi2.utils.isValidTerm
import kotlinx.android.synthetic.main.adapter_edit_favorites.view.*
import java.util.*

class EditFavoritesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    companion object {
        const val HOLDER_RES_ID = R.layout.adapter_edit_favorites
    }

    fun bind(context: Context, canvasContext: CanvasContext, callback: AdapterToEditFavoriteCoursesCallback) = with(itemView) {

        title.text = canvasContext.name

        if (canvasContext is Course) {
            if (canvasContext.isFavorite) {
                star.setImageDrawable(ColorUtils.colorIt(ThemePrefs.brandColor, ContextCompat.getDrawable(context, R.drawable.ic_star_filled)!!))
                title.contentDescription = String.format(Locale.getDefault(), context.getString(R.string.favorited_content_description), canvasContext.name, context.getString(R.string.content_description_favorite))
            } else {
                star.setImageDrawable(ColorUtils.colorIt(ThemePrefs.brandColor, ContextCompat.getDrawable(context, R.drawable.ic_star)!!))
                title.contentDescription = String.format(Locale.getDefault(), context.getString(R.string.favorited_content_description), canvasContext.name, context.getString(R.string.content_description_not_favorite))
            }
        }

        itemView.setOnClickListener {
            if(canvasContext is Course) {
                if(canvasContext.isFavorite) {
                    callback.onRowClicked(canvasContext, false)
                } else {
                    callback.onRowClicked(canvasContext, true)
                }
            }
        }
    }
}

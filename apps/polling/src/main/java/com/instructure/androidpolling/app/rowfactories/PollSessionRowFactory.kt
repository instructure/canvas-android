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

package com.instructure.androidpolling.app.rowfactories

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.instructure.androidpolling.app.R
import kotlinx.android.synthetic.main.listview_item_poll_session.view.*

object PollSessionRowFactory {

    internal class ViewHolder(view: View) {
        var title: TextView = view.title
        var sectionName: TextView = view.sectionName
        var isPublished: ImageView = view.isPublished
    }

    fun buildRowView(layoutInflater: LayoutInflater, context: Context, courseName: String, sectionName: String, isPublished: Boolean, conView: View?): View {
        var convertView = conView
        val holder: ViewHolder?

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.listview_item_poll_session, null, false)

            holder = ViewHolder(convertView)

            convertView!!.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        holder.title.text = courseName
        holder.sectionName.text = sectionName
        if (isPublished) {
            holder.isPublished.visibility = View.VISIBLE
            holder.isPublished.setColorFilter(context.resources.getColor(R.color.canvaspollingtheme_color), PorterDuff.Mode.SRC_IN)
        } else {
            holder.isPublished.visibility = View.INVISIBLE
        }

        return convertView
    }
}
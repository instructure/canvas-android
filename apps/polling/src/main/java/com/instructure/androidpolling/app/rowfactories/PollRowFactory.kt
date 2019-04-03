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
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView

import com.instructure.androidpolling.app.R
import com.instructure.canvasapi2.utils.DateHelper
import kotlinx.android.synthetic.main.listview_group_open_polls.view.*
import kotlinx.android.synthetic.main.listview_item_poll_session_student.view.*

import java.util.Date

object PollRowFactory {

    internal class ViewHolder(view: View) {
        var title: TextView = view.title
        var sectionName: TextView = view.sectionName
        var createdDate: TextView = view.createdDate
    }

    internal class GroupViewHolder(view: View) {
        var groupText: TextView = view.groupText
    }

    fun buildRowView(inflater: LayoutInflater, sectionName: String, question: String, conView: View?, context: Context, createdAt: Date): View {
        var convertView = conView
        val holder: ViewHolder

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_item_poll_session_student, null)
            holder = ViewHolder(convertView)
            convertView!!.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        holder.title.text = question
        holder.sectionName.text = sectionName
        holder.createdDate.text = DateHelper.dateToDayMonthYearString(context, createdAt)

        return convertView
    }


    fun buildGroupView(inflater: LayoutInflater, groupName: String, conView: View?): View {
        var convertView = conView
        val holder: GroupViewHolder

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_group_open_polls, null)
            holder = GroupViewHolder(convertView)
            convertView!!.tag = holder
        } else {
            holder = convertView.tag as GroupViewHolder
        }

        holder.groupText.text = groupName

        return convertView
    }
}

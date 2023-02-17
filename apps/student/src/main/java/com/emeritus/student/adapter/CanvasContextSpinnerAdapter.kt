/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.emeritus.student.adapter

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.utils.backgroundColor
import com.emeritus.student.R
import java.util.*

class CanvasContextSpinnerAdapter(context: Context, private val mData: ArrayList<CanvasContext>) : ArrayAdapter<CanvasContext>(context, R.layout.canvas_context_spinner_adapter_item, mData) {
    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItem(position: Int): CanvasContext? = mData[position]
    override fun areAllItemsEnabled(): Boolean = false

    override fun isEnabled(position: Int): Boolean {
        var isEnabled = true
        if (mData[position].id == GROUP_SEPARATOR.toLong() || mData[position].id == COURSE_SEPARATOR.toLong()) {
            isEnabled = false
        }
        return isEnabled
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val viewHolder: CanvasContextViewHolder

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.canvas_context_spinner_adapter_item, parent, false)
            viewHolder = CanvasContextViewHolder()
            viewHolder.title = convertView!!.findViewById<View>(R.id.title) as TextView
            viewHolder.indicator = convertView.findViewById<View>(R.id.icon) as ImageView
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as CanvasContextViewHolder
        }

        val item = mData[position]
        if (item != null) {
            viewHolder.title!!.text = item.name
            viewHolder.indicator!!.visibility = View.VISIBLE
            viewHolder.indicator!!.background = createIndicatorBackground(item.backgroundColor)
        } else {
            viewHolder.indicator!!.visibility = View.GONE
            viewHolder.title!!.text = ""
        }

        return convertView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val viewHolder: CanvasContextViewHolder

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.canvas_context_spinner_adapter_item, parent, false)
            viewHolder = CanvasContextViewHolder()
            viewHolder.title = convertView!!.findViewById<View>(R.id.title) as TextView
            viewHolder.indicator = convertView.findViewById<View>(R.id.icon) as ImageView
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as CanvasContextViewHolder
        }

        val item = mData[position]

        if (item != null) {
            viewHolder.title!!.text = item.name

            if (item.id == GROUP_SEPARATOR.toLong() || item.id == COURSE_SEPARATOR.toLong()) {
                viewHolder.title!!.setTypeface(null, Typeface.BOLD)
                viewHolder.indicator!!.visibility = View.GONE
            } else {
                viewHolder.title!!.setTypeface(null, Typeface.NORMAL)
                viewHolder.indicator!!.visibility = View.VISIBLE
                viewHolder.indicator!!.background = createIndicatorBackground(item.backgroundColor)
            }
        }

        return convertView
    }

    private fun createIndicatorBackground(color: Int): ShapeDrawable {
        val circle = ShapeDrawable(OvalShape())
        circle.paint.color = color
        return circle
    }

    private class CanvasContextViewHolder {
        internal var title: TextView? = null
        internal var indicator: ImageView? = null
    }

    companion object {

        const val COURSE_SEPARATOR = -22222
        const val GROUP_SEPARATOR = -11111


        fun newAdapterInstance(context: Context, courses: List<Course>, groups: List<Group>): CanvasContextSpinnerAdapter {
            val canvasContexts = ArrayList<CanvasContext>()

            if (!courses.isEmpty()) {
                val courseSeparator = Course(
                        id = COURSE_SEPARATOR.toLong(),
                        name = context.getString(R.string.courses)
                )

                canvasContexts.add(courseSeparator)
                canvasContexts.addAll(courses)
            }
            if (!groups.isEmpty()) {
                val groupSeparator = Course(
                        id = GROUP_SEPARATOR.toLong(),
                        name = context.getString(R.string.groups)
                )

                canvasContexts.add(groupSeparator)
                canvasContexts.addAll(groups)
            }
            return CanvasContextSpinnerAdapter(context, canvasContexts)
        }
    }
}

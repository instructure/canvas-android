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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.pandautils.utils.setVisible
import com.emeritus.student.R

class TermSpinnerAdapter(
    context: Context,
    resource: Int,
    private val gradingPeriods: List<GradingPeriod>,
    private val showDropdownArrow: Boolean = true
) : ArrayAdapter<GradingPeriod>(context, resource, gradingPeriods) {

    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    var isLoading = false

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: TermSpinnerViewHolder
        val view: View

        if (convertView == null) {
            view = inflater.inflate(R.layout.term_spinner_view, parent, false)
            holder = TermSpinnerViewHolder(
                periodName = view.findViewById(R.id.periodName),
                dropDown = view.findViewById(R.id.dropDownArrow),
                progressBar = view.findViewById(R.id.progressBar)
            )
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as TermSpinnerViewHolder
        }

        holder.dropDown.setVisible(!isLoading && showDropdownArrow)
        holder.progressBar.setVisible(isLoading)
        holder.periodName.text = gradingPeriods[position].title

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: TermDropDownViewHolder
        val view: View

        if (convertView == null) {
            view = inflater.inflate(R.layout.spinner_row_grading_period, parent, false)
            holder = TermDropDownViewHolder(view.findViewById(R.id.periodName))
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as TermDropDownViewHolder
        }

        holder.periodName.text = gradingPeriods[position].title

        return view
    }

    fun getPositionForId(id: Long): Int {
        return (0 until count).indexOfFirst { getItem(it)?.id == id }
    }

    private data class TermSpinnerViewHolder(
        val periodName: TextView,
        val dropDown: ImageView,
        val progressBar: ProgressBar
    )

    private data class TermDropDownViewHolder(val periodName: TextView)
}

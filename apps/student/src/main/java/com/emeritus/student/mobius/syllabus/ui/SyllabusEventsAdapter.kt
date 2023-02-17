/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.emeritus.student.mobius.syllabus.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.setGone
import com.emeritus.student.R
import com.emeritus.student.mobius.syllabus.SyllabusEvent
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.viewholder_card_generic.view.*

class SyllabusEventsAdapter(val consumer: Consumer<SyllabusEvent>?) : RecyclerView.Adapter<SyllabusEventViewHolder>() {

    private var events: List<ScheduleItemViewState> = emptyList()

    fun updateEvents(events: List<ScheduleItemViewState>) {
        this.events = events
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SyllabusEventViewHolder {
        return SyllabusEventViewHolder(
            LayoutInflater.from(parent.context).inflate(
                SyllabusEventViewHolder.RESOURCE_ID,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: SyllabusEventViewHolder, position: Int) {
        holder.onBind(consumer, events[position])
    }
}

class SyllabusEventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun onBind(consumer: Consumer<SyllabusEvent>?, event: ScheduleItemViewState) {
        with (itemView) {
            description.setGone()
            points.setGone()

            title.text = event.title
            date.text = event.date
            icon.setImageDrawable(ColorKeeper.getColoredDrawable(context, event.iconRes, event.color))

            setOnClickListener { consumer?.accept(SyllabusEvent.SyllabusItemClicked(event.id)) }
        }
    }

    companion object {
        const val RESOURCE_ID = R.layout.viewholder_card_generic
    }
}
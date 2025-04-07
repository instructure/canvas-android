/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.teacher.features.syllabus.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.onClick
import com.instructure.teacher.databinding.ViewholderSyllabusItemBinding
import com.instructure.teacher.features.syllabus.SyllabusEvent
import com.spotify.mobius.functions.Consumer

class SyllabusEventsAdapter(private val consumer: Consumer<SyllabusEvent>?) : RecyclerView.Adapter<SyllabusEventsAdapter.SyllabusEventViewHolder>() {

    private var events: List<ScheduleItemViewState> = emptyList()

    fun updateEvents(events: List<ScheduleItemViewState>) {
        this.events = events
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SyllabusEventViewHolder {
        val binding = ViewholderSyllabusItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SyllabusEventViewHolder(binding)
    }

    override fun getItemCount(): Int = events.size

    override fun onBindViewHolder(holder: SyllabusEventViewHolder, position: Int) {
        holder.onBind(consumer, events[position])
    }

    inner class SyllabusEventViewHolder(private val binding: ViewholderSyllabusItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(consumer: Consumer<SyllabusEvent>?, event: ScheduleItemViewState) {
            with(binding) {
                syllabusItemTitle.text = event.title
                syllabusItemDate.text = event.date
                syllabusItemIcon.setImageDrawable(ColorKeeper.getColoredDrawable(root.context, event.iconRes, event.color))
                root.onClick { consumer?.accept(SyllabusEvent.SyllabusItemClicked(event.id)) }
            }
        }
    }
}

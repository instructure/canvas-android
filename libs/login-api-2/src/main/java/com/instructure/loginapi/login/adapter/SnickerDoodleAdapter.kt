/*
 * Copyright (C) 2018 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.instructure.loginapi.login.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.instructure.loginapi.login.R
import com.instructure.loginapi.login.snicker.SnickerDoodle

class SnickerDoodleAdapter(
    private val snickerDoodles: List<SnickerDoodle>,
    private val onSelected: (snickerDoodle: SnickerDoodle) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.adapter_snicker_doodle, parent, false)
        return object : RecyclerView.ViewHolder(v){}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val snickerDoodle = snickerDoodles[position]
        holder.itemView.findViewById<TextView>(R.id.title).text = snickerDoodle.title
        holder.itemView.findViewById<TextView>(R.id.subtitle).text = snickerDoodle.subtitle
        holder.itemView.setOnClickListener { onSelected(snickerDoodles[holder.adapterPosition]) }
    }

    override fun getItemCount(): Int = snickerDoodles.size
}

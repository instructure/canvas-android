/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.androidfoosball.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.holders.FoosRankViewHolder
import com.instructure.androidfoosball.ktmodels.User


class FoosRankLeaderboardAdapter (private val mContext: Context, private val mUsers: List<User>, val foosRankCallback: (User) -> Unit) : RecyclerView.Adapter<FoosRankViewHolder>() {

    override fun getItemCount(): Int {
        return mUsers.size
    }

    override fun onBindViewHolder(holder: FoosRankViewHolder, position: Int) {
        holder.bind(mContext, mUsers[position], position, foosRankCallback)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoosRankViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_foos_rank, parent, false)
        return FoosRankViewHolder(view)
    }



}

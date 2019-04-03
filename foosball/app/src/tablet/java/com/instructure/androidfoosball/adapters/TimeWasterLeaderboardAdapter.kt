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
import com.instructure.androidfoosball.App
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.holders.TimeWasterViewHolder
import com.instructure.androidfoosball.ktmodels.Game
import com.instructure.androidfoosball.ktmodels.GameStatus
import com.instructure.androidfoosball.ktmodels.User

class TimeWasterLeaderboardAdapter(private val mContext: Context, private val mUsers: List<User>) : RecyclerView.Adapter<TimeWasterViewHolder>() {

    private val averageRoundTime by lazy {
        val games = App.realm.where(Game::class.java).equalTo("status", GameStatus.FINISHED.name).findAll().toList()
        val rounds = games.flatMap { it.rounds }.filter { it.startTime > 0 && it.endTime > 0 }
        val totalTime = rounds.sumBy { ((it.endTime - it.startTime) / 1000).toInt() }
        totalTime / games.size.toDouble() / 3600
    }

    override fun getItemCount(): Int = mUsers.size

    override fun onBindViewHolder(holder: TimeWasterViewHolder, position: Int) {
        holder.bind(mContext, mUsers[position], position, averageRoundTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeWasterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_time_waster, parent, false)
        return TimeWasterViewHolder(view)
    }

}

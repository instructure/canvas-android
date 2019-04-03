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
package com.instructure.androidfoosball.holders

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.User
import com.instructure.androidfoosball.utils.setAvatar
import kotlinx.android.synthetic.main.adapter_foos_rank.view.*
import org.jetbrains.anko.sdk21.listeners.onClick


class FoosRankViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(context: Context, user: User, position: Int, foosRankCallback: (User) -> Unit) {
        itemView.foosRank.text = when {
            user.rankedGamesPlayed > 0 -> user.foosRanking.toString()
            else -> context.getString(R.string.no_ranking)
        }
        itemView.position.text = "${position + 1}"
        itemView.name.text = user.name
        itemView.avatar.setAvatar(user, context.resources.getDimension(R.dimen.avatar_size_medium).toInt())
        itemView.onClick { foosRankCallback(user) }
    }
}

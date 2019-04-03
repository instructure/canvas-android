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
import com.instructure.androidfoosball.activities.LeaderboardActivity
import com.instructure.androidfoosball.ktmodels.CustomTeam
import com.instructure.androidfoosball.ktmodels.User
import com.instructure.androidfoosball.utils.getWinRate
import com.instructure.androidfoosball.utils.setAvatar
import com.instructure.androidfoosball.utils.setVisible
import kotlinx.android.synthetic.tablet.adapter_team_leaderboard.view.*


class TeamLeaderboardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(context: Context, team: CustomTeam, users: Map<String, User>, position: Int) {
        val playerOne: User? = users[team.users.getOrNull(0) ?: ""]
        val playerTwo: User? = users[team.users.getOrNull(1) ?: ""]
        if (playerOne == null || playerTwo == null) {
            return
        }
        val winRate = team.getWinRate(LeaderboardActivity.MIN_GAMES_FOR_TEAM_RANKING)
        itemView.team_winRate.text = when {
            winRate >= 0 -> context.getString(R.string.win_rate_format).format(winRate)
            else -> context.getString(R.string.no_ranking)
        }
        itemView.team_position.text = (position + 1).toString()
        itemView.team_name.setVisible(!team.teamName.isBlank())
        itemView.team_name.text = team.teamName
        itemView.wins.text = context.getString(R.string.leaderboard_wins).format(team.teamWins)
        itemView.losses.text = context.getString(R.string.leaderboard_losses).format(team.teamLosses)
        itemView.avatar_player_one.setAvatar(playerOne, context.resources.getDimension(R.dimen.avatar_size_medium).toInt())
        itemView.avatar_player_two.setAvatar(playerTwo, context.resources.getDimension(R.dimen.avatar_size_medium).toInt())

    }
}

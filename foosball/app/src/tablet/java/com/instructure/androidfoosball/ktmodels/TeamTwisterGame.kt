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
package com.instructure.androidfoosball.ktmodels

import com.instructure.androidfoosball.utils.disjunctiveUnion
import com.instructure.androidfoosball.utils.edit
import com.instructure.androidfoosball.utils.swappedIf
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

@Suppress("unused")
open class TeamTwisterGame(
        @PrimaryKey
        var id: String = UUID.randomUUID().toString(),
        var status: String = GameStatus.ONGOING.name,
        var pointsToWin: Int = 0,
        var goalCount: Int = 0,
        var teams: RealmList<TeamWithPoints> = RealmList(),
        var startTime: Long = -1L,
        var endTime: Long = -1L
) : RealmObject() {

    private val winningTeam: TeamWithPoints? get() = teams.firstOrNull { it.points >= pointsToWin }

    private val losingTeams: List<TeamWithPoints?> get() = teams - winningTeam

    private val teamConfigIdx get() = TEAM_CONFIGS[goalCount % TEAM_CONFIGS.size]

    private val nextTeams: Pair<TeamWithPoints, TeamWithPoints>
        get() {
            val nextConfigIdx = TEAM_CONFIGS[(goalCount + 1) % TEAM_CONFIGS.size]
            return teams[nextConfigIdx] to teams[5 - nextConfigIdx]
        }

    val hasWinner: Boolean get() = winningTeam != null

    val currentTeams get() = teams[teamConfigIdx] to teams[5 - teamConfigIdx]

    fun getServingSide() = with(currentTeams) {
        if (first.serveValue > second.serveValue) TableSide.SIDE_1 else TableSide.SIDE_2
    }

    fun recordGoal(side: TableSide) {
        edit {
            val (scoringTeam, otherTeam) = currentTeams.swappedIf(side.isSide2)
            scoringTeam.points++
            scoringTeam.madeLastGoal = true
            otherTeam.madeLastGoal = false
            goalCount++
        }
    }

    fun getNextSwappedPlayers() = currentTeams.first.users.disjunctiveUnion(nextTeams.first.users)

    companion object {
        private val TEAM_CONFIGS = listOf(0, 3, 1, 5, 3, 4, 5, 2, 4, 0, 2, 1)
    }
}

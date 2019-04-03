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
open class TableKingGame(
        @PrimaryKey
        var id: String = UUID.randomUUID().toString(),
        var status: String = GameStatus.ONGOING.name,
        var pointsToWin: Int = 0,
        var completedRounds: Int = 0,
        var teams: RealmList<TeamWithPoints> = RealmList(),
        var startTime: Long = -1L,
        var endTime: Long = -1L
) : RealmObject() {

    private val hasRoundWinner: Boolean get() = with(currentTeams) { first.points == pointsToWin || second.points == pointsToWin }

    private val teamConfigIdx get() = TEAM_CONFIGS[completedRounds % TEAM_CONFIGS.size]

    private val nextTeams: Pair<TeamWithPoints, TeamWithPoints>
        get() {
            val nextConfig = TEAM_CONFIGS[(completedRounds + 1) % TEAM_CONFIGS.size]
            return teams[nextConfig] to teams[5 - nextConfig]
        }

    val currentTeams get() = teams[teamConfigIdx] to teams[5 - teamConfigIdx]

    val isGameComplete: Boolean get() = completedRounds >= 3

    fun getServingSide() = with(currentTeams) {
        if (first.serveValue > second.serveValue) TableSide.SIDE_1 else TableSide.SIDE_2
    }

    fun recordGoal(side: TableSide) = edit {
        val (scoringTeam, otherTeam) = currentTeams.swappedIf(side.isSide2)
        scoringTeam.points++
        scoringTeam.madeLastGoal = true
        otherTeam.madeLastGoal = false
        if (hasRoundWinner) completedRounds++
    }

    fun getGoalOutcome(side: TableSide): Pair<Boolean, Boolean> {
        val scoringTeam = when (side) {
            TableSide.SIDE_1 -> currentTeams.first
            TableSide.SIDE_2 -> currentTeams.second
        }
        val willWinRound = (scoringTeam.points + 1) >= pointsToWin
        val willEndGame = willWinRound && (completedRounds + 1) >= 3
        return willWinRound to willEndGame
    }

    fun getNextSwappedPlayers() = currentTeams.first.users.disjunctiveUnion(nextTeams.first.users)

    companion object {
        private val TEAM_CONFIGS = listOf(0, 3, 1)
    }
}

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

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*


@Suppress("unused")
open class Game(
        @PrimaryKey
        var id: String = UUID.randomUUID().toString(),
        var status: String = GameStatus.ONGOING.name,
        var bestOf: Int = 3,
        var rounds: RealmList<Round> = RealmList(),
        var teamOne: Team? = null,
        var teamTwo: Team? = null,
        var startTime: Long = -1L,
        var endTime: Long = -1L
) : RealmObject() {

    fun currentRound(): Round = rounds.last()

    fun hasWinner(): Boolean = getWinningTeam() != null

    fun getWinningTeam(): Team? {

        val (winCountTeamOne, winCountTeamTwo) = rounds.fold(0 to 0) { (first, second), round ->
            val winner = round.getWinningTeam()
            when (winner) {
                teamOne -> (first + 1) to second
                teamTwo -> first to (second + 1)
                else -> first to second
            }
        }

        val roundsToWin = 1 + (bestOf / 2)
        return when {
            winCountTeamOne >= roundsToWin -> teamOne
            winCountTeamTwo >= roundsToWin -> teamTwo
            else -> null
        }
    }

    fun getLosingTeam(): Team? = when (getWinningTeam()) {
        teamOne -> teamTwo
        teamTwo -> teamOne
        else -> null
    }

    fun getTeamWinCount(team: Team) = rounds.count { it.getWinningTeam() == team }

    /**
     * Returns the [TableSide] that should serve the ball, based on which [Team] scored last.
     */
    fun getServingSide() = if (getLastScoringTeam() == currentRound().sideOneTeam) TableSide.SIDE_2 else TableSide.SIDE_1

    /**
     * Returns the [Team] that last scored during this game. If no team has scored yet, this function
     * returns the team with the higher FoosRank
     */
    private fun getLastScoringTeam() = when {
        // If current round has goal history, return the team of the last goal
        currentRound().goalHistory.isNotEmpty() -> currentRound().goalHistory.last().team

        // If not, and there was a previous round, return the team of that round's last goal
        rounds.size > 1 -> rounds[rounds.size - 2].goalHistory.last().team

        // Otherwise, return the team with the better average win rate
        else -> listOf(teamOne, teamTwo).maxBy { it?.getCompositeFoosRank() ?: 0.0 }!!
    }
}

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

import com.instructure.androidfoosball.utils.edit
import com.instructure.androidfoosball.utils.elseIfBlank
import io.realm.RealmList
import io.realm.RealmObject

@Suppress("unused")
open class Round(
        var pointsToWin: Int = 5,
        var sideOneTeam: Team? = null,
        var sideTwoTeam: Team? = null,
        var goalHistory: RealmList<Goal> = RealmList(),
        var startTime: Long = -1L,
        var endTime: Long = -1L
) : RealmObject() {
    fun getScore(team: Team) = goalHistory.count { it.team == team }

    fun getTeamName(side: TableSide, table: Table) = when (side) {
        TableSide.SIDE_1 -> sideOneTeam?.teamName.elseIfBlank(table.sideOneName)
        TableSide.SIDE_2 -> sideTwoTeam?.teamName.elseIfBlank(table.sideTwoName)
    }
    fun getTeamName(team: Team, table: Table) = when (team) {
        sideOneTeam -> sideOneTeam?.teamName.elseIfBlank(table.sideOneName)
        sideTwoTeam -> sideTwoTeam?.teamName.elseIfBlank(table.sideTwoName)
        else -> ""
    }

    fun recordGoal(side: TableSide) {
        edit {
            val goal = Goal(if (side == TableSide.SIDE_1) sideOneTeam else sideTwoTeam)
            goalHistory.add(goal)
        }
    }

    fun hasWinner() = getWinningTeam() != null

    fun getWinningTeam(): Team? = when {
        getScore(sideOneTeam!!) >= pointsToWin -> sideOneTeam
        getScore(sideTwoTeam!!) >= pointsToWin -> sideTwoTeam
        else -> null
    }

    fun getLosingTeam(): Team? = when {
        getScore(sideOneTeam!!) >= pointsToWin -> sideTwoTeam
        getScore(sideTwoTeam!!) >= pointsToWin -> sideOneTeam
        else -> null
    }
}

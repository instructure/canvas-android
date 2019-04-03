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

import com.instructure.androidfoosball.App
import com.instructure.androidfoosball.utils.getTeamHash
import io.realm.RealmList

interface ITeam {

    var users: RealmList<User>

    fun getTeamHash() = users.map(User::id).getTeamHash()

    val teamName: String? get() = App.realm.where(RealmTeam::class.java).equalTo("id", getTeamHash()).findFirst()?.teamName

    fun getAverageWinRate(): Float {
        val (wins, losses) = users.fold(Pair(0, 0)) { pair, user ->
            Pair(pair.first + user.wins, pair.second + user.losses)
        }
        if (wins == 0) return 0f
        if (losses == 0) return 100f
        return wins * 100f / (wins + losses)
    }

    fun getCompositeFoosRank() = users.map { it.foosRanking }.average()

}

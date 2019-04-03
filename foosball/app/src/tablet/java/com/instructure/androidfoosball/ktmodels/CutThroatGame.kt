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


open class CutThroatGame(
        @PrimaryKey
        var id: String = UUID.randomUUID().toString(),
        var status: String = GameStatus.ONGOING.name,
        var pointsToWin: Int = 0,
        var rotateAfter: Int = 0,
        var singleIdx: Int = 0,
        var pointsSinceRotation: Int = 0,
        var players: RealmList<CutThroatPlayer> = RealmList(),
        var startTime: Long = -1L,
        var endTime: Long = -1L
) : RealmObject() {
    fun getSingle() = players[singleIdx]
    fun hasWinner(): Boolean = getWinner() != null
    fun getWinner(): User? = players.firstOrNull { it.score >= pointsToWin }?.user
}

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

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*


data class CustomTeam(
        var id: String = "",
        var teamName: String = "",
        var teamWins: Long = 0,
        var teamLosses: Long = 0,
        var users: List<String> = ArrayList()
) {
    fun toRealmTeam() = RealmTeam(id, teamName, teamWins, teamLosses).apply { userList = users }
}

open class RealmTeam(
        @PrimaryKey
        var id: String = "",
        var teamName: String = "",
        var teamWins: Long = 0,
        var teamLosses: Long = 0,
        var usersString: String = ""
) : RealmObject() {

    var userList: List<String>
        get() = usersString.split("|")
        set(value) {
            usersString = value.joinToString("|")
        }

    fun toCustomTeam() = CustomTeam(id, teamName, teamWins, teamLosses, userList)
}

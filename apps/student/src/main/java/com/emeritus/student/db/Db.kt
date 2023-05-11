/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.emeritus.student.db

import com.squareup.sqldelight.db.SqlDriver

object Db {
    private var driverRef: SqlDriver? = null
    private var dbRef: StudentDb? = null

    val ready: Boolean
        get() = driverRef != null

    fun dbSetup(driver: SqlDriver) {
        val db = createQueryWrapper(driver)
        driverRef = driver
        dbRef = db
    }

    internal fun dbClear() {
        driverRef!!.close()
        dbRef = null
        driverRef = null
    }

    val instance: StudentDb
        get() = dbRef!!
}
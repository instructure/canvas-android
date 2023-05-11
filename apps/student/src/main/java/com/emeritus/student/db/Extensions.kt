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

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver

const val DB_NAME = "student.db"

fun Db.getInstance(context: Context): StudentDb {
    if (!ready)
    // Note: To use an in-memory database (for testing purposes), pass in 'null' for the name argument or don't pass anything at all (null by default)
        dbSetup(
            AndroidSqliteDriver(
                Schema,
                context,
                DB_NAME,
                callback = object : AndroidSqliteDriver.Callback(
                    Schema
                ) {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        db.execSQL("PRAGMA foreign_keys=ON;")
                    }
                })
        )

    return instance
}

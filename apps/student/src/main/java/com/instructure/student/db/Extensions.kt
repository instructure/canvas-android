package com.instructure.student.db

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver

fun Db.getInstance(context: Context): StudentDb {
    if (!ready)
        dbSetup(AndroidSqliteDriver(Schema, context))

    return instance
}
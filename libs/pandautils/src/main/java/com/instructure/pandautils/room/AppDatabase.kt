package com.instructure.pandautils.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.instructure.pandautils.room.daos.FileUploadInputDao
import com.instructure.pandautils.room.entities.FileUploadInput

@Database(entities = [FileUploadInput::class], version = 1)
abstract class AppDatabase: RoomDatabase() {

    abstract fun fileUploadInputDao(): FileUploadInputDao
}
package com.instructure.pandautils.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.instructure.pandautils.room.daos.FileUploadInputDao
import com.instructure.pandautils.room.entities.FileUploadInput

@Database(entities = [FileUploadInput::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun fileUploadInputDao(): FileUploadInputDao
}
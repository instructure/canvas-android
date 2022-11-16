package com.instructure.pandautils.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.instructure.canvasapi2.db.entities.Attachment
import com.instructure.canvasapi2.db.entities.FileUploadInput
import com.instructure.pandautils.room.daos.AttachmentDao
import com.instructure.pandautils.room.daos.FileUploadInputDao

@Database(entities = [FileUploadInput::class, Attachment::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun fileUploadInputDao(): FileUploadInputDao

    abstract fun attachmentDao(): AttachmentDao
}
package com.instructure.pandautils.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.instructure.canvasapi2.db.entities.*
import com.instructure.pandautils.room.daos.*

@Database(
    entities = [
        Attachment::class,
        Author::class,
        FileUploadInput::class,
        MediaComment::class,
        SubmissionComment::class
    ], version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun attachmentDao(): AttachmentDao

    abstract fun authorDao(): AuthorDao

    abstract fun fileUploadInputDao(): FileUploadInputDao

    abstract fun mediaCommentDao(): MediaCommentDao

    abstract fun submissionCommentDao(): SubmissionCommentDao
}
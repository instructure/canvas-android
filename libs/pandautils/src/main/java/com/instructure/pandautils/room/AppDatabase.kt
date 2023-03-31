package com.instructure.pandautils.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.instructure.pandautils.room.daos.*
import com.instructure.pandautils.room.entities.*

@Database(
    entities = [
        AttachmentEntity::class,
        AuthorEntity::class,
        DashboardFileUploadEntity::class,
        FileUploadInputEntity::class,
        MediaCommentEntity::class,
        PendingSubmissionCommentEntity::class,
        SubmissionCommentEntity::class,
    ], version = 3
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun attachmentDao(): AttachmentDao

    abstract fun authorDao(): AuthorDao

    abstract fun fileUploadInputDao(): FileUploadInputDao

    abstract fun mediaCommentDao(): MediaCommentDao

    abstract fun submissionCommentDao(): SubmissionCommentDao

    abstract fun pendingSubmissionCommentDao(): PendingSubmissionCommentDao

    abstract fun dashboardFileUploadDao(): DashboardFileUploadDao
}
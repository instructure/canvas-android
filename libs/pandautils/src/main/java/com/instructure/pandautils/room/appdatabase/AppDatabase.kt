package com.instructure.pandautils.room.appdatabase

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.instructure.pandautils.room.appdatabase.daos.*
import com.instructure.pandautils.room.appdatabase.entities.*
import com.instructure.pandautils.room.common.Converters
import com.instructure.pandautils.room.common.daos.AttachmentDao
import com.instructure.pandautils.room.common.daos.AuthorDao
import com.instructure.pandautils.room.common.daos.MediaCommentDao
import com.instructure.pandautils.room.common.daos.SubmissionCommentDao
import com.instructure.pandautils.room.common.entities.AttachmentEntity
import com.instructure.pandautils.room.common.entities.AuthorEntity
import com.instructure.pandautils.room.common.entities.MediaCommentEntity
import com.instructure.pandautils.room.common.entities.SubmissionCommentEntity

@Database(
    entities = [
        AttachmentEntity::class,
        AuthorEntity::class,
        FileUploadInputEntity::class,
        MediaCommentEntity::class,
        SubmissionCommentEntity::class,
        PendingSubmissionCommentEntity::class,
        DashboardFileUploadEntity::class
    ], version = 7
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
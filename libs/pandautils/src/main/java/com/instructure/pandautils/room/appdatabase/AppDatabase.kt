package com.instructure.pandautils.room.appdatabase

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.instructure.pandautils.room.appdatabase.daos.AttachmentDao
import com.instructure.pandautils.room.appdatabase.daos.AuthorDao
import com.instructure.pandautils.room.appdatabase.daos.DashboardFileUploadDao
import com.instructure.pandautils.room.appdatabase.daos.EnvironmentFeatureFlagsDao
import com.instructure.pandautils.room.appdatabase.daos.FileUploadInputDao
import com.instructure.pandautils.room.appdatabase.daos.MediaCommentDao
import com.instructure.pandautils.room.appdatabase.daos.ModuleBulkProgressDao
import com.instructure.pandautils.room.appdatabase.daos.PendingSubmissionCommentDao
import com.instructure.pandautils.room.appdatabase.daos.ReminderDao
import com.instructure.pandautils.room.appdatabase.daos.SubmissionCommentDao
import com.instructure.pandautils.room.appdatabase.entities.AttachmentEntity
import com.instructure.pandautils.room.appdatabase.entities.AuthorEntity
import com.instructure.pandautils.room.appdatabase.entities.DashboardFileUploadEntity
import com.instructure.pandautils.room.appdatabase.entities.EnvironmentFeatureFlags
import com.instructure.pandautils.room.appdatabase.entities.FileUploadInputEntity
import com.instructure.pandautils.room.appdatabase.entities.MediaCommentEntity
import com.instructure.pandautils.room.appdatabase.entities.ModuleBulkProgressEntity
import com.instructure.pandautils.room.appdatabase.entities.PendingSubmissionCommentEntity
import com.instructure.pandautils.room.appdatabase.entities.ReminderEntity
import com.instructure.pandautils.room.appdatabase.entities.SubmissionCommentEntity
import com.instructure.pandautils.room.assignment.list.daos.AssignmentListFilterDao
import com.instructure.pandautils.room.assignment.list.entities.AssignmentListFilterEntity
import com.instructure.pandautils.room.common.Converters

@Database(
    entities = [
        AttachmentEntity::class,
        AuthorEntity::class,
        EnvironmentFeatureFlags::class,
        FileUploadInputEntity::class,
        MediaCommentEntity::class,
        SubmissionCommentEntity::class,
        PendingSubmissionCommentEntity::class,
        DashboardFileUploadEntity::class,
        ReminderEntity::class,
        ModuleBulkProgressEntity::class,
        AssignmentListFilterEntity::class
    ], version = 11
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

    abstract fun environmentFeatureFlagsDao(): EnvironmentFeatureFlagsDao

    abstract fun reminderDao(): ReminderDao

    abstract fun moduleBulkProgressDao(): ModuleBulkProgressDao

    abstract fun assignmentListFilterDao(): AssignmentListFilterDao
}
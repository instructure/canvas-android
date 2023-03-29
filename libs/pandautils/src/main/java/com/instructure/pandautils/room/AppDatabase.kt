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
        CourseEntity::class,
        CourseGradingPeriodEntity::class,
        DashboardFileUploadEntity::class,
        EnrollmentEntity::class,
        FileUploadInputEntity::class,
        GradesEntity::class,
        GradingPeriodEntity::class,
        MediaCommentEntity::class,
        PendingSubmissionCommentEntity::class,
        SectionEntity::class,
        SubmissionCommentEntity::class,
        TermEntity::class,
        UserCalendarEntity::class,
        UserEntity::class
    ], version = 4
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

    abstract fun courseDao(): CourseDao

    abstract fun enrollmentDao(): EnrollmentDao

    abstract fun gradesDao(): GradesDao

    abstract fun gradingPeriodDao(): GradingPeriodDao

    abstract fun sectionDao(): SectionDao

    abstract fun termDao(): TermDao

    abstract fun userCalendarDao(): UserCalendarDao

    abstract fun userDao(): UserDao

    abstract fun courseGradingPeriodDao(): CourseGradingPeriodDao
}
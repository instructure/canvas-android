/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.pandautils.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.instructure.pandautils.room.daos.*
import com.instructure.pandautils.room.entities.*

@Database(
    entities = [
        CourseEntity::class,
        CourseGradingPeriodEntity::class,
        EnrollmentEntity::class,
        GradesEntity::class,
        GradingPeriodEntity::class,
        SectionEntity::class,
        TabEntity::class,
        TermEntity::class,
        UserCalendarEntity::class,
        UserEntity::class
    ], version = 1
)
@TypeConverters(Converters::class)
abstract class OfflineDatabase : RoomDatabase() {

    abstract fun courseDao(): CourseDao

    abstract fun enrollmentDao(): EnrollmentDao

    abstract fun gradesDao(): GradesDao

    abstract fun gradingPeriodDao(): GradingPeriodDao

    abstract fun sectionDao(): SectionDao

    abstract fun termDao(): TermDao

    abstract fun userCalendarDao(): UserCalendarDao

    abstract fun userDao(): UserDao

    abstract fun courseGradingPeriodDao(): CourseGradingPeriodDao

    abstract fun tabDao(): TabDao
}
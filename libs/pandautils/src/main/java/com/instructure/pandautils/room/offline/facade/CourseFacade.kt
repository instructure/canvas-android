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

package com.instructure.pandautils.room.offline.facade

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.room.offline.daos.*
import com.instructure.pandautils.room.offline.entities.*

class CourseFacade(
    private val termDao: TermDao,
    private val courseDao: CourseDao,
    private val gradingPeriodDao: GradingPeriodDao,
    private val courseGradingPeriodDao: CourseGradingPeriodDao,
    private val sectionDao: SectionDao,
    private val tabDao: TabDao,
    private val enrollmentFacade: EnrollmentFacade,
    private val courseSettingsDao: CourseSettingsDao,
    private val apiPrefs: ApiPrefs
) {

    suspend fun insertCourse(course: Course) {
        course.term?.let {
            termDao.insertOrUpdate(TermEntity(it))
        }

        courseDao.insertOrUpdate(CourseEntity(course))

        course.settings?.let {
            courseSettingsDao.insert(CourseSettingsEntity(it, course.id))
        }

        course.sections.forEach { section ->
            sectionDao.insertOrUpdate(SectionEntity(section, course.id))
        }

        course.enrollments?.forEach { enrollment ->
            enrollmentFacade.insertEnrollment(enrollment, course.id)
        }

        course.gradingPeriods?.forEach { gradingPeriod ->
            gradingPeriodDao.insert(GradingPeriodEntity(gradingPeriod))
            courseGradingPeriodDao.insert(CourseGradingPeriodEntity(course.id, gradingPeriod.id))
        }

        course.tabs?.forEach { tab ->
            tabDao.insert(TabEntity(tab, course.id))
        }
    }

    suspend fun getCourseById(id: Long): Course? {
        val courseEntity = courseDao.findById(id)
        return if (courseEntity != null) createFullApiModelFromEntity(courseEntity) else null
    }

    suspend fun getAllCourses(): List<Course> {
        return courseDao.findAll().map {
            createFullApiModelFromEntity(it)
        }
    }

    private suspend fun createFullApiModelFromEntity(courseEntity: CourseEntity): Course {
        val termEntity = courseEntity.termId?.let { termDao.findById(it) }

        val enrollments = apiPrefs.user?.id?.let {
            enrollmentFacade.getEnrollmentsForUserByCourseId(courseEntity.id, it)
        } ?: enrollmentFacade.getEnrollmentsByCourseId(courseEntity.id)
        val sectionEntities = sectionDao.findByCourseId(courseEntity.id)
        val courseGradingPeriodEntities = courseGradingPeriodDao.findByCourseId(courseEntity.id)
        val gradingPeriods = courseGradingPeriodEntities.map {
            gradingPeriodDao.findById(it.gradingPeriodId).toApiModel()
        }
        val tabEntities = tabDao.findByCourseId(courseEntity.id)
        val settingsEntity = courseSettingsDao.findByCourseId(courseEntity.id)

        return courseEntity.toApiModel(
            term = termEntity?.toApiModel(),
            enrollments = enrollments.toMutableList(),
            sections = sectionEntities.map { it.toApiModel() },
            gradingPeriods = gradingPeriods,
            tabs = tabEntities.map { it.toApiModel() },
            settings = settingsEntity?.toApiModel()
        )
    }

    suspend fun getGradingPeriodsByCourseId(id: Long): List<GradingPeriod> {
        val gradingPeriodEntities = courseGradingPeriodDao.findByCourseId(id).map {
            gradingPeriodDao.findById(it.gradingPeriodId)
        }

        return gradingPeriodEntities.map { it.toApiModel() }
    }
}
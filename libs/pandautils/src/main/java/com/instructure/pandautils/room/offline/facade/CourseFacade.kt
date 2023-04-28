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
import com.instructure.pandautils.room.offline.daos.*
import com.instructure.pandautils.room.offline.entities.*

class CourseFacade(
    private val termDao: TermDao,
    private val courseDao: CourseDao,
    private val gradingPeriodDao: GradingPeriodDao,
    private val courseGradingPeriodDao: CourseGradingPeriodDao,
    private val sectionDao: SectionDao,
    private val tabDao: TabDao,
    private val enrollmentFacade: EnrollmentFacade
) {

    suspend fun insertCourse(course: Course) {
        course.term?.let {
            termDao.insert(TermEntity(it))
        }

        courseDao.insert(CourseEntity(course))

        course.enrollments?.forEach { enrollment ->
            enrollmentFacade.insertEnrollment(enrollment, course.id)
        }

        course.gradingPeriods?.forEach { gradingPeriod ->
            gradingPeriodDao.insert(GradingPeriodEntity(gradingPeriod))
            courseGradingPeriodDao.insert(CourseGradingPeriodEntity(course.id, gradingPeriod.id))
        }

        course.sections.forEach { section ->
            sectionDao.insert(SectionEntity(section))
        }

        course.tabs?.forEach { tab ->
            tabDao.insert(TabEntity(tab, course.id))
        }
    }
}
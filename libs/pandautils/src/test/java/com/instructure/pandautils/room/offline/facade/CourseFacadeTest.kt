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

import com.instructure.canvasapi2.models.*
import com.instructure.pandautils.room.offline.daos.*
import com.instructure.pandautils.room.offline.entities.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

@ExperimentalCoroutinesApi
class CourseFacadeTest {

    private val termDao: TermDao = mockk(relaxed = true)
    private val courseDao: CourseDao = mockk(relaxed = true)
    private val gradingPeriodDao: GradingPeriodDao = mockk(relaxed = true)
    private val courseGradingPeriodDao: CourseGradingPeriodDao = mockk(relaxed = true)
    private val sectionDao: SectionDao = mockk(relaxed = true)
    private val tabDao: TabDao = mockk(relaxed = true)
    private val enrollmentFacade: EnrollmentFacade = mockk(relaxed = true)
    private val courseSettingsDao: CourseSettingsDao = mockk(relaxed = true)

    private val facade = CourseFacade(
        termDao,
        courseDao,
        gradingPeriodDao,
        courseGradingPeriodDao,
        sectionDao,
        tabDao,
        enrollmentFacade,
        courseSettingsDao
    )

    @Test
    fun `Calling insertCourse should insert course and related entities`() = runTest {
        val term = Term()
        val enrollments = mutableListOf(Enrollment())
        val gradingPeriods = listOf(GradingPeriod())
        val sections = listOf(Section())
        val tabs = listOf(Tab())
        val settings = CourseSettings(restrictQuantitativeData = true)
        val course = Course(
            term = term, enrollments = enrollments, gradingPeriods = gradingPeriods,
            sections = sections, tabs = tabs, settings = settings
        )

        facade.insertCourse(course)

        coVerify { termDao.insertOrUpdate(TermEntity(term)) }
        coVerify { courseDao.insertOrUpdate(CourseEntity(course)) }
        enrollments.forEach { enrollment ->
            coVerify { enrollmentFacade.insertEnrollment(enrollment, course.id) }
        }
        gradingPeriods.forEach { gradingPeriod ->
            coVerify { gradingPeriodDao.insert(GradingPeriodEntity(gradingPeriod)) }
            coVerify { courseGradingPeriodDao.insert(CourseGradingPeriodEntity(course.id, gradingPeriod.id)) }
        }
        sections.forEach { section ->
            coVerify { sectionDao.insertOrUpdate(SectionEntity(section, course.id)) }
        }
        tabs.forEach { tab ->
            coVerify { tabDao.insert(TabEntity(tab, course.id)) }
        }
        coVerify { courseSettingsDao.insert(CourseSettingsEntity(settings, course.id)) }
    }

    @Test
    fun `Calling getCourseById should return the course with the specified ID`() = runTest {
        val courseId = 1L
        val term = Term(id = 1L, name = "Term")
        val course = Course(id = courseId, term = term)
        val courseEntity = CourseEntity(course)
        val termEntity = TermEntity(term)
        val enrollments = listOf(Enrollment(id = 1L, role = Enrollment.EnrollmentType.Student))
        val section = Section(courseId = courseId, students = null)
        val sectionEntities = listOf(SectionEntity(section, courseId))
        val gradingPeriod = GradingPeriod(id = 1L, title = "Grading period")
        val gradingPeriodEntity = GradingPeriodEntity(gradingPeriod)
        val courseGradingPeriodEntities = listOf(CourseGradingPeriodEntity(courseId, gradingPeriod.id))
        val tab = Tab(tabId = "tabId", label = "Label")
        val tabEntities = listOf(TabEntity(tab, courseId))
        val courseSettings = CourseSettings(restrictQuantitativeData = true)

        coEvery { courseDao.findById(courseId) } returns courseEntity
        coEvery { termDao.findById(any()) } returns termEntity
        coEvery { enrollmentFacade.getEnrollmentsByCourseId(courseId) } returns enrollments
        coEvery { sectionDao.findByCourseId(courseId) } returns sectionEntities
        coEvery { courseGradingPeriodDao.findByCourseId(courseId) } returns courseGradingPeriodEntities
        coEvery { gradingPeriodDao.findById(any()) } returns gradingPeriodEntity
        coEvery { tabDao.findByCourseId(courseId) } returns tabEntities
        coEvery { courseSettingsDao.findByCourseId(courseId) } returns CourseSettingsEntity(courseSettings, courseId)

        val result = facade.getCourseById(courseId)!!

        Assert.assertEquals(courseId, result.id)
        Assert.assertEquals(term, result.term)
        Assert.assertEquals(enrollments, result.enrollments)
        Assert.assertEquals(section, result.sections.first())
        Assert.assertEquals(gradingPeriod, result.gradingPeriods?.first())
        Assert.assertEquals(tab, result.tabs?.first())
        Assert.assertEquals(courseSettings, result.settings)
    }

    @Test
    fun `getAllCourses should return the list of courses mapped correctly to api model`() = runTest {
        val courseId = 1L
        val term = Term(id = 1L, name = "Term")
        val course = Course(id = courseId, term = term)
        val courseEntity = CourseEntity(course)
        val termEntity = TermEntity(term)
        val enrollments = listOf(Enrollment(id = 1L, role = Enrollment.EnrollmentType.Student))
        val section = Section(courseId = courseId, students = null)
        val sectionEntities = listOf(SectionEntity(section, courseId))
        val gradingPeriod = GradingPeriod(id = 1L, title = "Grading period")
        val gradingPeriodEntity = GradingPeriodEntity(gradingPeriod)
        val courseGradingPeriodEntities = listOf(CourseGradingPeriodEntity(courseId, gradingPeriod.id))
        val tab = Tab(tabId = "tabId", label = "Label")
        val tabEntities = listOf(TabEntity(tab, courseId))
        val courseSettings = CourseSettings(restrictQuantitativeData = true)

        coEvery { courseDao.findAll() } returns listOf(courseEntity)
        coEvery { termDao.findById(any()) } returns termEntity
        coEvery { enrollmentFacade.getEnrollmentsByCourseId(courseId) } returns enrollments
        coEvery { sectionDao.findByCourseId(courseId) } returns sectionEntities
        coEvery { courseGradingPeriodDao.findByCourseId(courseId) } returns courseGradingPeriodEntities
        coEvery { gradingPeriodDao.findById(any()) } returns gradingPeriodEntity
        coEvery { tabDao.findByCourseId(courseId) } returns tabEntities
        coEvery { courseSettingsDao.findByCourseId(courseId) } returns CourseSettingsEntity(courseSettings, courseId)

        val result = facade.getAllCourses()

        Assert.assertEquals(1, result.size)
        Assert.assertEquals(courseId, result.first().id)
        Assert.assertEquals(term, result.first().term)
        Assert.assertEquals(enrollments, result.first().enrollments)
        Assert.assertEquals(section, result.first().sections.first())
        Assert.assertEquals(gradingPeriod, result.first().gradingPeriods?.first())
        Assert.assertEquals(tab, result.first().tabs?.first())
        Assert.assertEquals(courseSettings, result.first().settings)
    }

    @Test
    fun `Calling getGradingPeriodsByCourseId should return the grading periods by specified CourseID`() = runTest {
        val gradingPeriods = listOf(GradingPeriod(id = 1L, title = "Grading period 1"), GradingPeriod(id = 2L, title = "Grading period 2"))
        val gradingPeriodEntities = gradingPeriods.map { GradingPeriodEntity(it) }
        val courseGradingPeriodEntities = gradingPeriods.map { CourseGradingPeriodEntity(1L, it.id) }

        coEvery { courseGradingPeriodDao.findByCourseId(1L) } returns courseGradingPeriodEntities
        gradingPeriodEntities.forEach {
            coEvery { gradingPeriodDao.findById(it.id) } returns it
        }

        val result = facade.getGradingPeriodsByCourseId(1L)

        Assert.assertEquals(gradingPeriods.size, result.size)
        Assert.assertEquals(gradingPeriods, result)
    }
}
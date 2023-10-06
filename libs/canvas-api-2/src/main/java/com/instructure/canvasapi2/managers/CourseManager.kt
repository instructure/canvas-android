/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.models.postmodels.UpdateCourseBody
import com.instructure.canvasapi2.models.postmodels.UpdateCourseWrapper
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.ExhaustiveListCallback
import com.instructure.canvasapi2.utils.weave.apiAsync
import kotlinx.coroutines.Deferred
import java.io.IOException

object CourseManager {

    fun getAllFavoriteCoursesAsync(forceNetwork: Boolean) = apiAsync<List<Course>> { getAllFavoriteCourses(forceNetwork, it) }

    fun getAllFavoriteCourses(forceNetwork: Boolean, callback: StatusCallback<List<Course>>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        val depaginatedCallback = object : ExhaustiveListCallback<Course>(callback) {
            override fun getNextPage(callback: StatusCallback<List<Course>>, nextUrl: String, isCached: Boolean) {
                CourseAPI.getNextPageFavoriteCourses(forceNetwork, nextUrl, adapter, callback)
            }
        }

        adapter.statusCallback = depaginatedCallback
        CourseAPI.getFirstPageFavoriteCourses(adapter, depaginatedCallback, params)
    }

    fun getCoursesAsync(forceNetwork: Boolean) = apiAsync<List<Course>> { getCourses(forceNetwork, it) }

    fun getCourses(forceNetwork: Boolean, callback: StatusCallback<List<Course>>) {
        if (ApiPrefs.isStudentView) {
            getCoursesTeacher(forceNetwork, callback)
            return
        }

        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        val depaginatedCallback = object : ExhaustiveListCallback<Course>(callback) {
            override fun getNextPage(callback: StatusCallback<List<Course>>, nextUrl: String, isCached: Boolean) {
                CourseAPI.getNextPageCourses(forceNetwork, nextUrl, adapter, callback)
            }
        }

        adapter.statusCallback = depaginatedCallback
        CourseAPI.getFirstPageCourses(adapter, depaginatedCallback, params)
    }

    fun getCoursesWithGradingScheme(forceNetwork: Boolean, callback: StatusCallback<List<Course>>) {
        if (ApiPrefs.isStudentView) {
            getCoursesTeacher(forceNetwork, callback)
            return
        }

        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        val depaginatedCallback = object : ExhaustiveListCallback<Course>(callback) {
            override fun getNextPage(callback: StatusCallback<List<Course>>, nextUrl: String, isCached: Boolean) {
                CourseAPI.getNextPageCourses(forceNetwork, nextUrl, adapter, callback)
            }
        }

        adapter.statusCallback = depaginatedCallback
        CourseAPI.getFirstPageCoursesWithGradingScheme(adapter, depaginatedCallback, params)
    }

    fun getCoursesWithConcluded(forceNetwork: Boolean, callback: StatusCallback<List<Course>>) {
        if (ApiPrefs.isStudentView) {
            getCoursesTeacher(forceNetwork, callback)
            return
        }

        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        val depaginatedCallback = object : ExhaustiveListCallback<Course>(callback) {
            override fun getNextPage(callback: StatusCallback<List<Course>>, nextUrl: String, isCached: Boolean) {
                CourseAPI.getNextPageCourses(forceNetwork, nextUrl, adapter, callback)
            }
        }

        adapter.statusCallback = depaginatedCallback
        CourseAPI.getFirstPageCoursesWithConcluded(adapter, depaginatedCallback, params)
    }

    fun getCoursesWithConcludedAsync(forceNetwork: Boolean) = apiAsync<List<Course>> { getCoursesWithConcluded(forceNetwork, it) }

    fun getDashboardCoursesAsync(forceNetwork: Boolean) = apiAsync<List<DashboardCard>> { getDashboardCourses(forceNetwork, it) }

    fun getDashboardCourses(forceNetwork: Boolean, callback: StatusCallback<List<DashboardCard>>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        CourseAPI.getDashboardCourses(adapter, callback, params)
    }

    fun getCoursesWithSyllabus(forceNetwork: Boolean, callback: StatusCallback<List<Course>>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        val depaginatedCallback = object : ExhaustiveListCallback<Course>(callback) {
            override fun getNextPage(callback: StatusCallback<List<Course>>, nextUrl: String, isCached: Boolean) {
                CourseAPI.getNextPageCourses(forceNetwork, nextUrl, adapter, callback)
            }
        }

        adapter.statusCallback = depaginatedCallback
        CourseAPI.getFirstPageCoursesWithSyllabus(adapter, depaginatedCallback, params)
    }

    fun getCoursesWithSyllabusAsyncWithActiveEnrollmentAsync(forceNetwork: Boolean) = apiAsync<List<Course>> { getCoursesWithSyllabusWithActiveEnrollment(forceNetwork, it) }

    private fun getCoursesWithSyllabusWithActiveEnrollment(forceNetwork: Boolean, callback: StatusCallback<List<Course>>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        val depaginatedCallback = object : ExhaustiveListCallback<Course>(callback) {
            override fun getNextPage(callback: StatusCallback<List<Course>>, nextUrl: String, isCached: Boolean) {
                CourseAPI.getNextPageCourses(forceNetwork, nextUrl, adapter, callback)
            }
        }

        adapter.statusCallback = depaginatedCallback
        CourseAPI.getFirstPageCoursesWithSyllabusWithActiveEnrollment(adapter, depaginatedCallback, params)
    }

    fun getCoursesTeacher(forceNetwork: Boolean, callback: StatusCallback<List<Course>>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        val depaginatedCallback = object : ExhaustiveListCallback<Course>(callback) {
            override fun getNextPage(callback: StatusCallback<List<Course>>, nextUrl: String, isCached: Boolean) {
                CourseAPI.getNextPageCourses(forceNetwork, nextUrl, adapter, callback)
            }
        }

        adapter.statusCallback = depaginatedCallback

        CourseAPI.getFirstPageCoursesTeacher(adapter, depaginatedCallback, params)
    }

    fun getCoursesTeacherAsync(forceNetwork: Boolean) = apiAsync<List<Course>> { getCoursesTeacher(forceNetwork, it) }

    fun getGradingPeriodsForCourse(callback: StatusCallback<GradingPeriodResponse>, courseId: Long, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        CourseAPI.getGradingPeriodsForCourse(adapter, callback, params, courseId)
    }

    fun getCourseAsync(courseId: Long, forceNetwork: Boolean) = apiAsync { getCourse(courseId, it, forceNetwork) }

    fun getCourse(courseId: Long, callback: StatusCallback<Course>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        CourseAPI.getCourse(courseId, adapter, callback, params)
    }

    fun getCourseWithSyllabus(courseId: Long, callback: StatusCallback<Course>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        CourseAPI.getCourseWithSyllabus(courseId, adapter, callback, params)
    }

    fun getCourseWithSyllabusAsync(courseId: Long, forceNetwork: Boolean) = apiAsync<Course> {
        getCourseWithSyllabus(courseId, it, forceNetwork)
    }

    fun getCourseSettingsAsync(courseId: Long, forceNetwork: Boolean) = apiAsync<CourseSettings> {
        val adapter = RestBuilder(it)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        CourseAPI.getCourseSettings(courseId, adapter, it, params)
    }

    fun editCourseSettingsAsync(courseId: Long, summaryAllowed: Boolean): Deferred<DataResult<CourseSettings>> {
        return apiAsync { editCourseSettings(courseId, summaryAllowed, it) }
    }

    private fun editCourseSettings(courseId: Long, summaryAllowed: Boolean, callback: StatusCallback<CourseSettings>) {
        val queryParams = HashMap<String, Boolean>()
        queryParams["syllabus_course_summary"] = summaryAllowed

        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = true)

        CourseAPI.updateCourseSettings(courseId, queryParams, adapter, callback, params)
    }

    fun getCourseWithGrade(courseId: Long, callback: StatusCallback<Course>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        CourseAPI.getCourseWithGrade(courseId, adapter, callback, params)
    }

    fun getCourseWithGradeAsync(courseId: Long, forceNetwork: Boolean) = apiAsync<Course> {
        getCourseWithGrade(courseId, it, forceNetwork)
    }

    fun getCourseStudent(courseId: Long, studentId: Long, callback: StatusCallback<User>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        CourseAPI.getCourseStudent(courseId, studentId, adapter, callback, params)
    }

    fun addCourseToFavorites(courseId: Long, callback: StatusCallback<Favorite>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        CourseAPI.addCourseToFavorites(courseId, adapter, callback, params)
    }

    fun addCourseToFavoritesAsync(courseId: Long) = apiAsync<Favorite> { addCourseToFavorites(courseId, it, true) }

    fun removeCourseFromFavorites(courseId: Long, callback: StatusCallback<Favorite>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        CourseAPI.removeCourseFromFavorites(courseId, adapter, callback, params)
    }

    fun removeCourseFromFavoritesAsync(courseId: Long) = apiAsync<Favorite> { removeCourseFromFavorites(courseId, it, true) }

    fun editCourseName(courseId: Long, newCourseName: String, callback: StatusCallback<Course>, forceNetwork: Boolean) {
        val queryParams = HashMap<String, String>()
        queryParams["course[name]"] = newCourseName

        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        CourseAPI.updateCourse(courseId, queryParams, adapter, callback, params)
    }

    fun editCourseHomePage(
        courseId: Long,
        newHomePage: String,
        forceNetwork: Boolean,
        callback: StatusCallback<Course>
    ) {
        val queryParams = HashMap<String, String>()
        queryParams["course[default_view]"] = newHomePage

        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        CourseAPI.updateCourse(courseId, queryParams, adapter, callback, params)
    }

    fun editCourseSyllabusAsync(courseId: Long, syllabusBody: String): Deferred<DataResult<Course>> {
        return apiAsync { editCourseSyllabus(courseId, syllabusBody, it) }
    }

    private fun editCourseSyllabus(courseId: Long, syllabusBody: String, callback: StatusCallback<Course>) {
        val updateCourseBody = UpdateCourseBody(syllabusBody)
        val updateCourseWrapper = UpdateCourseWrapper(updateCourseBody)

        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = true)

        CourseAPI.updateCourse(courseId, updateCourseWrapper, adapter, callback, params)
    }

    private fun getCoursesWithEnrollmentType(forceNetwork: Boolean, callback: StatusCallback<List<Course>>, type: String) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        CourseAPI.getCoursesByEnrollmentType(adapter, callback, params, type)
    }

    fun getCoursesWithEnrollmentType(
        forceNetwork: Boolean,
        type: String
    ) = apiAsync<List<Course>> {
        getCoursesWithEnrollmentType(forceNetwork, it, type)
    }

    fun getGroupsForCourse(courseId: Long, callback: StatusCallback<List<Group>>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        val depaginatedCallback = object : ExhaustiveListCallback<Group>(callback) {
            override fun getNextPage(callback: StatusCallback<List<Group>>, nextUrl: String, isCached: Boolean) {
                CourseAPI.getNextPageGroups(nextUrl, adapter, callback, params)
            }
        }
        adapter.statusCallback = depaginatedCallback
        CourseAPI.getFirstPageGroups(courseId, adapter, depaginatedCallback, params)
    }

    fun getCoursePermissions(
        courseId: Long,
        requestedPermissions: List<String>,
        callback: StatusCallback<CanvasContextPermission>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        CourseAPI.getCoursePermissions(courseId, requestedPermissions, adapter, callback, params)
    }

    fun getPermissionsAsync(
        courseId: Long,
        requestedPermissions: List<String> = emptyList(),
        forceNetwork: Boolean = false
    ) = apiAsync<CanvasContextPermission> { getCoursePermissions(courseId, requestedPermissions, it, forceNetwork) }

    fun getUserEnrollmentsForGradingPeriod(
        courseId: Long,
        userId: Long,
        gradingPeriodId: Long,
        callback: StatusCallback<List<Enrollment>>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        CourseAPI.getUserEnrollmentsForGradingPeriod(courseId, userId, gradingPeriodId, adapter, params, callback)
    }

    @Throws(IOException::class)
    fun getCoursesSynchronousWithGradingScheme(forceNetwork: Boolean): List<Course> {
        val adapter = RestBuilder()
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        val data = CourseAPI.getCoursesSynchronouslyWithGradingScheme(adapter, params)
        return data ?: ArrayList()
    }

    fun createCourseMap(courses: List<Course>?): Map<Long, Course> = courses?.associateBy { it.id } ?: emptyMap()

    fun getRubricSettings(courseId: Long, rubricId: Long, forceNetwork: Boolean, callback: StatusCallback<RubricSettings>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        CourseAPI.getRubricSettings(courseId, rubricId, adapter, callback, params)
    }

    fun getCoursesWithGradesAsync(forceNetwork: Boolean) = apiAsync<List<Course>> { getCoursesWithGrades(forceNetwork, it) }

    private fun getCoursesWithGrades(forceNetwork: Boolean, callback: StatusCallback<List<Course>>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        val depaginatedCallback = object : ExhaustiveListCallback<Course>(callback) {
            override fun getNextPage(callback: StatusCallback<List<Course>>, nextUrl: String, isCached: Boolean) {
                CourseAPI.getNextPageCourses(forceNetwork, nextUrl, adapter, callback)
            }
        }

        adapter.statusCallback = depaginatedCallback
        CourseAPI.getFirstPageCoursesWithGrades(adapter, callback, params)
    }

}

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
import com.instructure.canvasapi2.utils.ExhaustiveListCallback
import com.instructure.canvasapi2.utils.weave.apiAsync
import java.io.IOException
import java.util.ArrayList
import java.util.HashMap

object CourseManager {

    @JvmStatic
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

    @JvmStatic
    fun getCourses(forceNetwork: Boolean, callback: StatusCallback<List<Course>>) {
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

    @JvmStatic
    fun getDashboardCourses(forceNetwork: Boolean, callback: StatusCallback<List<DashboardCard>>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        CourseAPI.getDashboardCourses(adapter, callback, params)
    }

    @JvmStatic
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

    @JvmStatic
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

    @JvmStatic
    fun getGradingPeriodsForCourse(callback: StatusCallback<GradingPeriodResponse>, courseId: Long, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        CourseAPI.getGradingPeriodsForCourse(adapter, callback, params, courseId)
    }

    @JvmStatic
    fun getCourse(courseId: Long, callback: StatusCallback<Course>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        CourseAPI.getCourse(courseId, adapter, callback, params)
    }

    @JvmStatic
    fun getCourseWithSyllabus(courseId: Long, callback: StatusCallback<Course>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        CourseAPI.getCourseWithSyllabus(courseId, adapter, callback, params)
    }

    fun getCourseWithSyllabusAsync(courseId: Long, forceNetwork: Boolean) = apiAsync<Course> {
        getCourseWithSyllabus(courseId, it, forceNetwork)
    }

    @JvmStatic
    fun getCourseWithGrade(courseId: Long, callback: StatusCallback<Course>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        CourseAPI.getCourseWithGrade(courseId, adapter, callback, params)
    }

    fun getCourseWithGradeAsync(courseId: Long, forceNetwork: Boolean) = apiAsync<Course> {
        getCourseWithGrade(courseId, it, forceNetwork)
    }

    @JvmStatic
    fun getCourseStudent(courseId: Long, studentId: Long, callback: StatusCallback<User>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        CourseAPI.getCourseStudent(courseId, studentId, adapter, callback, params)
    }

    @JvmStatic
    fun addCourseToFavorites(courseId: Long, callback: StatusCallback<Favorite>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        CourseAPI.addCourseToFavorites(courseId, adapter, callback, params)
    }

    @JvmStatic
    fun removeCourseFromFavorites(courseId: Long, callback: StatusCallback<Favorite>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        CourseAPI.removeCourseFromFavorites(courseId, adapter, callback, params)
    }

    @JvmStatic
    fun editCourseName(courseId: Long, newCourseName: String, callback: StatusCallback<Course>, forceNetwork: Boolean) {
        val queryParams = HashMap<String, String>()
        queryParams["course[name]"] = newCourseName

        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        CourseAPI.updateCourse(courseId, queryParams, adapter, callback, params)
    }

    @JvmStatic
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

    @JvmStatic
    fun getCoursesWithEnrollmentType(forceNetwork: Boolean, callback: StatusCallback<List<Course>>, type: String) {
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

    @JvmStatic
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

    @JvmStatic
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

    @JvmStatic
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
    @JvmStatic
    fun getCoursesSynchronous(forceNetwork: Boolean): List<Course> {
        val adapter = RestBuilder()
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        val data = CourseAPI.getCoursesSynchronously(adapter, params)
        return data ?: ArrayList()
    }

    @JvmStatic
    fun createCourseMap(courses: List<Course>?): Map<Long, Course> = courses?.associateBy { it.id } ?: emptyMap()

    @JvmStatic
    fun getRubricSettings(courseId: Long, rubricId: Long, forceNetwork: Boolean, callback: StatusCallback<RubricSettings>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        CourseAPI.getRubricSettings(courseId, rubricId, adapter, callback, params)
    }

}

package com.instructure.pandautils.data.repository.course

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DashboardCard
import com.instructure.canvasapi2.utils.DataResult

class CourseRepositoryImpl(
    private val courseApi: CourseAPI.CoursesInterface
) : CourseRepository {

    override suspend fun getCourse(courseId: Long, forceRefresh: Boolean): DataResult<Course> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return courseApi.getCourse(courseId, params)
    }

    override suspend fun getFavoriteCourses(forceRefresh: Boolean): DataResult<List<Course>> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return courseApi.getFavoriteCourses(params)
    }

    override suspend fun getDashboardCards(forceRefresh: Boolean): DataResult<List<DashboardCard>> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return courseApi.getDashboardCourses(params)
    }
}
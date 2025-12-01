package com.instructure.pandautils.data.repository.course

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.DataResult

class CourseRepositoryImpl(
    private val courseApi: CourseAPI.CoursesInterface
) : CourseRepository {

    override suspend fun getCourse(courseId: Long, forceRefresh: Boolean): DataResult<Course> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return courseApi.getCourse(courseId, params)
    }
}
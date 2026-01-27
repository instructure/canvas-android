package com.instructure.pandautils.data.repository.course

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DashboardCard
import com.instructure.canvasapi2.utils.DataResult

interface CourseRepository {
    suspend fun getCourse(courseId: Long, forceRefresh: Boolean): DataResult<Course>
    suspend fun getCourses(forceRefresh: Boolean): DataResult<List<Course>>
    suspend fun getFavoriteCourses(forceRefresh: Boolean): DataResult<List<Course>>
    suspend fun getDashboardCards(forceRefresh: Boolean): DataResult<List<DashboardCard>>
}
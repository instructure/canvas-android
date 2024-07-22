package com.instructure.pandautils.features.inbox.coursepicker

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.depaginate
import javax.inject.Inject

class CoursePickerRepositoryImpl @Inject constructor(
    private val courseAPI: CourseAPI.CoursesInterface,
    private val groupAPI: GroupAPI.GroupInterface
): CoursePickerRepository {
    override suspend fun getCourses(): List<Course> {
        val params = RestParams(usePerPageQueryParam = true)

        return courseAPI.getFirstPageCourses(params).depaginate {
            courseAPI.next(it, params)
        }.dataOrNull ?: emptyList()
    }

    override suspend fun getGroups(): List<Group> {
        val params = RestParams(usePerPageQueryParam = true)

        return groupAPI.getFirstPageGroups(params).depaginate {
            groupAPI.getNextPageGroups(it, params)
        }.dataOrNull ?: emptyList()
    }
}
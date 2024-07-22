package com.instructure.pandautils.features.inbox.coursepicker

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group

interface CoursePickerRepository {
    suspend fun getCourses(): List<Course>

    suspend fun getGroups(): List<Group>
}
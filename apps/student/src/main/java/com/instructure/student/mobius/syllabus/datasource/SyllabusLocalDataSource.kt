package com.instructure.student.mobius.syllabus.datasource

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult

class SyllabusLocalDataSource : SyllabusDataSource {

    override suspend fun getCourseSettings(courseId: Long, forceNetwork: Boolean): CourseSettings {
        TODO("Not yet implemented")
    }

    override suspend fun getCourseWithSyllabus(courseId: Long, forceNetwork: Boolean): DataResult<Course> {
        TODO("Not yet implemented")
    }

    override suspend fun getCalendarEvents(
        allEvents: Boolean,
        type: CalendarEventAPI.CalendarEventType,
        startDate: String?,
        endDate: String?,
        canvasContexts: List<String>,
        forceNetwork: Boolean
    ): DataResult<List<ScheduleItem>> {
        TODO("Not yet implemented")
    }
}
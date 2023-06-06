package com.instructure.student.mobius.syllabus.datasource

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate

class SyllabusNetworkDataSource(
    private val courseApi: CourseAPI.CoursesInterface,
    private val calendarEventApi: CalendarEventAPI.CalendarEventInterface
) : SyllabusDataSource {

    override suspend fun getCourseSettings(courseId: Long, forceNetwork: Boolean): CourseSettings? {
        val restParams = RestParams(isForceReadFromNetwork = forceNetwork)
        return courseApi.getCourseSettings(courseId, restParams).dataOrNull
    }

    override suspend fun getCourseWithSyllabus(courseId: Long, forceNetwork: Boolean): DataResult<Course> {
        val restParams = RestParams(isForceReadFromNetwork = forceNetwork)
        return courseApi.getCourseWithSyllabus(courseId, restParams)
    }

    override suspend fun getCalendarEvents(
        allEvents: Boolean,
        type: CalendarEventAPI.CalendarEventType,
        startDate: String?,
        endDate: String?,
        canvasContexts: List<String>,
        forceNetwork: Boolean
    ): DataResult<List<ScheduleItem>> {
        val restParams = RestParams(isForceReadFromNetwork = forceNetwork)
        return calendarEventApi.getCalendarEvents(
            allEvents,
            type.apiName,
            startDate,
            endDate,
            canvasContexts,
            restParams
        )
            .depaginate { calendarEventApi.next(it, restParams) }
    }
}
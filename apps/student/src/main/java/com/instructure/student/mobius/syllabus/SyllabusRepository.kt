package com.instructure.student.mobius.syllabus

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.mobius.syllabus.datasource.SyllabusDataSource
import com.instructure.student.mobius.syllabus.datasource.SyllabusLocalDataSource
import com.instructure.student.mobius.syllabus.datasource.SyllabusNetworkDataSource

class SyllabusRepository(
    syllabusLocalDataSource: SyllabusLocalDataSource,
    syllabusNetworkDataSource: SyllabusNetworkDataSource,
    networkStateProvider: NetworkStateProvider
) : Repository<SyllabusDataSource>(syllabusLocalDataSource, syllabusNetworkDataSource, networkStateProvider) {

    suspend fun getCourseSettings(courseId: Long, forceNetwork: Boolean): CourseSettings? {
        return dataSource.getCourseSettings(courseId, forceNetwork)
    }

    suspend fun getCourseWithSyllabus(courseId: Long, forceNetwork: Boolean): DataResult<Course> {
        return dataSource.getCourseWithSyllabus(courseId, forceNetwork)
    }

    suspend fun getCalendarEvents(
        allEvents: Boolean,
        type: CalendarEventAPI.CalendarEventType,
        startDate: String?,
        endDate: String?,
        canvasContexts: List<String>,
        forceNetwork: Boolean
    ): DataResult<List<ScheduleItem>> {
        return dataSource.getCalendarEvents(allEvents, type, startDate, endDate, canvasContexts, forceNetwork)
    }
}
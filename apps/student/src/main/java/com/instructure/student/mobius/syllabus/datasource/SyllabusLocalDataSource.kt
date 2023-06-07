package com.instructure.student.mobius.syllabus.datasource

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.daos.CourseSettingsDao
import com.instructure.pandautils.room.offline.facade.CourseFacade
import com.instructure.pandautils.room.offline.facade.ScheduleItemFacade

class SyllabusLocalDataSource(
    private val courseSettingsDao: CourseSettingsDao,
    private val courseFacade: CourseFacade,
    private val scheduleItemFacade: ScheduleItemFacade
) : SyllabusDataSource {

    override suspend fun getCourseSettings(courseId: Long, forceNetwork: Boolean): CourseSettings? {
        return courseSettingsDao.findByCourseId(courseId)?.toApiModel()
    }

    override suspend fun getCourseWithSyllabus(courseId: Long, forceNetwork: Boolean): DataResult<Course> {
        return courseFacade.getCourseById(courseId)?.let {
            DataResult.Success(it)
        } ?: DataResult.Fail()
    }

    override suspend fun getCalendarEvents(
        allEvents: Boolean,
        type: CalendarEventAPI.CalendarEventType,
        startDate: String?,
        endDate: String?,
        canvasContexts: List<String>,
        forceNetwork: Boolean
    ): DataResult<List<ScheduleItem>> {
        return try {
            DataResult.Success(scheduleItemFacade.findByItemType(canvasContexts, type.apiName))
        } catch (e: Exception) {
            e.printStackTrace()
            DataResult.Fail()
        }

    }
}
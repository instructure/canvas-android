package com.instructure.pandautils.data.repository.course

import com.instructure.canvasapi2.apis.AnnouncementAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DashboardCard
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate

class CourseRepositoryImpl(
    private val courseApi: CourseAPI.CoursesInterface,
    private val announcementApi: AnnouncementAPI.AnnouncementInterface
) : CourseRepository {

    override suspend fun getCourse(courseId: Long, forceRefresh: Boolean): DataResult<Course> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return courseApi.getCourse(courseId, params)
    }

    override suspend fun getCourses(forceRefresh: Boolean): DataResult<List<Course>> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceRefresh)
        return courseApi.getFirstPageCourses(params).depaginate { nextUrl ->
            courseApi.next(nextUrl, params)
        }
    }

    override suspend fun getFavoriteCourses(forceRefresh: Boolean): DataResult<List<Course>> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return courseApi.getFavoriteCourses(params).depaginate { nextUrl ->
            courseApi.next(nextUrl, params)
        }
    }

    override suspend fun getDashboardCards(forceRefresh: Boolean): DataResult<List<DashboardCard>> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return courseApi.getDashboardCourses(params)
    }

    override suspend fun getCourseAnnouncement(courseId: Long, announcementId: Long, forceRefresh: Boolean): DataResult<DiscussionTopicHeader> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return announcementApi.getCourseAnnouncement(courseId, announcementId, params)
    }

    override suspend fun getCourseAnnouncements(courseId: Long, forceRefresh: Boolean): DataResult<List<DiscussionTopicHeader>> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh, usePerPageQueryParam = true)
        return announcementApi.getFirstPageAnnouncementsList(
            CanvasContext.Type.COURSE.apiString,
            courseId,
            params
        ).depaginate { nextUrl ->
            announcementApi.getNextPageAnnouncementsList(nextUrl, params)
        }
    }
}
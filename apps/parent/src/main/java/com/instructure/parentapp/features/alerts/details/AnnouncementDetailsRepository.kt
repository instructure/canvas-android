package com.instructure.parentapp.features.alerts.details

import com.instructure.canvasapi2.apis.AccountNotificationAPI
import com.instructure.canvasapi2.apis.AnnouncementAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import javax.inject.Inject

class AnnouncementDetailsRepository @Inject constructor(
    private val announcementApi: AnnouncementAPI.AnnouncementInterface,
    private val courseApi: CourseAPI.CoursesInterface,
    private val accountNotificationApi: AccountNotificationAPI.AccountNotificationInterface
) {
    suspend fun getCourse(
        courseId: Long,
        forceNetwork: Boolean
    ): Course? {
        val restParams = RestParams(isForceReadFromNetwork = forceNetwork)
        return courseApi.getCourse(courseId, restParams).dataOrNull
    }

    suspend fun getCourseAnnouncement(
        courseId: Long,
        announcementId: Long,
        forceNetwork: Boolean
    ): DiscussionTopicHeader {
        val restParams = RestParams(isForceReadFromNetwork = forceNetwork)
        return announcementApi.getCourseAnnouncement(
            courseId,
            announcementId,
            restParams
        ).dataOrThrow
    }

    suspend fun getGlobalAnnouncement(
        announcementId: Long,
        forceNetwork: Boolean
    ): AccountNotification {
        val restParams = RestParams(isForceReadFromNetwork = forceNetwork)
        return accountNotificationApi.getAccountNotification(announcementId, restParams).dataOrThrow
    }
}

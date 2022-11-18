package com.instructure.teacher.features.dashboard.notifications

import com.instructure.canvasapi2.managers.*
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.features.dashboard.notifications.DashboardNotificationRepository

class TeacherDashboardNotificationRepository(
    private val courseManager: CourseManager,
    groupManager: GroupManager,
    enrollmentManager: EnrollmentManager,
    conferenceManager: ConferenceManager,
    accountNotificationManager: AccountNotificationManager,
) : DashboardNotificationRepository(groupManager, enrollmentManager, conferenceManager, accountNotificationManager) {
    override suspend fun getCourses(forceNetwork: Boolean): List<Course>? {
        return courseManager.getCoursesAsync(forceNetwork)
            .await().dataOrNull?.filter { it.isTeacher || it.isDesigner || it.isTA }
    }
}
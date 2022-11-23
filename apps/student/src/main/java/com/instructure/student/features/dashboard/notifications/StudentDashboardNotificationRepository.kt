package com.instructure.student.features.dashboard.notifications

import com.instructure.canvasapi2.managers.*
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.features.dashboard.notifications.DashboardNotificationRepository

class StudentDashboardNotificationRepository(
    private val courseManager: CourseManager,
    groupManager: GroupManager,
    enrollmentManager: EnrollmentManager,
    conferenceManager: ConferenceManager,
    accountNotificationManager: AccountNotificationManager,
) : DashboardNotificationRepository(groupManager, enrollmentManager, conferenceManager, accountNotificationManager) {

    override suspend fun getCourses(forceNetwork: Boolean): List<Course>? {
        return courseManager.getCoursesAsync(forceNetwork).await().dataOrNull?.filter { it.isStudent }
    }
}
package com.instructure.pandautils.features.dashboard.notifications

import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.managers.AccountNotificationManager
import com.instructure.canvasapi2.managers.ConferenceManager
import com.instructure.canvasapi2.managers.EnrollmentManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.models.*

abstract class DashboardNotificationRepository(
    private val groupManager: GroupManager,
    private val enrollmentManager: EnrollmentManager,
    private val conferenceManager: ConferenceManager,
    private val accountNotificationManager: AccountNotificationManager,
) {

    abstract suspend fun getCourses(forceNetwork: Boolean): List<Course>?

    suspend fun getGroups(forceNetwork: Boolean): List<Group>? {
        return groupManager.getAllGroupsAsync(forceNetwork).await().dataOrNull
    }

    suspend fun getInvitations(forceNetwork: Boolean): List<Enrollment>? {
        return enrollmentManager.getSelfEnrollmentsAsync(
            null,
            listOf(EnrollmentAPI.STATE_INVITED, EnrollmentAPI.STATE_CURRENT_AND_FUTURE),
            forceNetwork
        ).await()
            .dataOrNull
    }

    suspend fun getAccountNotifications(forceNetwork: Boolean): List<AccountNotification>? {
        return accountNotificationManager.getAllAccountNotificationsAsync(forceNetwork).await().dataOrNull
    }

    suspend fun getConferences(forceNetwork: Boolean): List<Conference>? {
        return conferenceManager.getLiveConferencesAsync(forceNetwork).await().dataOrNull
    }
}
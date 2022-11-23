package com.instructure.student.di

import com.instructure.canvasapi2.managers.*
import com.instructure.pandautils.features.dashboard.notifications.DashboardNotificationRepository
import com.instructure.student.features.dashboard.notifications.StudentDashboardNotificationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class DashboardNotificationModule {

    @Provides
    fun provideDashboardNotificationRepository(
        courseManager: CourseManager,
        groupManager: GroupManager,
        enrollmentManager: EnrollmentManager,
        conferenceManager: ConferenceManager,
        accountNotificationManager: AccountNotificationManager,
    ): DashboardNotificationRepository {
        return StudentDashboardNotificationRepository(
            courseManager,
            groupManager,
            enrollmentManager,
            conferenceManager,
            accountNotificationManager
        )
    }
}
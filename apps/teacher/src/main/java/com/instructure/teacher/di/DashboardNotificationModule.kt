package com.instructure.teacher.di

import com.instructure.canvasapi2.managers.*
import com.instructure.pandautils.features.dashboard.notifications.DashboardNotificationRepository
import com.instructure.teacher.features.dashboard.notifications.TeacherDashboardNotificationRepository
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
        accountNotificationManager: AccountNotificationManager
    ): DashboardNotificationRepository {
        return TeacherDashboardNotificationRepository(
            courseManager,
            groupManager,
            enrollmentManager,
            conferenceManager,
            accountNotificationManager
        )
    }
}
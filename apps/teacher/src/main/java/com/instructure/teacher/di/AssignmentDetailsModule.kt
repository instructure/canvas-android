package com.instructure.teacher.di

import com.instructure.pandautils.features.assignments.details.AssignmentDetailsRepository
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsRouter
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsSubmissionHandler
import com.instructure.pandautils.receivers.alarm.AlarmReceiverNotificationHandler
import com.instructure.teacher.features.assignment.details.TeacherAlarmReceiverNotificationHandler
import com.instructure.teacher.features.assignment.details.TeacherAssignmentDetailsRepository
import com.instructure.teacher.features.assignment.details.TeacherAssignmentDetailsRouter
import com.instructure.teacher.features.assignment.details.TeacherAssignmentDetailsSubmissionHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(FragmentComponent::class)
class AssignmentDetailsFragmentModule() {
    @Provides
    fun provideAssignmentDetailsRouter(): AssignmentDetailsRouter {
        return TeacherAssignmentDetailsRouter()
    }
}

@Module
@InstallIn(ViewModelComponent::class)
class AssignmentDetailsModule {
    @Provides
    fun provideAssignmentDetailsRepository(): AssignmentDetailsRepository {
        return TeacherAssignmentDetailsRepository()
    }

    @Provides
    fun provideAssignmentDetailsSubmissionHandler(): AssignmentDetailsSubmissionHandler {
        return TeacherAssignmentDetailsSubmissionHandler()
    }
}

@Module
@InstallIn(SingletonComponent::class)
class AssignmentDetailsSingletonModule {
    @Provides
    fun provideAssignmentDetailsNotificationHandler(): AlarmReceiverNotificationHandler {
        return TeacherAlarmReceiverNotificationHandler()
    }
}
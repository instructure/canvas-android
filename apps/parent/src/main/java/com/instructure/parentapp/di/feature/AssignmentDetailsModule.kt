package com.instructure.parentapp.di.feature

import com.instructure.pandautils.features.assignments.details.AssignmentDetailsRepository
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsRouter
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsSubmissionHandler
import com.instructure.pandautils.receivers.alarm.AlarmReceiverNotificationHandler
import com.instructure.parentapp.features.assignment.details.ParentAssignmentDetailsRepository
import com.instructure.parentapp.features.assignment.details.ParentAssignmentDetailsRouter
import com.instructure.parentapp.features.assignment.details.ParentAssignmentDetailsSubmissionHandler
import com.instructure.parentapp.features.assignment.details.receiver.ParentAlarmReceiverNotificationHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(FragmentComponent::class)
class AssignmentDetailsFragmentModule {
    @Provides
    fun provideAssignmentDetailsRouter(): AssignmentDetailsRouter {
        return ParentAssignmentDetailsRouter()
    }
}

@Module
@InstallIn(ViewModelComponent::class)
class AssignmentDetailsModule {
    @Provides
    fun provideAssignmentDetailsRepository(): AssignmentDetailsRepository {
        return ParentAssignmentDetailsRepository()
    }

    @Provides
    fun provideAssignmentDetailsSubmissionHandler(): AssignmentDetailsSubmissionHandler {
        return ParentAssignmentDetailsSubmissionHandler()
    }
}

@Module
@InstallIn(SingletonComponent::class)
class AssignmentDetailsSingletonModule {
    @Provides
    fun provideAssignmentDetailsNotificationHandler(): AlarmReceiverNotificationHandler {
        return ParentAlarmReceiverNotificationHandler()
    }
}
/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.pandautils.di

import com.instructure.canvasapi2.apis.AccountNotificationAPI
import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.ConferencesApi
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.managers.graphql.RecentGradedSubmissionsManager
import com.instructure.pandautils.data.repository.accountnotification.AccountNotificationRepository
import com.instructure.pandautils.data.repository.accountnotification.AccountNotificationRepositoryImpl
import com.instructure.pandautils.data.repository.assignment.AssignmentRepository
import com.instructure.pandautils.data.repository.assignment.AssignmentRepositoryImpl
import com.instructure.pandautils.data.repository.conference.ConferenceRepository
import com.instructure.pandautils.data.repository.conference.ConferenceRepositoryImpl
import com.instructure.pandautils.data.repository.course.CourseRepository
import com.instructure.pandautils.data.repository.course.CourseRepositoryImpl
import com.instructure.pandautils.data.repository.enrollment.EnrollmentRepository
import com.instructure.pandautils.data.repository.enrollment.EnrollmentRepositoryImpl
import com.instructure.pandautils.data.repository.group.GroupRepository
import com.instructure.pandautils.data.repository.group.GroupRepositoryImpl
import com.instructure.pandautils.data.repository.planner.PlannerRepository
import com.instructure.pandautils.data.repository.planner.PlannerRepositoryImpl
import com.instructure.pandautils.data.repository.submission.SubmissionRepository
import com.instructure.pandautils.data.repository.submission.SubmissionRepositoryImpl
import com.instructure.pandautils.data.repository.user.UserRepository
import com.instructure.pandautils.data.repository.user.UserRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun provideEnrollmentRepository(
        enrollmentApi: EnrollmentAPI.EnrollmentInterface
    ): EnrollmentRepository {
        return EnrollmentRepositoryImpl(enrollmentApi)
    }

    @Provides
    @Singleton
    fun provideCourseRepository(
        courseApi: CourseAPI.CoursesInterface
    ): CourseRepository {
        return CourseRepositoryImpl(courseApi)
    }

    @Provides
    @Singleton
    fun provideAccountNotificationRepository(
        accountNotificationApi: AccountNotificationAPI.AccountNotificationInterface
    ): AccountNotificationRepository {
        return AccountNotificationRepositoryImpl(accountNotificationApi)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        userApi: UserAPI.UsersInterface
    ): UserRepository {
        return UserRepositoryImpl(userApi)
    }

    @Provides
    @Singleton
    fun provideGroupRepository(
        groupApi: GroupAPI.GroupInterface
    ): GroupRepository {
        return GroupRepositoryImpl(groupApi)
    }

    @Provides
    @Singleton
    fun provideAssignmentRepository(
        userApi: UserAPI.UsersInterface,
        assignmentApi: AssignmentAPI.AssignmentInterface
    ): AssignmentRepository {
        return AssignmentRepositoryImpl(userApi, assignmentApi)
    }

    @Provides
    @Singleton
    fun providePlannerRepository(
        plannerApi: PlannerAPI.PlannerInterface
    ): PlannerRepository {
        return PlannerRepositoryImpl(plannerApi)
    }

    @Provides
    @Singleton
    fun provideSubmissionRepository(
        recentGradedSubmissionsManager: RecentGradedSubmissionsManager
    ): SubmissionRepository {
        return SubmissionRepositoryImpl(recentGradedSubmissionsManager)
    }

    @Provides
    @Singleton
    fun provideConferenceRepository(
        conferencesApi: ConferencesApi.ConferencesInterface
    ): ConferenceRepository {
        return ConferenceRepositoryImpl(conferencesApi)
    }
}
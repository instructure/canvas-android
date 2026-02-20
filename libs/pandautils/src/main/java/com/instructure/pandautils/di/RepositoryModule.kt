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
import com.instructure.canvasapi2.apis.AnnouncementAPI
import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.ConferencesApi
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.CourseNicknameAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.managers.graphql.RecentGradedSubmissionsManager
import com.instructure.pandautils.data.repository.accountnotification.AccountNotificationRepository
import com.instructure.pandautils.data.repository.accountnotification.AccountNotificationRepositoryImpl
import com.instructure.pandautils.data.repository.announcement.AnnouncementLocalDataSource
import com.instructure.pandautils.data.repository.announcement.AnnouncementNetworkDataSource
import com.instructure.pandautils.data.repository.announcement.AnnouncementRepository
import com.instructure.pandautils.data.repository.announcement.AnnouncementRepositoryImpl
import com.instructure.pandautils.data.repository.assignment.AssignmentRepository
import com.instructure.pandautils.data.repository.assignment.AssignmentRepositoryImpl
import com.instructure.pandautils.data.repository.conference.ConferenceRepository
import com.instructure.pandautils.data.repository.conference.ConferenceRepositoryImpl
import com.instructure.pandautils.data.repository.course.CourseLocalDataSource
import com.instructure.pandautils.data.repository.course.CourseNetworkDataSource
import com.instructure.pandautils.data.repository.course.CourseRepository
import com.instructure.pandautils.data.repository.course.CourseRepositoryImpl
import com.instructure.pandautils.data.repository.coursenickname.CourseNicknameRepository
import com.instructure.pandautils.data.repository.coursenickname.CourseNicknameRepositoryImpl
import com.instructure.pandautils.data.repository.enrollment.EnrollmentRepository
import com.instructure.pandautils.data.repository.enrollment.EnrollmentRepositoryImpl
import com.instructure.pandautils.data.repository.group.GroupLocalDataSource
import com.instructure.pandautils.data.repository.group.GroupNetworkDataSource
import com.instructure.pandautils.data.repository.group.GroupRepository
import com.instructure.pandautils.data.repository.group.GroupRepositoryImpl
import com.instructure.pandautils.data.repository.planner.PlannerRepository
import com.instructure.pandautils.data.repository.planner.PlannerRepositoryImpl
import com.instructure.pandautils.data.repository.submission.SubmissionRepository
import com.instructure.pandautils.data.repository.submission.SubmissionRepositoryImpl
import com.instructure.pandautils.data.repository.user.UserRepository
import com.instructure.pandautils.data.repository.user.UserRepositoryImpl
import com.instructure.pandautils.room.offline.daos.DashboardCardDao
import com.instructure.pandautils.room.offline.facade.CourseFacade
import com.instructure.pandautils.room.offline.facade.DiscussionTopicHeaderFacade
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
class RepositoryModule {

    @Provides
    @ActivityRetainedScoped
    fun provideEnrollmentRepository(
        enrollmentApi: EnrollmentAPI.EnrollmentInterface
    ): EnrollmentRepository {
        return EnrollmentRepositoryImpl(enrollmentApi)
    }

    @Provides
    fun provideCourseNetworkDataSource(
        courseApi: CourseAPI.CoursesInterface
    ): CourseNetworkDataSource {
        return CourseNetworkDataSource(courseApi)
    }

    @Provides
    fun provideCourseLocalDataSource(
        courseFacade: CourseFacade,
        dashboardCardDao: DashboardCardDao
    ): CourseLocalDataSource {
        return CourseLocalDataSource(courseFacade, dashboardCardDao)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideCourseRepository(
        localDataSource: CourseLocalDataSource,
        networkDataSource: CourseNetworkDataSource,
        networkStateProvider: NetworkStateProvider,
        featureFlagProvider: FeatureFlagProvider
    ): CourseRepository {
        return CourseRepositoryImpl(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideAccountNotificationRepository(
        accountNotificationApi: AccountNotificationAPI.AccountNotificationInterface
    ): AccountNotificationRepository {
        return AccountNotificationRepositoryImpl(accountNotificationApi)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideUserRepository(
        userApi: UserAPI.UsersInterface
    ): UserRepository {
        return UserRepositoryImpl(userApi)
    }

    @Provides
    fun provideAnnouncementNetworkDataSource(
        announcementApi: AnnouncementAPI.AnnouncementInterface
    ): AnnouncementNetworkDataSource {
        return AnnouncementNetworkDataSource(announcementApi)
    }

    @Provides
    fun provideAnnouncementLocalDataSource(
        discussionTopicHeaderFacade: DiscussionTopicHeaderFacade
    ): AnnouncementLocalDataSource {
        return AnnouncementLocalDataSource(discussionTopicHeaderFacade)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideAnnouncementRepository(
        localDataSource: AnnouncementLocalDataSource,
        networkDataSource: AnnouncementNetworkDataSource,
        networkStateProvider: NetworkStateProvider,
        featureFlagProvider: FeatureFlagProvider
    ): AnnouncementRepository {
        return AnnouncementRepositoryImpl(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideCourseNicknameRepository(
        courseNicknameApi: CourseNicknameAPI.NicknameInterface
    ): CourseNicknameRepository {
        return CourseNicknameRepositoryImpl(courseNicknameApi)
    }

    @Provides
    fun provideGroupNetworkDataSource(
        groupApi: GroupAPI.GroupInterface
    ): GroupNetworkDataSource {
        return GroupNetworkDataSource(groupApi)
    }

    @Provides
    fun provideGroupLocalDataSource(): GroupLocalDataSource {
        return GroupLocalDataSource()
    }

    @Provides
    @ActivityRetainedScoped
    fun provideGroupRepository(
        localDataSource: GroupLocalDataSource,
        networkDataSource: GroupNetworkDataSource,
        networkStateProvider: NetworkStateProvider,
        featureFlagProvider: FeatureFlagProvider
    ): GroupRepository {
        return GroupRepositoryImpl(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideAssignmentRepository(
        userApi: UserAPI.UsersInterface,
        assignmentApi: AssignmentAPI.AssignmentInterface
    ): AssignmentRepository {
        return AssignmentRepositoryImpl(userApi, assignmentApi)
    }

    @Provides
    @ActivityRetainedScoped
    fun providePlannerRepository(
        plannerApi: PlannerAPI.PlannerInterface
    ): PlannerRepository {
        return PlannerRepositoryImpl(plannerApi)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideSubmissionRepository(
        recentGradedSubmissionsManager: RecentGradedSubmissionsManager
    ): SubmissionRepository {
        return SubmissionRepositoryImpl(recentGradedSubmissionsManager)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideConferenceRepository(
        conferencesApi: ConferencesApi.ConferencesInterface
    ): ConferenceRepository {
        return ConferenceRepositoryImpl(conferencesApi)
    }
}
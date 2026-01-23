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
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.CourseNicknameAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.pandautils.data.repository.accountnotification.AccountNotificationRepository
import com.instructure.pandautils.data.repository.accountnotification.AccountNotificationRepositoryImpl
import com.instructure.pandautils.data.repository.course.CourseRepository
import com.instructure.pandautils.data.repository.course.CourseRepositoryImpl
import com.instructure.pandautils.data.repository.enrollment.EnrollmentRepository
import com.instructure.pandautils.data.repository.enrollment.EnrollmentRepositoryImpl
import com.instructure.pandautils.data.repository.group.GroupRepository
import com.instructure.pandautils.data.repository.group.GroupRepositoryImpl
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
        courseApi: CourseAPI.CoursesInterface,
        announcementApi: AnnouncementAPI.AnnouncementInterface
    ): CourseRepository {
        return CourseRepositoryImpl(courseApi, announcementApi)
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
        userApi: UserAPI.UsersInterface,
        courseNicknameApi: CourseNicknameAPI.NicknameInterface
    ): UserRepository {
        return UserRepositoryImpl(userApi, courseNicknameApi)
    }

    @Provides
    @Singleton
    fun provideGroupRepository(
        groupApi: GroupAPI.GroupInterface
    ): GroupRepository {
        return GroupRepositoryImpl(groupApi)
    }
}
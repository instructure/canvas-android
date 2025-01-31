/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
package com.instructure.teacher.di

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.apis.ProgressAPI
import com.instructure.canvasapi2.apis.RecipientAPI
import com.instructure.pandautils.features.inbox.compose.InboxComposeRepository
import com.instructure.pandautils.features.inbox.details.InboxDetailsBehavior
import com.instructure.pandautils.features.inbox.list.InboxRepository
import com.instructure.pandautils.features.inbox.list.InboxRouter
import com.instructure.teacher.features.inbox.compose.TeacherInboxComposeRepository
import com.instructure.teacher.features.inbox.details.TeacherInboxDetailsBehavior
import com.instructure.teacher.features.inbox.list.TeacherInboxRepository
import com.instructure.teacher.features.inbox.list.TeacherInboxRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(FragmentComponent::class)
class InboxFragmentModule {

    @Provides
    fun providesInboxRouter(activity: FragmentActivity, fragment: Fragment): InboxRouter {
        return TeacherInboxRouter(activity, fragment)
    }
}

@Module
@InstallIn(ViewModelComponent::class)
class InboxModule {

    @Provides
    fun provideInboxRepository(
        inboxApi: InboxApi.InboxInterface,
        coursesApi: CourseAPI.CoursesInterface,
        groupsApi: GroupAPI.GroupInterface,
        progressApi: ProgressAPI.ProgressInterface
    ): InboxRepository {
        return TeacherInboxRepository(inboxApi, coursesApi, groupsApi, progressApi)
    }

    @Provides
    fun provideInboxComposeRepository(
        coursesApi: CourseAPI.CoursesInterface,
        recipientApi: RecipientAPI.RecipientInterface,
        inboxApi: InboxApi.InboxInterface
    ): InboxComposeRepository {
        return TeacherInboxComposeRepository(coursesApi, recipientApi, inboxApi)
    }

    @Provides
    fun provideInboxDetailsBehavior(): InboxDetailsBehavior {
        return TeacherInboxDetailsBehavior()
    }

}
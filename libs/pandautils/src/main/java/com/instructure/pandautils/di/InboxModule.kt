/*
 * Copyright (C) 2022 - present Instructure, Inc.
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

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.apis.ProgressAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.pandautils.features.inbox.list.InboxRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class InboxModule {

    @Provides
    fun provideInboxRepository(): InboxRepository {
        val inboxApi = RestBuilder().build(InboxApi.InboxInterface::class.java, RestParams())
        val coursesApi = RestBuilder().build(CourseAPI.CoursesInterface::class.java, RestParams())
        val groupsApi = RestBuilder().build(GroupAPI.GroupInterface::class.java, RestParams())
        val progressApi = RestBuilder().build(ProgressAPI.ProgressInterface::class.java, RestParams())
        return InboxRepository(inboxApi, coursesApi, groupsApi, progressApi)
    }
}
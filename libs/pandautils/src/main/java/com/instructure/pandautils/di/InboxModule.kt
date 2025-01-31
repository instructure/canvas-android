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

import android.content.Context
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.inbox.details.InboxDetailsRepository
import com.instructure.pandautils.features.inbox.details.InboxDetailsRepositoryImpl
import com.instructure.pandautils.features.inbox.list.InboxEntryItemCreator
import com.instructure.pandautils.features.inbox.utils.InboxSharedEvents
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
class InboxModule {

    @Provides
    fun provideInboxEntryCreator(@ApplicationContext context: Context, apiPrefs: ApiPrefs): InboxEntryItemCreator {
        return InboxEntryItemCreator(context, apiPrefs)
    }

    @Provides
    fun provideInboxDetailsRepository(inboxAPI: InboxApi.InboxInterface): InboxDetailsRepository {
        return InboxDetailsRepositoryImpl(inboxAPI)
    }
}

@Module
@InstallIn(SingletonComponent::class)
class InboxSingletonModule {
    @Provides
    @Singleton
    fun provideInboxSharedEvents(): InboxSharedEvents {
        return InboxSharedEvents()
    }
}
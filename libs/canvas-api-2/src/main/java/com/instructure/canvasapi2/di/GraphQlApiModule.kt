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
package com.instructure.canvasapi2.di

import com.instructure.canvasapi2.managers.CommentLibraryManager
import com.instructure.canvasapi2.managers.CommentLibraryManagerImpl
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.canvasapi2.managers.InboxSettingsManagerImpl
import com.instructure.canvasapi2.managers.StudentContextManager
import com.instructure.canvasapi2.managers.StudentContextManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Provide classes for GraphQL related APIs. We need to separate this from the ApiModule if we want to overwrite these specific
 * providers in tests. Unfortunatelly Hilt doesn't allow overriding provider functions.
 */
@Module
@InstallIn(SingletonComponent::class)
class GraphQlApiModule {

    @Provides
    fun provideCommentLibraryManager(): CommentLibraryManager {
        return CommentLibraryManagerImpl()
    }

    @Provides
    fun provideInboxSettingsManager(): InboxSettingsManager {
        return InboxSettingsManagerImpl()
    }

    @Provides
    fun provideStudentContextManager(): StudentContextManager {
        return StudentContextManagerImpl()
    }
}
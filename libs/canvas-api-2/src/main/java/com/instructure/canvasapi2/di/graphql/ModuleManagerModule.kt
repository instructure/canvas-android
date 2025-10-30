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
package com.instructure.canvasapi2.di.graphql

import com.apollographql.apollo.ApolloClient
import com.instructure.canvasapi2.di.DefaultApolloClient
import com.instructure.canvasapi2.managers.graphql.ModuleManager
import com.instructure.canvasapi2.managers.graphql.ModuleManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class ModuleManagerModule {

    @Provides
    fun provideModuleManager(@DefaultApolloClient apolloClient: ApolloClient): ModuleManager {
        return ModuleManagerImpl(apolloClient)
    }

}

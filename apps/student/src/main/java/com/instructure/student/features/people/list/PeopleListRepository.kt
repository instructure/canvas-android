/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.student.features.people.list

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider

class PeopleListRepository(
        peopleListLocalDataSource: PeopleListLocalDataSource,
        peopleListNetworkDataSource: PeopleListNetworkDataSource,
        networkStateProvider: NetworkStateProvider,
        featureFlagProvider: FeatureFlagProvider
) : Repository<PeopleListDataSource>(peopleListLocalDataSource, peopleListNetworkDataSource, networkStateProvider, featureFlagProvider) {

    suspend fun loadFirstPagePeople(canvasContext: CanvasContext, forceNetwork: Boolean): DataResult<List<User>> {
        return dataSource().loadFirstPagePeople(canvasContext, forceNetwork)
    }

    suspend fun loadNextPagePeople(canvasContext: CanvasContext, forceNetwork: Boolean, nextUrl: String = ""): DataResult<List<User>> {
        return dataSource().loadNextPagePeople(canvasContext, forceNetwork, nextUrl)
    }

    suspend fun loadTeachers(canvasContext: CanvasContext, forceNetwork: Boolean): DataResult<List<User>> {
        return dataSource().loadTeachers(canvasContext, forceNetwork)
    }

    suspend fun loadTAs(canvasContext: CanvasContext, forceNetwork: Boolean): DataResult<List<User>> {
        return dataSource().loadTAs(canvasContext, forceNetwork)
    }
}
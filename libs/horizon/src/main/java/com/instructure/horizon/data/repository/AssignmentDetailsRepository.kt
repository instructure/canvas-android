/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.horizon.data.repository

import com.instructure.canvasapi2.models.Assignment
import com.instructure.horizon.data.datasource.AssignmentDetailsLocalDataSource
import com.instructure.horizon.data.datasource.AssignmentDetailsNetworkDataSource
import com.instructure.horizon.di.HorizonHtmlParserQualifier
import com.instructure.horizon.offline.OfflineSyncRepository
import com.instructure.pandautils.features.offline.sync.HtmlParser
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import javax.inject.Inject

class AssignmentDetailsRepository @Inject constructor(
    private val networkDataSource: AssignmentDetailsNetworkDataSource,
    private val localDataSource: AssignmentDetailsLocalDataSource,
    @HorizonHtmlParserQualifier private val htmlParser: HtmlParser,
    private val fileSyncRepository: HorizonFileSyncRepository,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
) : OfflineSyncRepository(networkStateProvider, featureFlagProvider) {

    suspend fun getAssignment(courseId: Long, assignmentId: Long, forceRefresh: Boolean): Assignment {
        return if (shouldFetchFromNetwork()) {
            networkDataSource.getAssignment(courseId, assignmentId, forceRefresh).also { assignment ->
                if (shouldSync()) {
                    val parsingResult = htmlParser.createHtmlStringWithLocalFiles(assignment.description, courseId)
                    localDataSource.saveAssignment(assignment, courseId, parsingResult.htmlWithLocalFileLinks)
                    fileSyncRepository.syncHtmlFiles(courseId, parsingResult)
                }
            }
        } else {
            localDataSource.getAssignment(assignmentId)
                ?: throw IllegalStateException("Assignment $assignmentId not available offline")
        }
    }

    override suspend fun sync() {
        TODO("Not yet implemented")
    }
}

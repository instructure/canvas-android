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
package com.instructure.horizon.offline.sync

import com.instructure.horizon.data.datasource.PageLocalDataSource
import com.instructure.horizon.data.datasource.PageNetworkDataSource
import com.instructure.horizon.data.repository.HorizonFileSyncRepository
import com.instructure.horizon.di.HorizonHtmlParserQualifier
import com.instructure.pandautils.features.offline.sync.HtmlParser
import javax.inject.Inject

class PageSyncer @Inject constructor(
    private val networkDataSource: PageNetworkDataSource,
    private val localDataSource: PageLocalDataSource,
    @HorizonHtmlParserQualifier private val htmlParser: HtmlParser,
    private val fileSyncRepository: HorizonFileSyncRepository,
) {
    suspend fun syncPages(courseId: Long, pageUrls: List<String>): ContentSyncResult {
        val additionalFileIds = mutableSetOf<Long>()
        val externalFileUrls = mutableSetOf<String>()

        for (pageUrl in pageUrls) {
            try {
                val page = networkDataSource.getPage(courseId, pageUrl, forceRefresh = true)
                val parsedBody = page.body?.let {
                    htmlParser.createHtmlStringWithLocalFiles(it, courseId)
                }
                if (parsedBody != null) {
                    additionalFileIds.addAll(parsedBody.internalFileIds)
                    externalFileUrls.addAll(parsedBody.externalFileUrls)
                    fileSyncRepository.syncHtmlFiles(courseId, parsedBody)
                }
                localDataSource.savePage(page, courseId, parsedBody?.htmlWithLocalFileLinks)
            } catch (_: Exception) {
                // Skip individual page failures
            }
        }

        return ContentSyncResult(
            additionalFileIds = additionalFileIds,
            externalFileUrls = externalFileUrls,
        )
    }
}

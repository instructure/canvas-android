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
package com.instructure.horizon.data.datasource

import com.instructure.canvasapi2.models.Page
import com.instructure.horizon.database.dao.HorizonPageDao
import com.instructure.horizon.database.entity.HorizonPageEntity
import javax.inject.Inject

class PageLocalDataSource @Inject constructor(
    private val pageDao: HorizonPageDao,
) {

    suspend fun getPage(courseId: Long, pageUrl: String): Page? {
        return pageDao.getPage(courseId, pageUrl)?.toPage()
    }

    suspend fun savePage(page: Page, courseId: Long, parsedBody: String?) {
        pageDao.savePage(
            HorizonPageEntity(
                pageId = page.id,
                courseId = courseId,
                pageUrl = page.url.orEmpty(),
                title = page.title,
                body = parsedBody,
            )
        )
    }

    private fun HorizonPageEntity.toPage(): Page {
        return Page(
            id = pageId,
            url = pageUrl,
            title = title,
            body = body,
        )
    }
}

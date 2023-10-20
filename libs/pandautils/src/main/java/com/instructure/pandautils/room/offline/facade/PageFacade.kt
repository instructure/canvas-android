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

package com.instructure.pandautils.room.offline.facade

import androidx.room.withTransaction
import com.instructure.canvasapi2.models.Page
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.daos.PageDao
import com.instructure.pandautils.room.offline.entities.PageEntity

class PageFacade(
    private val pageDao: PageDao,
    private val lockedInfoFacade: LockInfoFacade,
    private val offlineDatabase: OfflineDatabase
) {
    suspend fun insertPages(pages: List<Page>, courseId: Long) {
        offlineDatabase.withTransaction {
            deleteAllByCourseId(courseId)
            pageDao.insertAll(pages.map { PageEntity(it, courseId) })
            pages.forEach { page ->
                page.lockInfo?.let {
                    lockedInfoFacade.insertLockInfoForPage(it, page.id)
                }
            }
        }
    }

    suspend fun insertPage(page: Page, courseId: Long) {
        offlineDatabase.withTransaction {
            pageDao.insert(PageEntity(page, courseId))
            page.lockInfo?.let {
                lockedInfoFacade.insertLockInfoForPage(it, page.id)
            }
        }
    }

    suspend fun getFrontPage(courseId: Long): Page? {
        return pageDao.getFrontPage(courseId)?.let { createFullApiModel(it) }
    }

    suspend fun findByCourseId(courseId: Long): List<Page> {
        return pageDao.findByCourseId(courseId).map { createFullApiModel(it) }
    }

    suspend fun getPageDetails(courseId: Long, pageId: String): Page? {
        return pageDao.getPageDetails(courseId, pageId)?.let { createFullApiModel(it) }
    }

    suspend fun deleteAllByCourseId(courseId: Long) {
        pageDao.deleteAllByCourseId(courseId)
    }

    private suspend fun createFullApiModel(pageEntity: PageEntity): Page {
        val lockInfo = lockedInfoFacade.getLockInfoByPageId(pageEntity.id)
        return pageEntity.toApiModel(lockInfo)
    }
}

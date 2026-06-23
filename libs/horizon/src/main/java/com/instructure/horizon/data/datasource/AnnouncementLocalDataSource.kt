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

import com.instructure.horizon.database.dao.HorizonAnnouncementDao
import com.instructure.horizon.database.dao.HorizonSyncMetadataDao
import com.instructure.horizon.database.entity.ANNOUNCEMENT_TYPE_ACCOUNT
import com.instructure.horizon.database.entity.ANNOUNCEMENT_TYPE_COURSE
import com.instructure.horizon.database.entity.HorizonAnnouncementCommentEntity
import com.instructure.horizon.database.entity.HorizonAnnouncementEntity
import com.instructure.horizon.database.entity.HorizonSyncMetadataEntity
import com.instructure.horizon.database.entity.SyncDataType
import com.instructure.horizon.database.entity.announcementKey
import com.instructure.horizon.model.HorizonAnnouncement
import com.instructure.horizon.model.HorizonAnnouncementComment
import com.instructure.horizon.model.HorizonAnnouncementDetails
import com.instructure.horizon.model.HorizonAnnouncementKind
import java.util.Date
import javax.inject.Inject

class AnnouncementLocalDataSource @Inject constructor(
    private val dao: HorizonAnnouncementDao,
    private val syncMetadataDao: HorizonSyncMetadataDao,
) {
    suspend fun getAll(): List<HorizonAnnouncement> {
        return dao.getAll().map { it.toDomain() }
    }

    suspend fun getUnread(): List<HorizonAnnouncement> {
        return dao.getUnread().map { it.toDomain() }
    }

    suspend fun getDetails(kind: HorizonAnnouncementKind, id: Long, courseId: Long?): HorizonAnnouncementDetails? {
        val key = announcementKey(kind.toEntityType(), id, courseId)
        val announcement = dao.findByKey(key)?.toDomain() ?: return null
        val comments = dao.getCommentsForAnnouncement(key).map { it.toCommentDomain() }
        return HorizonAnnouncementDetails(announcement, comments)
    }

    suspend fun replaceAll(
        announcements: List<HorizonAnnouncement>,
        comments: Map<String, List<HorizonAnnouncementComment>>,
    ) {
        val entities = announcements.map { it.toEntity() }
        val commentEntities = comments.flatMap { (key, list) ->
            list.mapIndexed { index, comment ->
                HorizonAnnouncementCommentEntity(
                    announcementKey = key,
                    id = comment.id,
                    authorDisplayName = comment.authorDisplayName,
                    message = comment.message,
                    createdAtMs = comment.createdAt?.time,
                    position = index,
                )
            }
        }
        dao.replaceAll(entities, commentEntities)
        syncMetadataDao.upsert(
            HorizonSyncMetadataEntity(
                dataType = SyncDataType.ANNOUNCEMENTS,
                lastSyncedAtMs = System.currentTimeMillis(),
            )
        )
    }

    suspend fun markRead(kind: HorizonAnnouncementKind, id: Long, courseId: Long?) {
        dao.markRead(announcementKey(kind.toEntityType(), id, courseId))
    }

    suspend fun markClosed(kind: HorizonAnnouncementKind, id: Long, courseId: Long?) {
        dao.markClosed(announcementKey(kind.toEntityType(), id, courseId))
    }

    private fun HorizonAnnouncementEntity.toDomain() = HorizonAnnouncement(
        id = id,
        kind = if (type == ANNOUNCEMENT_TYPE_COURSE) HorizonAnnouncementKind.COURSE else HorizonAnnouncementKind.ACCOUNT,
        courseId = courseId,
        courseName = courseName,
        title = title,
        message = message,
        authorDisplayName = authorDisplayName,
        postedAt = postedAtMs?.let { Date(it) },
        isUnread = isUnread,
        isClosed = isClosed,
    )

    private fun HorizonAnnouncementCommentEntity.toCommentDomain() = HorizonAnnouncementComment(
        id = id,
        authorDisplayName = authorDisplayName,
        message = message,
        createdAt = createdAtMs?.let { Date(it) },
    )

    private fun HorizonAnnouncement.toEntity() = HorizonAnnouncementEntity(
        key = announcementKey(kind.toEntityType(), id, courseId),
        id = id,
        type = kind.toEntityType(),
        courseId = courseId,
        courseName = courseName,
        title = title,
        message = message,
        authorDisplayName = authorDisplayName,
        postedAtMs = postedAt?.time,
        isUnread = isUnread,
        isClosed = isClosed,
    )

    private fun HorizonAnnouncementKind.toEntityType(): String = when (this) {
        HorizonAnnouncementKind.COURSE -> ANNOUNCEMENT_TYPE_COURSE
        HorizonAnnouncementKind.ACCOUNT -> ANNOUNCEMENT_TYPE_ACCOUNT
    }
}

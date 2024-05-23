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
import com.instructure.canvasapi2.models.Conference
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.daos.ConferenceDao
import com.instructure.pandautils.room.offline.daos.ConferenceRecodingDao
import com.instructure.pandautils.room.offline.entities.ConferenceEntity
import com.instructure.pandautils.room.offline.entities.ConferenceRecordingEntity

class ConferenceFacade(
    private val conferenceDao: ConferenceDao,
    private val conferenceRecodingDao: ConferenceRecodingDao,
    private val offlineDatabase: OfflineDatabase
) {

    suspend fun insertConferences(conferences: List<Conference>, courseId: Long) {
        offlineDatabase.withTransaction {
            deleteAllByCourseId(courseId)

            conferenceDao.insertAll(conferences.map { ConferenceEntity(it, courseId) })

            conferences.forEach { conference ->
                conference.recordings.forEach { recording ->
                    conferenceRecodingDao.insert(ConferenceRecordingEntity(recording, conference.id))
                }
            }
        }
    }

    suspend fun getConferencesByCourseId(courseId: Long): List<Conference> {
        return conferenceDao.findByCourseId(courseId).map { conferenceEntity ->
            val recordings = conferenceRecodingDao.findByConferenceId(conferenceEntity.id).map { it.toApiModel() }
            conferenceEntity.toApiModel(recordings)
        }
    }

    suspend fun deleteAllByCourseId(courseId: Long) {
        conferenceDao.deleteAllByCourseId(courseId)
    }
}

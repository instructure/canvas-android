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

import com.instructure.horizon.data.datasource.CourseDetailsLocalDataSource
import com.instructure.horizon.data.datasource.CourseDetailsNetworkDataSource
import com.instructure.horizon.data.datasource.CourseProgressLocalDataSource
import com.instructure.horizon.data.datasource.CourseProgressNetworkDataSource
import com.instructure.horizon.data.repository.HorizonFileSyncRepository
import com.instructure.horizon.database.dao.HorizonEntitySyncMetadataDao
import com.instructure.horizon.database.entity.EntitySyncType
import com.instructure.horizon.database.entity.HorizonEntitySyncMetadataEntity
import com.instructure.horizon.di.HorizonHtmlParserQualifier
import com.instructure.pandautils.features.offline.sync.HtmlParser
import javax.inject.Inject

class CourseContentSyncer @Inject constructor(
    private val courseDetailsNetworkDataSource: CourseDetailsNetworkDataSource,
    private val courseDetailsLocalDataSource: CourseDetailsLocalDataSource,
    private val courseProgressNetworkDataSource: CourseProgressNetworkDataSource,
    private val courseProgressLocalDataSource: CourseProgressLocalDataSource,
    @HorizonHtmlParserQualifier private val htmlParser: HtmlParser,
    private val fileSyncRepository: HorizonFileSyncRepository,
    private val entitySyncMetadataDao: HorizonEntitySyncMetadataDao,
) {
    suspend fun sync(courseId: Long): CourseSyncResult {
        val additionalFileIds = mutableSetOf<Long>()
        val externalFileUrls = mutableSetOf<String>()

        val course = courseDetailsNetworkDataSource.getCourse(courseId, forceRefresh = true)
        val parsedSyllabus = htmlParser.createHtmlStringWithLocalFiles(course.courseSyllabus, course.courseId)
        additionalFileIds.addAll(parsedSyllabus.internalFileIds)
        externalFileUrls.addAll(parsedSyllabus.externalFileUrls)

        val programs = courseDetailsNetworkDataSource.getProgramsForCourse(courseId, forceRefresh = true)
        courseDetailsLocalDataSource.saveCourseDetails(
            course.copy(courseSyllabus = parsedSyllabus.htmlWithLocalFileLinks),
            programs,
        )
        fileSyncRepository.syncHtmlFiles(courseId, parsedSyllabus)

        val modules = courseProgressNetworkDataSource.getModuleItems(courseId, forceRefresh = true)
        courseProgressLocalDataSource.saveModuleItems(courseId, modules)

        val now = System.currentTimeMillis()
        entitySyncMetadataDao.upsert(HorizonEntitySyncMetadataEntity(EntitySyncType.COURSE, courseId, now))
        modules.flatMap { it.items }.forEach { item ->
            entitySyncMetadataDao.upsert(HorizonEntitySyncMetadataEntity(EntitySyncType.MODULE_ITEM, item.id, now))
        }

        return CourseSyncResult(
            moduleItems = modules,
            additionalFileIds = additionalFileIds,
            externalFileUrls = externalFileUrls,
        )
    }
}

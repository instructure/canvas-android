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

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.horizon.data.datasource.CourseDetailsLocalDataSource
import com.instructure.horizon.data.datasource.CourseDetailsNetworkDataSource
import com.instructure.horizon.data.datasource.CourseProgressLocalDataSource
import com.instructure.horizon.data.datasource.CourseProgressNetworkDataSource
import com.instructure.horizon.offline.OfflineSyncRepository
import com.instructure.pandautils.features.offline.sync.HtmlParser
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import javax.inject.Inject

class CourseRepository @Inject constructor(
    private val courseDetailsNetworkDataSource: CourseDetailsNetworkDataSource,
    private val courseDetailsLocalDataSource: CourseDetailsLocalDataSource,
    private val courseProgressNetworkDataSource: CourseProgressNetworkDataSource,
    private val courseProgressLocalDataSource: CourseProgressLocalDataSource,
    private val htmlParser: HtmlParser,
    private val fileSyncRepository: HorizonFileSyncRepository,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
) : OfflineSyncRepository(networkStateProvider, featureFlagProvider) {

    suspend fun getCourse(courseId: Long, forceRefresh: Boolean): CourseWithProgress {
        return if (shouldFetchFromNetwork()) {
            courseDetailsNetworkDataSource.getCourse(courseId, forceRefresh)
                .also { course ->
                    if (shouldSync()) {
                        val parsedSyllabus = htmlParser.createHtmlStringWithLocalFiles(course.courseSyllabus, course.courseId)
                        val programs = courseDetailsNetworkDataSource.getProgramsForCourse(courseId, forceRefresh)
                        courseDetailsLocalDataSource.saveCourseDetails(course.copy(courseSyllabus = parsedSyllabus.htmlWithLocalFileLinks), programs)
                        fileSyncRepository.syncHtmlFiles(course.courseId, parsedSyllabus)
                    }
                }
        } else {
            courseDetailsLocalDataSource.getCourse(courseId)
        }
    }

    suspend fun getProgramsForCourse(courseId: Long, forceRefresh: Boolean): List<Program> {
        return if (shouldFetchFromNetwork()) {
            courseDetailsNetworkDataSource.getProgramsForCourse(courseId, forceRefresh)
        } else {
            courseDetailsLocalDataSource.getProgramsForCourse(courseId)
        }
    }

    suspend fun hasExternalTools(courseId: Long, forceRefresh: Boolean): Boolean {
        return if (shouldFetchFromNetwork()) {
            courseDetailsNetworkDataSource.hasExternalTools(courseId, forceRefresh)
        } else {
            false
        }
    }

    suspend fun getModuleItems(courseId: Long, forceRefresh: Boolean): List<ModuleObject> {
        return if (shouldFetchFromNetwork()) {
            courseProgressNetworkDataSource.getModuleItems(courseId, forceRefresh)
                .also { if (shouldSync()) courseProgressLocalDataSource.saveModuleItems(courseId, it) }
        } else {
            courseProgressLocalDataSource.getModuleItems(courseId)
        }
    }

    override suspend fun sync() {
        TODO("Not yet implemented")
    }
}

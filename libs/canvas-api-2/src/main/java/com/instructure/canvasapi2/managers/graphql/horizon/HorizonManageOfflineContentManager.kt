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
package com.instructure.canvasapi2.managers.graphql.horizon

import com.apollographql.apollo.ApolloClient
import com.instructure.canvasapi2.HorizonManageOfflineContentQuery
import com.instructure.canvasapi2.enqueueQuery
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import java.util.Date

data class OfflineCourseWithFiles(
    val courseId: Long,
    val courseName: String,
    val files: List<OfflineFile>,
)

data class OfflineFile(
    val fileId: Long,
    val displayName: String,
    val sizeBytes: Long,
    val isLocked: Boolean,
)

interface HorizonManageOfflineContentManager {
    suspend fun getCoursesWithFiles(userId: Long, forceNetwork: Boolean = false): DataResult<List<OfflineCourseWithFiles>>
}

class HorizonManageOfflineContentManagerImpl(
    private val apolloClient: ApolloClient,
) : HorizonManageOfflineContentManager {

    override suspend fun getCoursesWithFiles(userId: Long, forceNetwork: Boolean): DataResult<List<OfflineCourseWithFiles>> {
        return try {
            val query = HorizonManageOfflineContentQuery(id = userId.toString())
            val result = apolloClient.enqueueQuery(query, forceNetwork).dataAssertNoErrors
            val enrollments = result.legacyNode?.onUser?.enrollments.orEmpty()

            val now = Date()
            val courses = enrollments.mapNotNull { enrollment ->
                val course = enrollment?.course ?: return@mapNotNull null
                val courseId = course._id.toLongOrNull() ?: return@mapNotNull null

                val files = course.modulesConnection?.edges.orEmpty()
                    .asSequence()
                    .mapNotNull { it?.node }
                    .flatMap { (it.moduleItems ?: emptyList()).asSequence() }
                    .mapNotNull { it?.content?.onFile }
                    .mapNotNull { file ->
                        val fileId = file._id.toLongOrNull() ?: return@mapNotNull null
                        OfflineFile(
                            fileId = fileId,
                            displayName = file.displayName.orEmpty(),
                            sizeBytes = (file.sizeBytes ?: 0).toLong(),
                            isLocked = isFileLocked(file.locked, file.lockAt, file.unlockAt, file.url, now),
                        )
                    }
                    .distinctBy { it.fileId }
                    .toList()

                OfflineCourseWithFiles(
                    courseId = courseId,
                    courseName = course.name.orEmpty(),
                    files = files,
                )
            }
            DataResult.Success(courses)
        } catch (e: Exception) {
            DataResult.Fail(Failure.Exception(e))
        }
    }

    private fun isFileLocked(locked: Boolean?, lockAt: Date?, unlockAt: Date?, url: String?, now: Date): Boolean {
        if (url.isNullOrBlank()) return true
        if (locked == true) return true
        if (lockAt != null && lockAt.before(now)) return true
        if (unlockAt != null && unlockAt.after(now)) return true
        return false
    }
}

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
package com.instructure.horizon.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.instructure.horizon.offline.sync.HorizonProgressState

@Entity(tableName = "horizon_course_sync_plan")
data class HorizonCourseSyncPlanEntity(
    @PrimaryKey val courseId: Long,
    val courseName: String,
    val syncModules: Boolean = true,
    val syncAssignments: Boolean = true,
    val syncPages: Boolean = true,
    val syncScores: Boolean = true,
    val syncFiles: Boolean = true,
    val state: HorizonProgressState = HorizonProgressState.PENDING,
    val modulesState: HorizonProgressState = HorizonProgressState.PENDING,
    val assignmentsState: HorizonProgressState = HorizonProgressState.PENDING,
    val pagesState: HorizonProgressState = HorizonProgressState.PENDING,
    val scoresState: HorizonProgressState = HorizonProgressState.PENDING,
    val filesState: HorizonProgressState = HorizonProgressState.PENDING,
) {
    companion object {
        const val CATEGORY_SIZE = 100_000L // 100KB weight per category for progress calculation
    }

    val totalCategoryCount: Int
        get() = listOf(syncModules, syncAssignments, syncPages, syncScores, syncFiles).count { it }

    val completedCategoryCount: Int
        get() {
            var count = 0
            if (syncModules && modulesState == HorizonProgressState.COMPLETED) count++
            if (syncAssignments && assignmentsState == HorizonProgressState.COMPLETED) count++
            if (syncPages && pagesState == HorizonProgressState.COMPLETED) count++
            if (syncScores && scoresState == HorizonProgressState.COMPLETED) count++
            if (syncFiles && filesState == HorizonProgressState.COMPLETED) count++
            return count
        }

    val totalSize: Long get() = totalCategoryCount * CATEGORY_SIZE

    val downloadedSize: Long get() = completedCategoryCount * CATEGORY_SIZE
}

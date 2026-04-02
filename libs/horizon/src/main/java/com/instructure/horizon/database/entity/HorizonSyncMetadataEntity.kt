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

@Entity(tableName = "horizon_sync_metadata")
data class HorizonSyncMetadataEntity(
    @PrimaryKey val key: String,
    val lastSyncedAtMs: Long,
) {
    companion object {
        const val KEY_DASHBOARD_ENROLLMENTS = "dashboard_enrollments"
        const val KEY_DASHBOARD_PROGRAMS = "dashboard_programs"
        const val KEY_DASHBOARD_MODULE_ITEMS = "dashboard_module_items"
    }
}

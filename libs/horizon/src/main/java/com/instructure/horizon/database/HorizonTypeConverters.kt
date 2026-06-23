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
package com.instructure.horizon.database

import androidx.room.TypeConverter
import com.instructure.horizon.database.entity.EntitySyncType
import com.instructure.horizon.database.entity.SyncDataType
import com.instructure.horizon.features.account.offlinesettings.SyncFrequency
import com.instructure.horizon.offline.sync.HorizonProgressState
import java.util.Date

class HorizonTypeConverters {

    @TypeConverter
    fun fromSyncDataType(value: SyncDataType): String = value.name

    @TypeConverter
    fun toSyncDataType(value: String): SyncDataType = SyncDataType.valueOf(value)

    @TypeConverter
    fun fromEntitySyncType(value: EntitySyncType): String = value.name

    @TypeConverter
    fun toEntitySyncType(value: String): EntitySyncType = EntitySyncType.valueOf(value)

    @TypeConverter
    fun fromDate(value: Date?): Long? = value?.time

    @TypeConverter
    fun toDate(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun fromHorizonProgressState(value: HorizonProgressState): String = value.name

    @TypeConverter
    fun toHorizonProgressState(value: String): HorizonProgressState = HorizonProgressState.valueOf(value)

    @TypeConverter
    fun fromSyncFrequency(value: SyncFrequency): String = value.name

    @TypeConverter
    fun toSyncFrequency(value: String): SyncFrequency = SyncFrequency.valueOf(value)
}
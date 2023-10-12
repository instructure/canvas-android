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

package com.instructure.pandautils.room.offline

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.instructure.canvasapi2.models.GradingRule
import com.instructure.pandautils.features.offline.sync.TabSyncData
import com.instructure.pandautils.features.offline.sync.settings.SyncFrequency
import com.instructure.pandautils.utils.toJson

@TypeConverters
class OfflineConverters {

    @TypeConverter
    fun fromGradingRule(gradingRule: GradingRule?): String? {
        if (gradingRule == null) return null
        return Gson().toJson(gradingRule)
    }

    @TypeConverter
    fun toGradingRule(s: String?): GradingRule? {
        if (s == null) return null
        return Gson().fromJson(s, GradingRule::class.java)
    }

    @TypeConverter
    fun fromSyncFrequency(syncFrequency: SyncFrequency): String {
        return syncFrequency.name
    }

    @TypeConverter
    fun toSyncFrequency(string: String): SyncFrequency {
        return SyncFrequency.valueOf(string)
    }

    @TypeConverter
    fun stringToTabSyncMap(value: String): Map<String, TabSyncData> {
        return Gson().fromJson(value,  object : TypeToken<Map<String, TabSyncData>>() {}.type)
    }

    @TypeConverter
    fun tabSyncMapToString(value: Map<String, TabSyncData>): String {
        return value.toJson()
    }
}
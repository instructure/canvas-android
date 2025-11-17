/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetMetadataDao {

    @Upsert
    suspend fun upsertMetadata(metadata: WidgetMetadataEntity)

    @Query("SELECT * FROM widget_metadata WHERE widgetId = :widgetId")
    fun observeMetadata(widgetId: String): Flow<WidgetMetadataEntity?>

    @Query("SELECT * FROM widget_metadata ORDER BY position ASC")
    fun observeAllMetadata(): Flow<List<WidgetMetadataEntity>>

    @Query("UPDATE widget_metadata SET position = :position WHERE widgetId = :widgetId")
    suspend fun updatePosition(widgetId: String, position: Int)

    @Query("UPDATE widget_metadata SET isVisible = :isVisible WHERE widgetId = :widgetId")
    suspend fun updateVisibility(widgetId: String, isVisible: Boolean)

    @Delete
    suspend fun deleteMetadata(metadata: WidgetMetadataEntity)
}
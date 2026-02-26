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

package com.instructure.pandautils.features.dashboard.widget.repository

import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.db.WidgetMetadataDao
import com.instructure.pandautils.features.dashboard.widget.db.WidgetMetadataEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface WidgetMetadataRepository {
    fun observeAllMetadata(): Flow<List<WidgetMetadata>>
    suspend fun saveMetadata(metadata: WidgetMetadata)
    suspend fun updatePosition(widgetId: String, position: Int)
    suspend fun updateVisibility(widgetId: String, isVisible: Boolean)
    suspend fun swapPositions(widgetId1: String, widgetId2: String)
}

@Singleton
class WidgetMetadataRepositoryImpl @Inject constructor(
    private val dao: WidgetMetadataDao
) : WidgetMetadataRepository {

    override fun observeAllMetadata(): Flow<List<WidgetMetadata>> {
        return dao.observeAllMetadata().map { entities ->
            entities.map { it.toMetadata() }
        }
    }

    override suspend fun saveMetadata(metadata: WidgetMetadata) {
        dao.upsertMetadata(metadata.toEntity())
    }

    override suspend fun updatePosition(widgetId: String, position: Int) {
        dao.updatePosition(widgetId, position)
    }

    override suspend fun updateVisibility(widgetId: String, isVisible: Boolean) {
        dao.updateVisibility(widgetId, isVisible)
    }

    override suspend fun swapPositions(widgetId1: String, widgetId2: String) {
        dao.swapPositions(widgetId1, widgetId2)
    }

    private fun WidgetMetadataEntity.toMetadata() = WidgetMetadata(
        id = widgetId,
        position = position,
        isVisible = isVisible,
        isEditable = isEditable
    )

    private fun WidgetMetadata.toEntity() = WidgetMetadataEntity(
        widgetId = id,
        position = position,
        isVisible = isVisible,
        isEditable = isEditable
    )
}
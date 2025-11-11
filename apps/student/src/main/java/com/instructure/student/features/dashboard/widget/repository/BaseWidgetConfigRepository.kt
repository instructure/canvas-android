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

package com.instructure.student.features.dashboard.widget.repository

import com.instructure.student.features.dashboard.widget.WidgetConfig
import com.instructure.student.features.dashboard.widget.db.WidgetConfigDao
import com.instructure.student.features.dashboard.widget.db.WidgetConfigEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

abstract class BaseWidgetConfigRepository<T : WidgetConfig>(
    private val dao: WidgetConfigDao
) : WidgetConfigRepository<T> {

    abstract fun deserializeConfig(json: String): T?
    abstract fun getDefaultConfig(): T

    override fun observeConfig(widgetId: String): Flow<T> {
        return dao.observeConfig(widgetId).map { entity ->
            entity?.configJson?.let { deserializeConfig(it) } ?: getDefaultConfig()
        }
    }

    override suspend fun saveConfig(config: T) {
        dao.upsertConfig(WidgetConfigEntity(config.widgetId, config.toJson()))
    }

    override suspend fun deleteConfig(widgetId: String) {
        val entity = dao.observeConfig(widgetId).first()
        entity?.let { dao.deleteConfig(it) }
    }
}
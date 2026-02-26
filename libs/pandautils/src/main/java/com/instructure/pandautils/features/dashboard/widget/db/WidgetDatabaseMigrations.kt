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

import com.instructure.pandautils.room.common.createMigration

val widgetDatabaseMigrations = arrayOf(

    createMigration(1, 2) { database ->
        database.execSQL("ALTER TABLE widget_metadata ADD COLUMN isEditable INTEGER NOT NULL DEFAULT 1")
        database.execSQL("ALTER TABLE widget_metadata ADD COLUMN isFullWidth INTEGER NOT NULL DEFAULT 0")
    },

    createMigration(2, 3) { database ->
        // Drop isFullWidth column by recreating the table
        database.execSQL("CREATE TABLE widget_metadata_new (widgetId TEXT PRIMARY KEY NOT NULL, position INTEGER NOT NULL, isVisible INTEGER NOT NULL, isEditable INTEGER NOT NULL DEFAULT 1)")
        database.execSQL("INSERT INTO widget_metadata_new (widgetId, position, isVisible, isEditable) SELECT widgetId, position, isVisible, isEditable FROM widget_metadata")
        database.execSQL("DROP TABLE widget_metadata")
        database.execSQL("ALTER TABLE widget_metadata_new RENAME TO widget_metadata")
    }
)

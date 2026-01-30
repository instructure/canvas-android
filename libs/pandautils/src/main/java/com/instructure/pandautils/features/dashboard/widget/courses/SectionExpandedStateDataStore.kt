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

package com.instructure.pandautils.features.dashboard.widget.courses

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.coursesWidgetDataStore by preferencesDataStore(name = SectionExpandedStateDataStore.STORE_NAME)

class SectionExpandedStateDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore: DataStore<Preferences> = context.coursesWidgetDataStore
    fun observeCoursesExpanded(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[COURSES_EXPANDED_KEY] ?: true
        }
    }

    fun observeGroupsExpanded(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[GROUPS_EXPANDED_KEY] ?: true
        }
    }

    suspend fun setCoursesExpanded(expanded: Boolean) {
        dataStore.edit { preferences ->
            preferences[COURSES_EXPANDED_KEY] = expanded
        }
    }

    suspend fun setGroupsExpanded(expanded: Boolean) {
        dataStore.edit { preferences ->
            preferences[GROUPS_EXPANDED_KEY] = expanded
        }
    }

    companion object {
        const val STORE_NAME = "courses_widget_store"
        private val COURSES_EXPANDED_KEY = booleanPreferencesKey("courses_expanded")
        private val GROUPS_EXPANDED_KEY = booleanPreferencesKey("groups_expanded")
    }
}
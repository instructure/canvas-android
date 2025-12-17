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

package com.instructure.student.features.dashboard.widget.forecast

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.forecastWidgetDataStore by preferencesDataStore(name = ForecastWidgetDataStore.STORE_NAME)

class ForecastWidgetDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore: DataStore<Preferences> = context.forecastWidgetDataStore

    fun observeSelectedSection(): Flow<ForecastSection?> {
        return dataStore.data.map { preferences ->
            val sectionName = preferences[SELECTED_SECTION_KEY]
            sectionName?.let {
                try {
                    ForecastSection.valueOf(it)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
        }
    }

    suspend fun setSelectedSection(section: ForecastSection?) {
        dataStore.edit { preferences ->
            if (section == null) {
                preferences.remove(SELECTED_SECTION_KEY)
            } else {
                preferences[SELECTED_SECTION_KEY] = section.name
            }
        }
    }

    companion object {
        const val STORE_NAME = "forecast_widget_store"
        private val SELECTED_SECTION_KEY = stringPreferencesKey("selected_section")
    }
}
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

package com.instructure.student.features.dashboard.widget.courses

import android.content.Context
import android.content.SharedPreferences
import com.instructure.pandautils.domain.usecase.BaseFlowUseCase
import com.instructure.student.util.StudentPrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class ObserveColorOverlayUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) : BaseFlowUseCase<Unit, Boolean>() {

    override fun execute(params: Unit): Flow<Boolean> = callbackFlow {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_HIDE_COURSE_COLOR_OVERLAY) {
                trySend(!StudentPrefs.hideCourseColorOverlay)
            }
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        send(!StudentPrefs.hideCourseColorOverlay)

        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    companion object {
        private const val PREFS_NAME = "candroidSP"
        private const val KEY_HIDE_COURSE_COLOR_OVERLAY = "hideCourseColorOverlay"
    }
}
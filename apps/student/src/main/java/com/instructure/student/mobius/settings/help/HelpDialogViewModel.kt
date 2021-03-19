/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.mobius.settings.help

import android.util.Log
import androidx.lifecycle.ViewModel
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.HelpLinksManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HelpDialogViewModel @Inject constructor(
    private val helpLinksManager: HelpLinksManager,
    private val courseManager: CourseManager) : ViewModel() {

    fun doSomething() {
        Log.d("asdasd", "helpLinksManager: ${helpLinksManager}, courseManager: ${courseManager}")
    }
}
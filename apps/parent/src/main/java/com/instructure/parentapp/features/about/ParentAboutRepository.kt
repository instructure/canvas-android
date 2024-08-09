/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.features.about

import android.content.Context
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.about.AboutRepository
import com.instructure.parentapp.BuildConfig

class ParentAboutRepository(context: Context, apiPrefs: ApiPrefs) :
    AboutRepository(context, apiPrefs) {

    override val appVersion: String = BuildConfig.VERSION_NAME
}
/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.emeritus.student.util

import com.instructure.canvasapi2.utils.BooleanResetPref
import com.instructure.canvasapi2.utils.PrefManager

/**
 * Usage: Add boolean flags here to force a refresh of api.
 * Use the special [BooleanResetPref] type so the default value is reset every automagically.
 */
object CacheControlFlags : PrefManager("candroidSP") {

    var forceRefreshBookmarks by BooleanResetPref()

}

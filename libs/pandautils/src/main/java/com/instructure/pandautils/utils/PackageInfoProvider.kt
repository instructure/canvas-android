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
package com.instructure.pandautils.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.instructure.canvasapi2.utils.Logger

class PackageInfoProvider(private val context: Context) {

    fun getPackageInfo(): PackageInfo? {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            Logger.d(e.message)
            null
        }
    }
}
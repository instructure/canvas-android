/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.utils

import android.content.Context
import android.content.Context.ACCESSIBILITY_SERVICE
import android.view.accessibility.AccessibilityManager
import com.instructure.teacher.R

val Context.isTablet get() = this.resources.getBoolean(R.bool.isDeviceTablet)

val Context.isTalkbackEnabled get() = {
    (getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager).isEnabled
}


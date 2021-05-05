/*
 * Copyright (C) 2021 - present Instructure, Inc.
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

package com.instructure.student.typeface

import com.instructure.pandautils.typeface.MEDIUM_FONT_KEY
import com.instructure.pandautils.typeface.REGULAR_FONT_KEY
import com.instructure.pandautils.typeface.TypefaceBehavior

class DefaultTypefaceBehavior : TypefaceBehavior() {
    override val typefaceMap: Map<String, String>
        get() = mapOf(
                REGULAR_FONT_KEY to "fonts/roboto_regular.ttf",
                MEDIUM_FONT_KEY to "fonts/roboto_medium.ttf"
        )
}
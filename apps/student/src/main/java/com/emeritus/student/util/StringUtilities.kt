/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

object StringUtilities {

    /**
     * The fromHTML method can cause a character that looks like "obj" to show up. This is undesired behavior most of the time.
     * This replaces "obj" with an empty space. "obj" is char 65532 and an empty space is char 32
     */
    fun simplifyHTML(sequence: CharSequence?): String {
        return sequence?.toString()?.replace(65532.toChar(), 32.toChar())?.trim().orEmpty()
    }
}

/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.canvasapi2.utils;

import com.instructure.canvasapi2.models.CanvasComparable;

/**
 * A set of utilities necessarily written in Java to provide "unsafe" behavior otherwise not allowed in Kotlin
 */
public class UnsafeJavaUtils {

    /** Forces a comparison between two CanvasComparable objects using raw types and unchecked calls */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <MDL extends CanvasComparable> int canvasCompare(MDL o1, MDL o2) {
        return o1.compareTo(o2);
    }

}

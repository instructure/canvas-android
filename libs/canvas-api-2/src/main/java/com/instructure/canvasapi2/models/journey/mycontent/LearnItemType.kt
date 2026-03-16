/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.canvasapi2.models.journey.mycontent

import com.instructure.journey.type.LearnItemType as ApolloLearnItemType

enum class LearnItemType {
    PROGRAM,
    COURSE;

    fun toApolloType(): ApolloLearnItemType {
        return when (this) {
            PROGRAM -> ApolloLearnItemType.PROGRAM
            COURSE -> ApolloLearnItemType.COURSE
        }
    }
}
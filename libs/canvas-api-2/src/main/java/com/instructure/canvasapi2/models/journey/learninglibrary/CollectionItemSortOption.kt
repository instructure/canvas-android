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
package com.instructure.canvasapi2.models.journey.learninglibrary

import com.instructure.journey.type.CollectionItemSortOption as ApolloCollectionItemSortOption
import com.instructure.journey.type.LearnItemSortOption as ApolloLearnItemSortOption

enum class CollectionItemSortOption {
    MOST_RECENT,
    LEAST_RECENT,
    NAME_A_Z,
    NAME_Z_A;

    fun toApolloModel(): ApolloCollectionItemSortOption {
        return when (this) {
            MOST_RECENT -> ApolloCollectionItemSortOption.MOST_RECENT
            LEAST_RECENT -> ApolloCollectionItemSortOption.LEAST_RECENT
            NAME_A_Z -> ApolloCollectionItemSortOption.NAME_A_Z
            NAME_Z_A -> ApolloCollectionItemSortOption.NAME_Z_A
        }
    }
}

fun ApolloCollectionItemSortOption.toModel(): CollectionItemSortOption? {
    return when (this) {
        ApolloCollectionItemSortOption.MOST_RECENT -> CollectionItemSortOption.MOST_RECENT
        ApolloCollectionItemSortOption.LEAST_RECENT -> CollectionItemSortOption.LEAST_RECENT
        ApolloCollectionItemSortOption.NAME_A_Z -> CollectionItemSortOption.NAME_A_Z
        ApolloCollectionItemSortOption.NAME_Z_A -> CollectionItemSortOption.NAME_Z_A
        else -> null
    }
}

fun CollectionItemSortOption.toLearnItemSortOption(): ApolloLearnItemSortOption {
    return when (this) {
        CollectionItemSortOption.MOST_RECENT -> ApolloLearnItemSortOption.MOST_RECENT
        CollectionItemSortOption.LEAST_RECENT -> ApolloLearnItemSortOption.LEAST_RECENT
        CollectionItemSortOption.NAME_A_Z -> ApolloLearnItemSortOption.NAME_A_Z
        CollectionItemSortOption.NAME_Z_A -> ApolloLearnItemSortOption.NAME_Z_A
    }
}
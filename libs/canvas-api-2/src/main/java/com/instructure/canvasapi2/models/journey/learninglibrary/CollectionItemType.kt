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

import com.instructure.journey.type.CollectionItemType as ApolloCollectionItemType

enum class CollectionItemType {
    COURSE,
    PAGE,
    ASSIGNMENT,
    QUIZ,
    EXTERNAL_URL,
    EXTERNAL_TOOL,
    FILE,
    PROGRAM
}

fun ApolloCollectionItemType.toModel(): CollectionItemType {
    return when (this) {
        ApolloCollectionItemType.COURSE -> CollectionItemType.COURSE
        ApolloCollectionItemType.PAGE -> CollectionItemType.PAGE
        ApolloCollectionItemType.ASSIGNMENT -> CollectionItemType.ASSIGNMENT
        ApolloCollectionItemType.QUIZ -> CollectionItemType.QUIZ
        ApolloCollectionItemType.EXTERNAL_URL -> CollectionItemType.EXTERNAL_URL
        ApolloCollectionItemType.EXTERNAL_TOOL -> CollectionItemType.EXTERNAL_TOOL
        ApolloCollectionItemType.FILE -> CollectionItemType.FILE
        ApolloCollectionItemType.PROGRAM -> CollectionItemType.PROGRAM
        ApolloCollectionItemType.UNKNOWN__ -> throw IllegalArgumentException("Unknown CollectionItemType: $this")
    }
}

fun CollectionItemType.toApolloType(): ApolloCollectionItemType {
    return when (this) {
        CollectionItemType.COURSE -> ApolloCollectionItemType.COURSE
        CollectionItemType.PAGE -> ApolloCollectionItemType.PAGE
        CollectionItemType.ASSIGNMENT -> ApolloCollectionItemType.ASSIGNMENT
        CollectionItemType.QUIZ -> ApolloCollectionItemType.QUIZ
        CollectionItemType.EXTERNAL_URL -> ApolloCollectionItemType.EXTERNAL_URL
        CollectionItemType.EXTERNAL_TOOL -> ApolloCollectionItemType.EXTERNAL_TOOL
        CollectionItemType.FILE -> ApolloCollectionItemType.FILE
        CollectionItemType.PROGRAM -> ApolloCollectionItemType.PROGRAM
    }
}
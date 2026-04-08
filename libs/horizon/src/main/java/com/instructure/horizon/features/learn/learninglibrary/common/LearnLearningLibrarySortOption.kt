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
package com.instructure.horizon.features.learn.learninglibrary.common

import androidx.annotation.StringRes
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemSortOption
import com.instructure.horizon.R

enum class LearnLearningLibrarySortOption(@StringRes val labelRes: Int) {
    MostRecent(R.string.learnLearningLibrarySortMostRecentLabel),
    LeastRecent(R.string.learnLearningLibrarySortLeastRecentLabel),
    NameAscending(R.string.learnLearningLibrarySortNameAZLabel),
    NameDescending(R.string.learnLearningLibrarySortNameZALabel);

    fun toCollectionItemSortOption(): CollectionItemSortOption = when (this) {
        MostRecent -> CollectionItemSortOption.MOST_RECENT
        LeastRecent -> CollectionItemSortOption.LEAST_RECENT
        NameAscending -> CollectionItemSortOption.NAME_A_Z
        NameDescending -> CollectionItemSortOption.NAME_Z_A
    }
}
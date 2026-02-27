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
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.horizon.R

enum class LearnLearningLibraryTypeFilter(@StringRes val labelRes: Int) {
    All(R.string.LearnLearningLibraryTypeFilterAllLabel),
    Assessments(R.string.LearnLearningLibraryTypeFilterAssessmentsLabel),
    Assignments(R.string.LearnLearningLibraryTypeFilterAssignmentsLabel),
    ExternalLinks(R.string.LearnLearningLibraryTypeFilterExternalLinksLabel),
    ExternalTools(R.string.LearnLearningLibraryTypeFilterExternalToolsLabel),
    Files(R.string.LearnLearningLibraryTypeFilterFilesLabel),
    Pages(R.string.LearnLearningLibraryTypeFilterPagesLabel)
    ;

    fun toCollectionItemType(): CollectionItemType? {
        return when(this) {
            All -> null
            Assignments -> CollectionItemType.ASSIGNMENT
            ExternalLinks -> CollectionItemType.EXTERNAL_URL
            ExternalTools -> CollectionItemType.EXTERNAL_TOOL
            Files -> CollectionItemType.FILE
            Pages -> CollectionItemType.PAGE
            else -> null
        }
    }
}
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

import android.content.res.Resources
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.pandautils.utils.orDefault

fun List<EnrolledLearningLibraryCollection>.toUiState(resources: Resources): List<LearnLearningLibraryCollectionState> {
    return this.map {
        LearnLearningLibraryCollectionState(
            id = it.id,
            name = it.name,
            itemCount = it.items.size,
            items = it.items.map { item ->
                item.toUiState(resources)
            }
        )
    }
}

fun LearningLibraryCollectionItem.toUiState(resources: Resources): LearnLearningLibraryCollectionItemState {
    return LearnLearningLibraryCollectionItemState(
        id = this.id,
        courseId = this.canvasCourse?.courseId?.toLongOrNull() ?: -1L,
        imageUrl = this.canvasCourse?.courseImageUrl,
        name = this.canvasCourse?.courseName.orEmpty(),
        isBookmarked = this.isBookmarked,
        canEnroll = this.itemType == CollectionItemType.COURSE && !this.isEnrolledInCanvas.orDefault(true),
        bookmarkLoading = false,
        enrollLoading = false,
        isCompleted = this.completionPercentage == 100.0,
        type = this.itemType,
        chips = listOf(
            this.itemType.toUiChipState(resources),
            this.completionPercentage?.toProgressUiChipState(resources),
            this.toUnitsUiChipState(resources),
            this.toEstimatedDurationUiChipState(resources)
        ).mapNotNull { it }
    )
}

fun CollectionItemType.toUiChipState(resources: Resources): LearnLearningLibraryCollectionItemChipState? {
    return when(this) {
        CollectionItemType.PAGE -> LearnLearningLibraryCollectionItemChipState(
            label = resources.getString(R.string.learnLearningLibraryPageLabel),
            color = StatusChipColor.Sky,
            iconRes = R.drawable.text_snippet
        )
        CollectionItemType.FILE -> LearnLearningLibraryCollectionItemChipState(
            label = resources.getString(R.string.learnLearningLibraryFileLabel),
            color = StatusChipColor.Sky,
            iconRes = R.drawable.attach_file
        )
        CollectionItemType.EXTERNAL_URL -> LearnLearningLibraryCollectionItemChipState(
            label = resources.getString(R.string.learnLearningLibraryExternalLinkLabel),
            color = StatusChipColor.Orange,
            iconRes = R.drawable.text_snippet
        )
        CollectionItemType.EXTERNAL_TOOL -> LearnLearningLibraryCollectionItemChipState(
            label = resources.getString(R.string.learnLearningLibraryExternalToolLabel),
            color = StatusChipColor.Honey,
            iconRes = R.drawable.note_alt
        )
        CollectionItemType.COURSE -> LearnLearningLibraryCollectionItemChipState(
            label = resources.getString(R.string.learnLearningLibraryCourseLabel),
            color = StatusChipColor.Institution,
            iconRes = R.drawable.book_2
        )
        CollectionItemType.PROGRAM -> LearnLearningLibraryCollectionItemChipState(
            label = resources.getString(R.string.learnLearningLibraryProgramLabel),
            color = StatusChipColor.Violet,
            iconRes = R.drawable.book_5,
        )
        else -> null
    }
}

fun Double.toProgressUiChipState(resources: Resources): LearnLearningLibraryCollectionItemChipState? {
    return if (this > 0 && this < 100) {
        LearnLearningLibraryCollectionItemChipState(
            label = resources.getString(R.string.learnLearningLibraryInProgressLabel),
            color = StatusChipColor.Grey,
            iconRes = R.drawable.trending_up
        )
    } else {
        null
    }
}

fun LearningLibraryCollectionItem.toEstimatedDurationUiChipState(resources: Resources): LearnLearningLibraryCollectionItemChipState? {
    val estimatedMinutes = this.canvasCourse?.estimatedDurationMinutes
    return if (estimatedMinutes != null) {
        LearnLearningLibraryCollectionItemChipState(
            label = resources.getQuantityString(R.plurals.learnLearningLibraryEstimatedDuration, estimatedMinutes.toInt(), estimatedMinutes.toInt()),
            color = StatusChipColor.Grey,
            iconRes = R.drawable.schedule
        )
    } else {
        null
    }
}

fun LearningLibraryCollectionItem.toUnitsUiChipState(resources: Resources): LearnLearningLibraryCollectionItemChipState? {
    return if (this.itemType == CollectionItemType.COURSE && this.canvasCourse != null && this.canvasCourse?.moduleItemCount.orDefault() > 0) {
        LearnLearningLibraryCollectionItemChipState(
            label = resources.getQuantityString(R.plurals.learnLearningLibraryContainedUnits, this.canvasCourse?.moduleItemCount.orDefault().toInt(), this.canvasCourse?.moduleItemCount.orDefault().toInt()),
            color = StatusChipColor.Grey,
            iconRes = R.drawable.courses_format_list_bulleted
        )
    } else {
        null
    }
}

fun CollectionItemType.iconRes(): Int? {
    return when(this) {
        CollectionItemType.PAGE -> R.drawable.text_snippet
        CollectionItemType.FILE -> R.drawable.attach_file
        CollectionItemType.EXTERNAL_URL -> R.drawable.text_snippet
        CollectionItemType.EXTERNAL_TOOL -> R.drawable.note_alt
        CollectionItemType.COURSE -> R.drawable.book_2
        CollectionItemType.PROGRAM -> R.drawable.book_5
        else -> null
    }
}

fun CollectionItemType.statusColor(): StatusChipColor? {
    return when(this) {
        CollectionItemType.PAGE -> StatusChipColor.Sky
        CollectionItemType.FILE -> StatusChipColor.Sky
        CollectionItemType.EXTERNAL_URL -> StatusChipColor.Orange
        CollectionItemType.EXTERNAL_TOOL -> StatusChipColor.Honey
        CollectionItemType.COURSE -> StatusChipColor.Institution
        CollectionItemType.PROGRAM -> StatusChipColor.Violet
        else -> null
    }
}
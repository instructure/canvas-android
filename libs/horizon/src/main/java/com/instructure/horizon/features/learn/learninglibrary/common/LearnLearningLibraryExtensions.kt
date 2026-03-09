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
import com.instructure.horizon.features.learn.navigation.LearnRoute
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.navigation.MainNavigationRoute
import com.instructure.pandautils.utils.orDefault

fun List<EnrolledLearningLibraryCollection>.toUiState(resources: Resources): List<LearnLearningLibraryCollectionState> {
    return this.map {
        LearnLearningLibraryCollectionState(
            id = it.id,
            name = it.name,
            itemCount = it.totalItemCount,
            items = it.items.map { item ->
                item.toUiState(resources)
            }
        )
    }
}

fun LearningLibraryCollectionItem.toUiState(resources: Resources): LearnLearningLibraryCollectionItemState {
    val canEnroll = (this.itemType == CollectionItemType.COURSE || this.itemType == CollectionItemType.PROGRAM) && !this.isEnrolledInCanvas.orDefault(true)

    return LearnLearningLibraryCollectionItemState(
        id = this.id,
        imageUrl = this.canvasCourse?.courseImageUrl,
        name = this.canvasCourse?.courseName.orEmpty(),
        isBookmarked = this.isBookmarked,
        canEnroll = canEnroll,
        bookmarkLoading = false,
        type = this.itemType,
        route = this.getRoute(),
        chips = listOf(
            this.itemType.toUiChipState(resources),
            this.toEstimatedDurationUiChipState(resources),
            this.toUnitsUiChipState(resources),
            this.toProgressUiChipState(resources),
        ).mapNotNull { it }
    )
}

private fun LearningLibraryCollectionItem.getRoute(): LearningLibraryRoute? {
    return when(this.itemType) {
        CollectionItemType.COURSE -> this.canvasCourse?.courseId?.toLongOrNull()?.let{
            LearningLibraryRoute.StringRoute(LearnRoute.LearnCourseDetailsScreen.route(it))
        }
        CollectionItemType.PROGRAM -> this.programId?.let {
            LearningLibraryRoute.StringRoute(LearnRoute.LearnProgramDetailsScreen.route(it))
        }
        else -> LearningLibraryRoute.ObjectRoute(MainNavigationRoute.ModuleItemSequence(
            courseId = this.canvasCourse?.courseId?.toLongOrNull() ?: -1L,
            moduleItemId = this.moduleInfo?.moduleItemId?.toLongOrNull(),
            moduleItemAssetType = this.moduleInfo?.moduleItemType,
            moduleItemAssetId = this.moduleInfo?.resourceId,
            showMyProgressButton = this.canvasCourse?.moduleItemCount != 1.0
        ))
    }
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
        CollectionItemType.QUIZ -> LearnLearningLibraryCollectionItemChipState(
            label = resources.getString(R.string.learnLearningLibraryAssessmentLabel),
            color = StatusChipColor.Green,
            iconRes = R.drawable.fast_check,
        )
        CollectionItemType.ASSIGNMENT -> LearnLearningLibraryCollectionItemChipState(
            label = resources.getString(R.string.learnLearningLibraryAssignmentLabel),
            color = StatusChipColor.Green,
            iconRes = R.drawable.fast_check,
        )
    }
}

fun LearningLibraryCollectionItem.toProgressUiChipState(resources: Resources): LearnLearningLibraryCollectionItemChipState? {
    val completionPercentage = this.completionPercentage
    return if (completionPercentage == null) {
        null
    } else if (this.isEnrolledInCanvas.orDefault() && completionPercentage >= 0 && completionPercentage < 100.0) {
        LearnLearningLibraryCollectionItemChipState(
            label = resources.getString(R.string.learnLearningLibraryInProgressLabel),
            color = StatusChipColor.Grey,
            iconRes = R.drawable.trending_up
        )
    } else if (completionPercentage == 100.0) {
        LearnLearningLibraryCollectionItemChipState(
            label = resources.getString(R.string.learnLearningLibraryCompletedLabel),
            color = StatusChipColor.Green,
            iconRes = R.drawable.check_circle
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
            label = resources.getQuantityString(R.plurals.learnLearningLibraryContainedUnits, this.canvasCourse?.moduleCount.orDefault().toInt(), this.canvasCourse?.moduleCount.orDefault().toInt()),
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
/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.horizon.features.learn

import com.instructure.canvasapi2.managers.CourseWithModuleItemDurations
import com.instructure.canvasapi2.managers.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.Program
import com.instructure.horizon.horizonui.platform.LoadingState

data class LearnUiState(
    val screenState: LoadingState = LoadingState(),
    val learningItems: List<LearningItem> = emptyList(),
    val selectedLearningItem: LearningItem? = null,
    val onSelectedLearningItemChanged: ((LearningItem) -> Unit) = {},
    val onCourseSelected: (Long) -> Unit = {},
)

sealed class LearningItem(val clickable: Boolean = true, val closeOnClick: Boolean = true, open val parentItem: LearningItem? = null) {
    data class CourseItem(val courseWithProgress: CourseWithProgress, override val parentItem: LearningItem? = null) : LearningItem() {
        override val title: String = courseWithProgress.courseName
    }

    data class ProgramGroupItem(val programName: String, val items: List<LearningItem>) : LearningItem(closeOnClick = false) {
        override val title: String = programName
    }

    data class ProgramDetails(
        val program: Program,
        val courses: List<CourseWithModuleItemDurations>,
        override val titleInDropdown: String
    ) :
        LearningItem() {
        override val title: String = program.name
    }

    data class LockedCourseItem(val courseName: String) : LearningItem(clickable = false) {
        override val title: String = courseName
    }

    data class BackToAllItems(override val title: String) : LearningItem(closeOnClick = false)

    data class ProgramHeaderItem(val programName: String) : LearningItem(clickable = false) {
        override val title: String = programName
    }

    abstract val title: String
    open val titleInDropdown: String
        get() {
            return title
        }
}
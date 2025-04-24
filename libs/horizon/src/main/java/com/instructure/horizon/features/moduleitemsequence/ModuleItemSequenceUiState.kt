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
package com.instructure.horizon.features.moduleitemsequence

import android.net.Uri
import com.instructure.horizon.features.moduleitemsequence.progress.ProgressScreenUiState
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.pandautils.utils.Const

data class ModuleItemSequenceUiState(
    val loadingState: LoadingState = LoadingState(),
    val items: List<ModuleItemUiState> = emptyList(),
    val currentPosition: Int = -1,
    val currentItem: ModuleItemUiState? = null,
    val progressScreenState: ProgressScreenUiState = ProgressScreenUiState(),
    val onPreviousClick: () -> Unit = {},
    val onNextClick: () -> Unit = {},
    val onProgressClick: () -> Unit = {},
)

data class ModuleItemUiState(
    val moduleName: String,
    val moduleItemName: String,
    val moduleItemId: Long,
    val detailTags: List<String> = emptyList(),
    val pillText: String? = null,
    val moduleItemContent: ModuleItemContent? = null,
    val markAsDoneUiState: MarkAsDoneUiState? = null,
    val isLoading: Boolean = false,
)

data class MarkAsDoneUiState(
    val isDone: Boolean = false,
    val isLoading: Boolean = false,
    val onMarkAsDoneClick: () -> Unit = {},
    val onMarkAsNotDoneClick: () -> Unit = {},
)

sealed class ModuleItemContent(val routeWithArgs: String) {
    data class Assignment(val courseId: Long, val assignmentId: Long) : ModuleItemContent(
        "courses/$courseId/assignments/$assignmentId"
    ) {
        companion object {
            const val ASSIGNMENT_ID = "assignmentId"
            const val ROUTE = "courses/{${Const.COURSE_ID}}/assignments/{${ASSIGNMENT_ID}}"
        }
    }

    data class Page(val courseId: Long, val pageUrl: String) :
        ModuleItemContent(
            "courses/$courseId/pages/$pageUrl"
        ) {
            companion object {
                const val PAGE_URL = "pageUrl"
                const val ROUTE = "courses/{${Const.COURSE_ID}}/pages/{${PAGE_URL}}"
            }
        }

    data class ExternalLink(val title: String, val url: String) :
        ModuleItemContent("external_link/${Uri.encode(title)}/${Uri.encode(url)}") {
        companion object {
            const val TITLE = "title"
            const val URL = "url"
            const val ROUTE = "external_link/{${TITLE}}/{${URL}}"
        }
    }

    data class File(val url: String) :
        ModuleItemContent(
            "courses/{${Const.COURSE_ID}}/files/{${FILE_ID}}"
        ) {
        companion object {
            const val FILE_ID = "fileId"
            const val ROUTE = "courses/{${Const.COURSE_ID}}/files/{${FILE_ID}}"
        }
    } // TODO File learning object ticket

    data class ExternalTool(val courseId: Long, val url: String, val externalUrl: String) :
        ModuleItemContent(
            "courses/$courseId/external_tool/${Uri.encode(url)}/external_url/${Uri.encode(externalUrl)}"
        ) {
        companion object {
            const val URL = "url"
            const val EXTERNAL_URL = "externalUrl"
            const val ROUTE = "courses/{${Const.COURSE_ID}}/external_tool/{$URL}/external_url/{$EXTERNAL_URL}"
        }
    }

    data class Assessment(val courseId: Long, val quizId: Long) : ModuleItemContent(
        "courses/$courseId/quizzes/$quizId"
    ) {
        companion object {
            const val QUIZ_ID = "quizId"
            const val ROUTE = "courses/{${Const.COURSE_ID}}/quizzes/{$QUIZ_ID}"
        }
    }

    data class Locked(val lockExplanation: String) :
        ModuleItemContent("locked/${Uri.encode(lockExplanation)}") {
        companion object {
            const val LOCK_EXPLANATION = "lock_explanation"
            const val ROUTE = "locked/{$LOCK_EXPLANATION}"
        }
    }
}
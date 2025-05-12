/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 *
 */

package com.instructure.canvasapi2.models

import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModuleItem(
    override val id: Long = 0,
    @SerializedName("module_id")
    val moduleId: Long = 0,
    var position: Int = 0,
    val title: String? = null,
    val indent: Int = 0,
    val type: String? = null,
    @SerializedName("html_url")
    val htmlUrl: String? = null,
    val url: String? = null,
    @SerializedName("completion_requirement")
    val completionRequirement: ModuleCompletionRequirement? = null,
    @SerializedName("content_details")
    val moduleDetails: ModuleContentDetails? = null,
    val published: Boolean? = null,
    @SerializedName("content_id")
    val contentId: Long = 0,
    @SerializedName("external_url")
    val externalUrl: String? = null,
    @SerializedName("page_url")
    val pageUrl: String? = null,
    val unpublishable: Boolean = true,
    @SerializedName("mastery_paths")
    var masteryPaths: MasteryPath? = null,
    @SerializedName("quiz_lti")
    var quizLti: Boolean = false,
    // When we display the "Choose Assignment Group" when an assignment uses Mastery Paths we create a new row to display.
    // We still need the module item id to select the assignment group that we want, but if we use the same id as the root
    // module item both items wouldn't display (because they would have the same id at that point).
    var masteryPathsItemId: Long = 0 // Helper variable
) : CanvasModel<ModuleItem>() {
    override val comparisonString get() = title

    enum class Type {
        Assignment, Discussion, File, Page, SubHeader, Quiz, ExternalUrl, ExternalTool, Locked, ChooseAssignmentGroup
    }

    companion object {
        const val MUST_MARK_DONE = "must_mark_done"
        const val MUST_VIEW = "must_view"
    }
}


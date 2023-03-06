/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.student.features.bookmarks

import android.view.View
import androidx.annotation.ColorInt
import com.instructure.canvasapi2.models.Bookmark
import com.instructure.pandautils.mvvm.ItemViewModel

data class BookmarksViewData(
    val items: List<ItemViewModel>
)

data class BookmarkViewData(
    val id: Long,
    val name: String?,
    val url: String?,
    @ColorInt val iconColor: Int
)

data class BookmarkGroupViewData(
    val name: String
)

sealed class BookmarksAction {
    data class OpenPopup(val view: View, val id: Long) : BookmarksAction()
    data class CreateShortcut(val bookmark: Bookmark) : BookmarksAction()
    data class ShowSnackbar(val snackbar: String) : BookmarksAction()
    data class ShowDeleteConfirmation(val bookmark: Bookmark) : BookmarksAction()
    data class ShowEditDialog(val bookmark: Bookmark) : BookmarksAction()
}

enum class BookmarkGrouping {
    CONTEXT,
    LABELS
}

enum class BookmarkViewType(val type: Int) {
    HEADER(1),
    ITEM(2)
}
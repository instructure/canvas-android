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

package com.instructure.student.features.bookmarks.itemviewmodels

import com.instructure.pandautils.binding.GroupItemViewModel
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.student.R
import com.instructure.student.features.bookmarks.BookmarkGroupViewData
import com.instructure.student.features.bookmarks.BookmarkViewType

class BookmarkGroupItemViewModel(
    val data: BookmarkGroupViewData,
    items: List<ItemViewModel>) :
    GroupItemViewModel(true, false, items) {
    override val layoutId: Int = R.layout.item_bookmark_group

    override val viewType: Int
        get() = BookmarkViewType.HEADER.type
}
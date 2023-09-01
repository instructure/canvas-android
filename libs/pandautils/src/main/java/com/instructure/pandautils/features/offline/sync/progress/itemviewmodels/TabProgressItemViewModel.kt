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
 *
 */

package com.instructure.pandautils.features.offline.sync.progress.itemviewmodels

import com.instructure.pandautils.R
import com.instructure.pandautils.features.offline.sync.progress.TabProgressViewData
import com.instructure.pandautils.features.offline.sync.progress.ViewType
import com.instructure.pandautils.mvvm.ItemViewModel

data class TabProgressItemViewModel(val data: TabProgressViewData) : ItemViewModel {
    override val layoutId = R.layout.item_tab_progress

    override val viewType = ViewType.COURSE_TAB_PROGRESS.viewType

    override fun areItemsTheSame(other: ItemViewModel): Boolean {
        return other is TabProgressItemViewModel && data.tabName == other.data.tabName
    }

    override fun areContentsTheSame(other: ItemViewModel): Boolean {
        return other is TabProgressItemViewModel
                && other.data == this.data
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TabProgressItemViewModel

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }


}
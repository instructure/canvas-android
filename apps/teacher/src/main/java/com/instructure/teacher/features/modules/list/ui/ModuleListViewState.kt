/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.teacher.features.modules.list.ui

import androidx.annotation.ColorInt
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.canvasapi2.models.ModuleItem

data class ModuleListViewState(
    val showRefreshing: Boolean = false,
    val items: List<ModuleListItemData> = emptyList(),
    val collapsedModuleIds: Set<Long> = emptySet()
)

sealed class ModuleListItemData {

    object Empty : ModuleListItemData()

    object Loading : ModuleListItemData()

    data class EmptyItem(val moduleId: Long) : ModuleListItemData()

    data class FullError(val buttonColor: Int): ModuleListItemData()

    data class InlineError(val buttonColor: Int): ModuleListItemData()

    data class SubHeader(
        val id: Long,
        val title: String?,
        val indent: Int,
        val enabled: Boolean,
        val published: Boolean?,
        val isLoading: Boolean
    ) : ModuleListItemData()

    data class ModuleData(
        val id: Long,
        val name: String,
        val isPublished: Boolean?,
        val moduleItems: List<ModuleListItemData>,
        val isLoading: Boolean
    ): ModuleListItemData()

    data class ModuleItemData(
        /** The ID of this module item */
        val id: Long,

        /** The title. If null, the title should be hidden. */
        val title: String?,

        /** The subtitle. If null, the subtitle should be hidden. */
        val subtitle: String?,

        /** The second line of subtitle. If null, it should be hidden. */
        val subtitle2: String?,

        /** The resource ID of the icon to show for this item. If null, the icon should be hidden. */
        val iconResId: Int?,

        /** Whether this module item is published. Should not display any publish icon if null. */
        val isPublished: Boolean?,

        /** The indent in pixels */
        val indent: Int,

        /** The icon tint color, which should be the course color */
        @ColorInt
        val tintColor: Int,

        /** Whether the item is enabled (and therefore clickable). This will be false for SubHeader items. */
        val enabled: Boolean,

        /**
         * Whether additional data is being loaded for this item, either for the purpose of routing or for the purpose
         * of refreshing this item after it has been updated elsewhere in the app.
         */
        val isLoading: Boolean = false,

        val type: ModuleItem.Type,

        val contentDetails: ModuleContentDetails? = null,

        val contentId: Long? = null,

        val unpublishable: Boolean = true
    ) : ModuleListItemData()

}

/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
package com.instructure.pandautils.room.offline.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.ModuleItem

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ModuleObjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["moduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ModuleItemEntity(
    @PrimaryKey
    val id: Long = 0,
    val moduleId: Long,
    var position: Int,
    val title: String?,
    val indent: Int,
    val type: String?,
    val htmlUrl: String?,
    val url: String?,
//    val completionRequirement: CompletionRequirement?,
//    val moduleDetails: ModuleContentDetails?,
    val published: Boolean?,
    val contentId: Long,
    val externalUrl: String?,
    val pageUrl: String?,
//    var masteryPaths: MasteryPath?,
) {
    constructor(moduleItem: ModuleItem, moduleId: Long) : this(
        id = moduleItem.id,
        moduleId = moduleId,
        position = moduleItem.position,
        title = moduleItem.title,
        indent = moduleItem.indent,
        type = moduleItem.type,
        htmlUrl = moduleItem.htmlUrl,
        url = moduleItem.url,
        published = moduleItem.published,
        contentId = moduleItem.contentId,
        externalUrl = moduleItem.externalUrl,
        pageUrl = moduleItem.pageUrl)

    fun toApiModel(): ModuleItem {
        return ModuleItem(
            id = id,
            position = position,
            title = title,
            indent = indent,
            type = type,
            htmlUrl = htmlUrl,
            url = url,
            published = published,
            contentId = contentId,
            externalUrl = externalUrl,
            pageUrl = pageUrl
        )
    }
}
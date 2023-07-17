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
import com.instructure.canvasapi2.models.Quiz

@Entity(
    primaryKeys = ["contextType", "contextId", "quizId"],
    foreignKeys = [
        ForeignKey(
            entity = QuizEntity::class,
            parentColumns = ["id"],
            childColumns = ["quizId"],
            onDelete = ForeignKey.CASCADE
        ),
    ]
)
data class QuizContextEntity(
    val contextType: String,
    val contextId: Long,
    val quizId: Long,
) {
   constructor(contextType: String, contextId: Long, quiz: Quiz) : this (
       contextType = contextType,
       contextId = contextId,
       quizId = quiz.id,
    )
}
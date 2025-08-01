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
package com.instructure.horizon.horizonui.organisms.cards

import android.content.Context
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.utils.isLocked
import com.instructure.horizon.R
import com.instructure.horizon.model.LearningObjectStatus
import com.instructure.horizon.model.LearningObjectType
import com.instructure.pandautils.utils.formatIsoDuration
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.DecimalFormat
import javax.inject.Inject

class ModuleItemCardStateMapper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun mapModuleItemToCardState(moduleItem: ModuleItem, onClick: (Long) -> Unit): ModuleItemCardState? {
        val learningObjectType = when (moduleItem.type) {
            ModuleItem.Type.Assignment.name -> {
                if (moduleItem.quizLti) {
                    LearningObjectType.ASSESSMENT
                } else {
                    LearningObjectType.ASSIGNMENT
                }
            }

            ModuleItem.Type.Page.name -> LearningObjectType.PAGE
            ModuleItem.Type.ExternalUrl.name -> LearningObjectType.EXTERNAL_URL
            ModuleItem.Type.File.name -> LearningObjectType.FILE
            ModuleItem.Type.ExternalTool.name -> LearningObjectType.EXTERNAL_TOOL
            ModuleItem.Type.Quiz.name -> LearningObjectType.ASSESSMENT
            else -> LearningObjectType.UNKNOWN
        }

        // Some types are not supported in Horizon (eg. Discussion) so we return null and filter them out
        if (learningObjectType == LearningObjectType.UNKNOWN) return null

        val completionRequirement = moduleItem.completionRequirement
        val learningObjectStatus = when {
            completionRequirement == null -> LearningObjectStatus.OPTIONAL
            completionRequirement.completed -> learningObjectType.completedStatus
            else -> LearningObjectStatus.REQUIRED
        }

        val points = moduleItem.moduleDetails?.pointsPossible?.toDoubleOrNull()?.toInt()
        val pointsString = points?.let {
            if (completionRequirement?.type == ModuleItem.MIN_SCORE) {
                val minScore = DecimalFormat("#.#").format(completionRequirement.minScore)
                context.resources.getQuantityString(R.plurals.moduleItemCard_pointsPossibleWithRequirement, it, it, minScore)
            } else {
                context.resources.getQuantityString(R.plurals.moduleItemCard_pointsPossible, it, it)
            }
        }

        return ModuleItemCardState(
            title = moduleItem.title.orEmpty(),
            learningObjectType = learningObjectType,
            learningObjectStatus = learningObjectStatus,
            locked = moduleItem.isLocked(),
            selected = false,
            remainingTime = moduleItem.estimatedDuration?.formatIsoDuration(context),
            dueDate = moduleItem.moduleDetails?.dueDate,
            pastDue = moduleItem.overDue,
            points = pointsString,
            onClick = { onClick(moduleItem.id) }
        )
    }
}
/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.aiassistant.common

import com.instructure.canvasapi2.models.journey.assist.JourneyAssistState
import com.instructure.horizon.features.aiassistant.common.model.AiAssistContext
import com.instructure.horizon.features.aiassistant.common.model.AiAssistContextSource
import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiAssistContextProvider @Inject constructor() {
    var aiAssistContext = AiAssistContext()

    fun addMessageToChatHistory(message: AiAssistMessage) {
        aiAssistContext = aiAssistContext.copy(
            chatHistory = aiAssistContext.chatHistory + message
        )
    }

    fun updateContextFromState(state: JourneyAssistState?) {
        if (state == null) return

        val updatedSources = aiAssistContext.contextSources
            .filterNot { it is AiAssistContextSource.Course || it is AiAssistContextSource.Page || it is AiAssistContextSource.File }
            .toMutableList()

        state.courseID?.let { updatedSources.add(AiAssistContextSource.Course(it)) }
        state.pageID?.let { updatedSources.add(AiAssistContextSource.Page(it)) }
        state.fileID?.let { updatedSources.add(AiAssistContextSource.File(it)) }

        aiAssistContext = aiAssistContext.copy(contextSources = updatedSources)
    }
}
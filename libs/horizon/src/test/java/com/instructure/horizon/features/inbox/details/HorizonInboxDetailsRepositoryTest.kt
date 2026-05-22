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
package com.instructure.horizon.features.inbox.details

import com.instructure.canvasapi2.models.BasicUser
import com.instructure.canvasapi2.models.Conversation
import com.instructure.horizon.domain.usecase.inbox.AddMessageToConversationUseCase
import com.instructure.horizon.domain.usecase.inbox.GetConversationUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class HorizonInboxDetailsRepositoryTest {
    private val getConversationUseCase: GetConversationUseCase = mockk(relaxed = true)
    private val addMessageToConversationUseCase: AddMessageToConversationUseCase = mockk(relaxed = true)

    private val repository = HorizonInboxDetailsRepository(getConversationUseCase, addMessageToConversationUseCase)

    private val testConversation = Conversation(
        id = 1L,
        subject = "Test Conversation",
        participants = mutableListOf(BasicUser(id = 1L, name = "User 1")),
    )

    @Test
    fun `getConversation delegates to use case`() = runTest {
        coEvery { getConversationUseCase(any()) } returns testConversation

        val result = repository.getConversation(id = 1L, forceRefresh = false)

        assertEquals("Test Conversation", result.subject)
        coVerify { getConversationUseCase(GetConversationUseCase.Params(1L, true, false)) }
    }

    @Test
    fun `addMessageToConversation delegates to use case`() = runTest {
        coEvery { addMessageToConversationUseCase(any()) } returns testConversation

        val result = repository.addMessageToConversation(
            contextCode = "course_1",
            conversationId = 1L,
            recipientIds = listOf("1", "2"),
            body = "Reply",
            includedMessageIds = listOf(10L),
            attachmentIds = listOf(100L),
        )

        assertEquals("Test Conversation", result.subject)
        coVerify {
            addMessageToConversationUseCase(
                AddMessageToConversationUseCase.Params(
                    contextCode = "course_1",
                    conversationId = 1L,
                    recipientIds = listOf("1", "2"),
                    body = "Reply",
                    includedMessageIds = listOf(10L),
                    attachmentIds = listOf(100L),
                )
            )
        }
    }
}

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
import com.instructure.horizon.data.repository.InboxRepository
import com.instructure.horizon.domain.usecase.inbox.AddMessageToConversationUseCase
import com.instructure.horizon.domain.usecase.inbox.GetConversationUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * The old HorizonInboxDetailsRepository was a thin pass-through over inbox use cases.
 * It is gone; the view model depends on GetConversationUseCase / AddMessageToConversationUseCase
 * directly. These tests now exercise those use cases against InboxRepository.
 */
class HorizonInboxDetailsRepositoryTest {
    private val inboxRepository: InboxRepository = mockk(relaxed = true)
    private val getConversationUseCase = GetConversationUseCase(inboxRepository)
    private val addMessageToConversationUseCase = AddMessageToConversationUseCase(inboxRepository)

    private val testConversation = Conversation(
        id = 1L,
        subject = "Test Conversation",
        participants = mutableListOf(BasicUser(id = 1L, name = "User 1")),
    )

    @Test
    fun `getConversation delegates to InboxRepository`() = runTest {
        coEvery { inboxRepository.getConversation(any(), any(), any()) } returns testConversation

        val result = getConversationUseCase(GetConversationUseCase.Params(id = 1L, markAsRead = true, forceNetwork = false))

        assertEquals("Test Conversation", result.subject)
        coVerify { inboxRepository.getConversation(1L, true, false) }
    }

    @Test
    fun `addMessageToConversation delegates to InboxRepository`() = runTest {
        coEvery { inboxRepository.addMessageToConversation(any(), any(), any(), any(), any(), any()) } returns testConversation

        val result = addMessageToConversationUseCase(
            AddMessageToConversationUseCase.Params(
                contextCode = "course_1",
                conversationId = 1L,
                recipientIds = listOf("1", "2"),
                body = "Reply",
                includedMessageIds = listOf(10L),
                attachmentIds = listOf(100L),
            )
        )

        assertEquals("Test Conversation", result.subject)
        coVerify {
            inboxRepository.addMessageToConversation(
                contextCode = "course_1",
                conversationId = 1L,
                recipientIds = listOf("1", "2"),
                body = "Reply",
                includedMessageIds = listOf(10L),
                attachmentIds = listOf(100L),
            )
        }
    }
}

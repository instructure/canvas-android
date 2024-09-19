/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.inbox.utils

import android.content.Context
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.BasicUser
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Message
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InboxComposeOptionsTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val context: Context = mockk(relaxed = true)
    private val conversation = Conversation(
        id = 1,
        participants = mutableListOf(BasicUser(id = 1, name = "User 1"), BasicUser(id = 2, name = "User 2")),
        messages = mutableListOf(
            Message(id = 1, authorId = 1, body = "Message 1", participatingUserIds = mutableListOf(1, 2)),
            Message(id = 2, authorId = 2, body = "Message 2", participatingUserIds = mutableListOf(1, 2)),
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        ContextKeeper.appContext = context

        mockkObject(ApiPrefs)
        every { ApiPrefs.user } returns User(id = 1, name = "User 1")
        coEvery { context.getString(
            com.instructure.pandautils.R.string.inboxForwardSubjectFwdPrefix,
            conversation.subject
        ) } returns "Fwd: ${conversation.subject}"
        coEvery { context.getString(
            com.instructure.pandautils.R.string.inboxReplySubjectRePrefix,
            conversation.subject
        ) } returns "Re: ${conversation.subject}"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test Compose options init value`() {
        val inboxComposeOptions = InboxComposeOptions()

        // Check if the mode is set correctly
        assertEquals(InboxComposeOptionsMode.NEW_MESSAGE, inboxComposeOptions.mode)

        //Check if the previousMessages are set correctly
        assertEquals(null, inboxComposeOptions.previousMessages)

        // Check if the default values are set correctly
        assertEquals(null, inboxComposeOptions.defaultValues.contextCode)
        assertEquals(null, inboxComposeOptions.defaultValues.contextName)
        assertEquals(emptyList<Recipient>(), inboxComposeOptions.defaultValues.recipients)
        assertEquals(false, inboxComposeOptions.defaultValues.sendIndividual)
        assertEquals("", inboxComposeOptions.defaultValues.subject)
        assertEquals("", inboxComposeOptions.defaultValues.body)
        assertEquals(emptyList<Attachment>(), inboxComposeOptions.defaultValues.attachments)

        // Check if the disabled fields are set correctly
        assertFalse(inboxComposeOptions.disabledFields.isContextDisabled)
        assertFalse(inboxComposeOptions.disabledFields.isRecipientsDisabled)
        assertFalse(inboxComposeOptions.disabledFields.isSendIndividualDisabled)
        assertFalse(inboxComposeOptions.disabledFields.isSubjectDisabled)
        assertFalse(inboxComposeOptions.disabledFields.isBodyDisabled)
        assertFalse(inboxComposeOptions.disabledFields.isAttachmentDisabled)

        // Check if the hidden fields are set correctly
        assertFalse(inboxComposeOptions.hiddenFields.isContextHidden)
        assertFalse(inboxComposeOptions.hiddenFields.isRecipientsHidden)
        assertFalse(inboxComposeOptions.hiddenFields.isSendIndividualHidden)
        assertFalse(inboxComposeOptions.hiddenFields.isSubjectHidden)
        assertFalse(inboxComposeOptions.hiddenFields.isBodyHidden)
        assertFalse(inboxComposeOptions.hiddenFields.isAttachmentHidden)
    }

    @Test
    fun `Test Compose options build for Reply`() {
        val inboxComposeOptions = InboxComposeOptions.buildReply(context, conversation, conversation.messages.last())

        // Check if the mode is set correctly
        assertEquals(InboxComposeOptionsMode.REPLY, inboxComposeOptions.mode)

        //Check if the previousMessages are set correctly
        assertEquals(conversation, inboxComposeOptions.previousMessages?.conversation)
        assertEquals(conversation.messages, inboxComposeOptions.previousMessages?.previousMessages)

        // Check if the default values are set correctly
        assertEquals(conversation.contextCode, inboxComposeOptions.defaultValues.contextCode)
        assertEquals(conversation.contextName, inboxComposeOptions.defaultValues.contextName)
        assertEquals(listOf(conversation.participants.map { it.id.toString() }.last()), inboxComposeOptions.defaultValues.recipients.map { it.stringId })
        assertEquals(false, inboxComposeOptions.defaultValues.sendIndividual)
        assertEquals("Re: ${conversation.subject}", inboxComposeOptions.defaultValues.subject)
        assertEquals("", inboxComposeOptions.defaultValues.body)
        assertEquals(emptyList<Attachment>(), inboxComposeOptions.defaultValues.attachments)

        // Check if the disabled fields are set correctly
        assertTrue(inboxComposeOptions.disabledFields.isContextDisabled)
        assertFalse(inboxComposeOptions.disabledFields.isRecipientsDisabled)
        assertFalse(inboxComposeOptions.disabledFields.isSendIndividualDisabled)
        assertTrue(inboxComposeOptions.disabledFields.isSubjectDisabled)
        assertFalse(inboxComposeOptions.disabledFields.isBodyDisabled)
        assertFalse(inboxComposeOptions.disabledFields.isAttachmentDisabled)

        // Check if the hidden fields are set correctly
        assertFalse(inboxComposeOptions.hiddenFields.isContextHidden)
        assertFalse(inboxComposeOptions.hiddenFields.isRecipientsHidden)
        assertTrue(inboxComposeOptions.hiddenFields.isSendIndividualHidden)
        assertFalse(inboxComposeOptions.hiddenFields.isSubjectHidden)
        assertFalse(inboxComposeOptions.hiddenFields.isBodyHidden)
        assertFalse(inboxComposeOptions.hiddenFields.isAttachmentHidden)
    }

    @Test
    fun `Test Compose options build for Reply All`() {
        val inboxComposeOptions = InboxComposeOptions.buildReplyAll(context, conversation, conversation.messages.last())

        // Check if the mode is set correctly
        assertEquals(InboxComposeOptionsMode.REPLY_ALL, inboxComposeOptions.mode)

        //Check if the previousMessages are set correctly
        assertEquals(conversation, inboxComposeOptions.previousMessages?.conversation)
        assertEquals(conversation.messages, inboxComposeOptions.previousMessages?.previousMessages)

        // Check if the default values are set correctly
        assertEquals(conversation.contextCode, inboxComposeOptions.defaultValues.contextCode)
        assertEquals(conversation.contextName, inboxComposeOptions.defaultValues.contextName)
        assertEquals(listOf(conversation.participants.map { it.id.toString() }.last()), inboxComposeOptions.defaultValues.recipients.map { it.stringId })
        assertEquals(false, inboxComposeOptions.defaultValues.sendIndividual)
        assertEquals("Re: ${conversation.subject}", inboxComposeOptions.defaultValues.subject)
        assertEquals("", inboxComposeOptions.defaultValues.body)
        assertEquals(emptyList<Attachment>(), inboxComposeOptions.defaultValues.attachments)

        // Check if the disabled fields are set correctly
        assertTrue(inboxComposeOptions.disabledFields.isContextDisabled)
        assertFalse(inboxComposeOptions.disabledFields.isRecipientsDisabled)
        assertFalse(inboxComposeOptions.disabledFields.isSendIndividualDisabled)
        assertTrue(inboxComposeOptions.disabledFields.isSubjectDisabled)
        assertFalse(inboxComposeOptions.disabledFields.isBodyDisabled)
        assertFalse(inboxComposeOptions.disabledFields.isAttachmentDisabled)

        // Check if the hidden fields are set correctly
        assertFalse(inboxComposeOptions.hiddenFields.isContextHidden)
        assertFalse(inboxComposeOptions.hiddenFields.isRecipientsHidden)
        assertTrue(inboxComposeOptions.hiddenFields.isSendIndividualHidden)
        assertFalse(inboxComposeOptions.hiddenFields.isSubjectHidden)
        assertFalse(inboxComposeOptions.hiddenFields.isBodyHidden)
        assertFalse(inboxComposeOptions.hiddenFields.isAttachmentHidden)
    }

    @Test
    fun `Test Compose options build for Forward`() {
        val inboxComposeOptions = InboxComposeOptions.buildForward(context, conversation, conversation.messages.last())

        // Check if the mode is set correctly
        assertEquals(InboxComposeOptionsMode.FORWARD, inboxComposeOptions.mode)

        //Check if the previousMessages are set correctly
        assertEquals(conversation, inboxComposeOptions.previousMessages?.conversation)
        assertEquals(conversation.messages, inboxComposeOptions.previousMessages?.previousMessages)

        // Check if the default values are set correctly
        assertEquals(conversation.contextCode, inboxComposeOptions.defaultValues.contextCode)
        assertEquals(conversation.contextName, inboxComposeOptions.defaultValues.contextName)
        assertEquals(emptyList<Recipient>(), inboxComposeOptions.defaultValues.recipients.map { it.stringId })
        assertEquals(false, inboxComposeOptions.defaultValues.sendIndividual)
        assertEquals("Fwd: ${conversation.subject}", inboxComposeOptions.defaultValues.subject)
        assertEquals("", inboxComposeOptions.defaultValues.body)
        assertEquals(emptyList<Attachment>(), inboxComposeOptions.defaultValues.attachments)

        // Check if the disabled fields are set correctly
        assertTrue(inboxComposeOptions.disabledFields.isContextDisabled)
        assertFalse(inboxComposeOptions.disabledFields.isRecipientsDisabled)
        assertFalse(inboxComposeOptions.disabledFields.isSendIndividualDisabled)
        assertTrue(inboxComposeOptions.disabledFields.isSubjectDisabled)
        assertFalse(inboxComposeOptions.disabledFields.isBodyDisabled)
        assertFalse(inboxComposeOptions.disabledFields.isAttachmentDisabled)

        // Check if the hidden fields are set correctly
        assertFalse(inboxComposeOptions.hiddenFields.isContextHidden)
        assertFalse(inboxComposeOptions.hiddenFields.isRecipientsHidden)
        assertTrue(inboxComposeOptions.hiddenFields.isSendIndividualHidden)
        assertFalse(inboxComposeOptions.hiddenFields.isSubjectHidden)
        assertFalse(inboxComposeOptions.hiddenFields.isBodyHidden)
        assertFalse(inboxComposeOptions.hiddenFields.isAttachmentHidden)
    }
}
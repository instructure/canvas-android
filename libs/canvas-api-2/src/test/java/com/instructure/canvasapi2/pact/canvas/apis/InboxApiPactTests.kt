/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.canvasapi2.pact.canvas.apis

import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit.PactVerification
import au.com.dius.pact.core.model.RequestResponsePact
import au.com.dius.pact.core.model.annotations.Pact
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.pact.canvas.logic.PactConversationFieldConfig
import com.instructure.canvasapi2.pact.canvas.logic.assertConversationPopulated
import com.instructure.canvasapi2.pact.canvas.logic.populateConversationFields
import io.pactfoundation.consumer.dsl.LambdaDsl
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

class InboxApiPactTests : ApiPactTestBase() {

    // Common logic
    private fun createService(): InboxApi.InboxInterface {
        val client = getClient()
        return client.create(InboxApi.InboxInterface::class.java)
    }

    //
    // region create a conversation
    //

    // "recipients[]=9": 9 is the id of the teacher defined in the provider state
    // "context_code=course_3": 3 is the id of the course in which the teacher and student are enrolled.
    val createConversationQuery = "group_conversation=true&recipients[]=9&body=Message&subject=Subject&context_code=course_3&bulk_message=0"
    val createConversationPath = "/api/v1/conversations"
    val createConversationFieldConfig = PactConversationFieldConfig(
            includeMessages = true,
            includeContextName = false,
            includeLastMessageData = false)
    val createConversationResponseBody = LambdaDsl.newJsonArray { arr ->
        arr.`object`() { obj ->
            obj.populateConversationFields(createConversationFieldConfig)
        }
    }.build()

    @Pact(consumer = "android")
    fun createConversationPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
                // Use a provider state with no existing conversations
                .given("user enrollments existing in canvas")

                .uponReceiving("Create a conversation")
                .path(createConversationPath)
                .method("POST")
                .query(createConversationQuery)
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(201) // Appropriate for this POST request
                .body(createConversationResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Ignore("Changed the endpoint to use fields instead of query string. Currently maintaining Pact tests have low priority.")
    @Test
    @PactVerification(fragment = "createConversationPact")
    fun `create a conversation`() {
        val service = createService()

        val createConversationCall = service.createConversation(
                recipients = listOf("9"),
                message = "Message",
                subject = "Subject",
                contextCode = "course_3",
                attachmentIds = longArrayOf(),
                isBulk = 0)
        val createConversationResult = createConversationCall.execute()

        assertQueryParamsAndPath(createConversationCall, createConversationQuery, createConversationPath)

        Assert.assertNotNull("Expected non-null response body", createConversationResult.body())
        val conversations = createConversationResult.body()!!

        assertTrue("Expected exactly one conversation returned", conversations.size == 1)

        for(i in 0..conversations.size-1) {
            assertConversationPopulated("Returned conversation $i", conversations[i], createConversationFieldConfig)
        }
    }
    //endregion

    //
    // region request all of a user's conversations
    //

    val getAllConversationsQuery = "interleave_submissions=1&include[]=participant_avatars&scope="
    val getAllConversationsPath = "/api/v1/conversations"
    val getAllConversationsFieldConfig = PactConversationFieldConfig()
    val getAllConversationsResponseBody = LambdaDsl.newJsonArray { arr ->
        arr.`object`() { obj ->
            obj.populateConversationFields(getAllConversationsFieldConfig)
        }
        arr.`object`() { obj ->
            obj.populateConversationFields(getAllConversationsFieldConfig)
        }
    }.build()

    @Pact(consumer = "android")
    fun getAllConversationsPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
                .given("mobile user with conversations")

                .uponReceiving("Grab user's conversations")
                .path(getAllConversationsPath)
                .method("GET")
                .query(getAllConversationsQuery)
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(getAllConversationsResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getAllConversationsPact")
    fun `grab user's conversations`() {
        val service = createService()

        val getConversationsCall = service.getConversations(scope = "")
        val getConversationsResult = getConversationsCall.execute()

        assertQueryParamsAndPath(getConversationsCall, getAllConversationsQuery, getAllConversationsPath)

        Assert.assertNotNull("Expected non-null response body", getConversationsResult.body())
        val conversations = getConversationsResult.body()!!

        assertTrue("Expected at least one conversation returned", conversations.size > 0)

        for(i in 0..conversations.size-1) {
            assertConversationPopulated("Returned conversation $i", conversations[i], getAllConversationsFieldConfig)
        }
    }
    //endregion

    //
    // region request all of a user's conversations
    //

    val getFilteredConversationsQuery = "interleave_submissions=1&include[]=participant_avatars&scope=unread&filter="
    val getFilteredConversationsPath = "/api/v1/conversations"
    val getFilteredConversationsFieldConfig = PactConversationFieldConfig(state = "unread")
    val getFilteredConversationsResponseBody = LambdaDsl.newJsonArray { arr ->
        arr.`object`() { obj ->
            obj.populateConversationFields(getFilteredConversationsFieldConfig)
        }
    }.build()

    @Pact(consumer = "android")
    fun getFilteredConversationsPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
                .given("mobile user with conversations")

                .uponReceiving("Grab user's filtered conversations")
                .path(getFilteredConversationsPath)
                .method("GET")
                .query(getFilteredConversationsQuery)
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(getFilteredConversationsResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getFilteredConversationsPact")
    fun `grab user's filtered conversations`() {
        val service = createService()

        val getFilteredConversationsCall = service.getConversationsFiltered(scope = "unread", canvasContextFilter = "")
        val getFilteredConversationsResult = getFilteredConversationsCall.execute()

        assertQueryParamsAndPath(getFilteredConversationsCall, getFilteredConversationsQuery, getFilteredConversationsPath)

        Assert.assertNotNull("Expected non-null response body", getFilteredConversationsResult.body())
        val conversations = getFilteredConversationsResult.body()!!

        assertTrue("Expected at least one conversation returned", conversations.size > 0)

        for(i in 0..conversations.size-1) {
            assertConversationPopulated("Returned conversation $i", conversations[i], getFilteredConversationsFieldConfig)
        }
    }
    //endregion

    //
    // region request a specific conversation
    //
    // Assumes that ALL conversation objects have the messages array populated,
    // and ALL nested messages have the media_comment field and attachments array
    // populated.  Make sure that the provider_state is set up accordingly.
    //

    val getOneConversationQuery = "include[]=participant_avatars&auto_mark_as_read=true"
    val getOneConversationPath = "/api/v1/conversations/1"
    val getOneConversationFieldConfig = PactConversationFieldConfig(
            includeMessages = true,
            includeMediaComment = true,
            includeAttachments = true)
    val getOneConversationResponseBody = LambdaDsl.newJsonBody { obj ->
        obj.populateConversationFields(getOneConversationFieldConfig)
    }.build()

    @Pact(consumer = "android")
    fun getOneConversationPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
                .given("mobile user with conversations")

                .uponReceiving("Grab a specific conversation")
                .path(getOneConversationPath)
                .method("GET")
                .query(getOneConversationQuery)
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(getOneConversationResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getOneConversationPact")
    fun `grab a specific conversation`() {
        val service = createService()

        val getConversationCall = service.getConversation(1, true)
        val getConversationResult = getConversationCall.execute()

        assertQueryParamsAndPath(getConversationCall, getOneConversationQuery, getOneConversationPath)

        Assert.assertNotNull("Expected non-null response body", getConversationResult.body())
        assertConversationPopulated("Returned conversation", getConversationResult.body()!!, getOneConversationFieldConfig)
    }
    //endregion

    //
    // region update a conversation
    //

    val updateConversationQuery = "conversation[workflow_state]=unread&conversation[starred]=true"
    val updateConversationPath = "/api/v1/conversations/1"
    val updateConversationFieldConfig = PactConversationFieldConfig(includeMessages = false, state = "unread", starred = true, includeContextName = false)
    val updateConversationResponseBody = LambdaDsl.newJsonBody { obj ->
        obj.populateConversationFields(updateConversationFieldConfig)
    }.build()

    @Pact(consumer = "android")
    fun updateConversationPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
                .given("mobile user with conversations")

                .uponReceiving("update a conversation")
                .path(updateConversationPath)
                .method("PUT")
                .query(updateConversationQuery)
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(updateConversationResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "updateConversationPact")
    fun `update a conversation`() {
        val service = createService()

        val updateConversationCall = service.updateConversation(1, workflowState = "unread", isStarred = true)
        val updateConversationResult = updateConversationCall.execute()

        assertQueryParamsAndPath(updateConversationCall, updateConversationQuery, updateConversationPath)

        Assert.assertNotNull("Expected non-null response body", updateConversationResult.body())
        assertConversationPopulated("Returned conversation", updateConversationResult.body()!!, updateConversationFieldConfig)
    }
    //endregion

    //
    // region add a message to a conversation
    //

    val addMessageQuery = "group_conversation=true&recipients[]=8&recipients[]=9&body=addedMessageBody"
    val addMessagePath = "/api/v1/conversations/1/add_message"
    val addMessageFieldConfig = PactConversationFieldConfig(includeMessages = true, includeContextName = false)
    val addMessageResponseBody = LambdaDsl.newJsonBody { obj ->
        obj.populateConversationFields(addMessageFieldConfig)
    }.build()

    @Pact(consumer = "android")
    fun addMessagePact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
                .given("mobile user with conversations")

                .uponReceiving("add a message to a conversation")
                .path(addMessagePath)
                .method("POST")
                .query(addMessageQuery)
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(addMessageResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Ignore("Changed the endpoint to use fields instead of query string. Currently maintaining Pact tests have low priority.")
    @Test
    @PactVerification(fragment = "addMessagePact")
    fun `add a message to a conversation`() {
        val service = createService()

        val addMessageCall = service.addMessage(
                conversationId = 1,
                recipientIds = listOf("8","9"),
                body="addedMessageBody",
                includedMessageIds=longArrayOf(),
                attachmentIds=longArrayOf(),
                contextCode=null)
        val addMessageResult = addMessageCall.execute()

        assertQueryParamsAndPath(addMessageCall, addMessageQuery, addMessagePath)

        Assert.assertNotNull("Expected non-null response body", addMessageResult.body())
        assertConversationPopulated("Returned conversation", addMessageResult.body()!!, addMessageFieldConfig)
    }
    //endregion

}
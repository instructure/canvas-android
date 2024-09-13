package com.instructure.pandautils.features.inbox.details

import android.content.Context
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InboxDetailsRepositoryTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val context: Context = mockk(relaxed = true)
    private val inboxAPI: InboxApi.InboxInterface = mockk(relaxed = true)
    private val inboxRepository = InboxDetailsRepositoryImpl(inboxAPI)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        ContextKeeper.appContext = context

        mockkObject(CanvasRestAdapter)
        every { CanvasRestAdapter.clearCacheUrls(any()) } returns mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Get Conversation successfully`() = runTest {
        val conversation = Conversation()
        val params = RestParams(isForceReadFromNetwork = false)

        coEvery { inboxAPI.getConversation(conversation.id, true, params) } returns DataResult.Success(conversation)

        val result = inboxRepository.getConversation(conversation.id)

        assertEquals(conversation, result.dataOrNull)
    }

    @Test
    fun `Get Conversation failed`() = runTest {
        val conversation = Conversation()
        val params = RestParams(isForceReadFromNetwork = false)

        coEvery { inboxAPI.getConversation(conversation.id, true, params) } returns DataResult.Fail()

        val result = inboxRepository.getConversation(conversation.id)

        assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Get Conversation successfully with force refresh`() = runTest {
        val conversation = Conversation()
        val params = RestParams(isForceReadFromNetwork = true)

        coEvery { inboxAPI.getConversation(conversation.id, true, params) } returns DataResult.Success(conversation)

        val result = inboxRepository.getConversation(conversation.id, true, true)

        assertEquals(conversation, result.dataOrNull)
    }

    @Test
    fun `Delete Conversation successfully`() = runTest {
        val conversation = Conversation()
        val params = RestParams()

        coEvery { inboxAPI.deleteConversation(conversation.id, params) } returns DataResult.Success(conversation)

        val result = inboxRepository.deleteConversation(conversation.id)

        assertEquals(conversation, result.dataOrNull)
    }

    @Test
    fun `Delete Conversation failed`() = runTest {
        val conversation = Conversation()
        val params = RestParams()

        coEvery { inboxAPI.deleteConversation(conversation.id, params) } returns DataResult.Fail()

        val result = inboxRepository.deleteConversation(conversation.id)

        assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Delete Message successfully`() = runTest {
        val conversation = Conversation()
        val messageIds = listOf(1L)
        val params = RestParams()

        coEvery { inboxAPI.deleteMessages(conversation.id, messageIds, params) } returns DataResult.Success(conversation)

        val result = inboxRepository.deleteMessage(conversation.id, messageIds)

        verify(exactly = 1) { CanvasRestAdapter.clearCacheUrls(any()) }
        assertEquals(conversation, result.dataOrNull)
    }

    @Test
    fun `Delete Message failed`() = runTest {
        val conversation = Conversation()
        val messageIds = listOf(1L)
        val params = RestParams()

        coEvery { inboxAPI.deleteMessages(conversation.id, messageIds, params) } returns DataResult.Fail()

        val result = inboxRepository.deleteMessage(conversation.id, messageIds)

        verify(exactly = 1) { CanvasRestAdapter.clearCacheUrls(any()) }
        assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Update Conversation isStarred successfully`() = runTest {
        val isStarred = true
        val conversation = Conversation()
        val params = RestParams()

        coEvery { inboxAPI.updateConversation(conversation.id, null, isStarred, params) } returns DataResult.Success(conversation.copy(isStarred = isStarred))

        val result = inboxRepository.updateStarred(conversation.id, isStarred)

        verify(exactly = 1) { CanvasRestAdapter.clearCacheUrls(any()) }
        assertEquals(isStarred, result.dataOrNull?.isStarred)
    }

    @Test
    fun `Update Conversation isStarred failed`() = runTest {
        val isStarred = true
        val conversation = Conversation()
        val params = RestParams()

        coEvery { inboxAPI.updateConversation(conversation.id, null, isStarred, params) } returns DataResult.Fail()

        val result = inboxRepository.updateStarred(conversation.id, isStarred)

        verify(exactly = 1) { CanvasRestAdapter.clearCacheUrls(any()) }
        assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Update Conversation workflow state successfully`() = runTest {
        val workflowState = Conversation.WorkflowState.READ
        val conversation = Conversation()
        val params = RestParams()

        coEvery { inboxAPI.updateConversation(conversation.id, workflowState.apiString, null, params) } returns DataResult.Success(conversation.copy(workflowState = workflowState))

        val result = inboxRepository.updateState(conversation.id, workflowState)

        verify(exactly = 1) { CanvasRestAdapter.clearCacheUrls(any()) }
        assertEquals(workflowState, result.dataOrNull?.workflowState)
    }

    @Test
    fun `Update Conversation workflow state failed`() = runTest {
        val workflowState = Conversation.WorkflowState.READ
        val conversation = Conversation()
        val params = RestParams()

        coEvery { inboxAPI.updateConversation(conversation.id, workflowState.apiString, null, params) } returns DataResult.Fail()

        val result = inboxRepository.updateState(conversation.id, workflowState)

        verify(exactly = 1) { CanvasRestAdapter.clearCacheUrls(any()) }
        assertEquals(DataResult.Fail(), result)
    }
}
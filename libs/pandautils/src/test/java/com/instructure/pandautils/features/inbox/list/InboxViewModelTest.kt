package com.instructure.pandautils.features.inbox.list/*
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

import android.content.Context
import android.content.res.Resources
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Progress
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.pandautils.R
import com.instructure.pandautils.features.inbox.list.itemviewmodels.InboxEntryItemViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.EnvironmentFeatureFlag
import com.instructure.pandautils.utils.FeatureFlagProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class InboxViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val inboxRepository: InboxRepository = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val inboxEntryItemCreator: InboxEntryItemCreator = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private lateinit var viewModel: InboxViewModel

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        coEvery { inboxRepository.getCanvasContexts() } returns DataResult.Success(emptyList())
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) } returns DataResult.Success(listOf(Conversation(id = 1)))
        coEvery { inboxRepository.batchUpdateConversations(any(), any()) } returns DataResult.Success(Progress())
        coEvery { inboxRepository.pollProgress(any()) } returns DataResult.Success(Progress(workflowState = "completed"))
        coEvery { inboxRepository.updateConversation(any(), any(), any()) } returns DataResult.Success(
            Conversation()
        )
        
        coEvery { featureFlagProvider.checkEnvironmentFeatureFlag(any()) } returns false

        every { resources.getString(R.string.inboxScopeInbox) } returns "Inbox"
        every { resources.getString(R.string.allCourses) } returns "All Courses"
        every { resources.getString(R.string.errorOccurred) } returns "Error"
        every { resources.getString(R.string.inboxOperationFailed) } returns "Epic Fail"

        every { inboxEntryItemCreator.createInboxEntryItem(any(), any(), any(), any()) } answers { createItem(args[0] as Conversation, args[1], args[2], args[3]) }
    }

    private fun createItem(conversation: Conversation, openConversation: Any?, selectionCallback: Any?, avatarCallback: Any?, starred: Boolean = false, unread: Boolean = false): InboxEntryItemViewModel {
        val viewData = InboxEntryViewData(id = conversation.id, AvatarViewData("", "", false), "", "", "", "", unread, starred, false)
        return InboxEntryItemViewModel(viewData, openConversation as (Boolean, Boolean) -> Unit, selectionCallback as (View, Boolean) -> Unit, avatarCallback as (Boolean) -> Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        
    }

    @Test
    fun `Filters are initally cleared and the Inbox scope is visible`() {
        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}

        assertEquals(InboxViewData(scope = "Inbox", filterText = "All Courses"), viewModel.data.value)
    }

    @Test
    fun `Post error state when fetching conversations fail`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) } returns DataResult.Fail()

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}

        assertEquals(ViewState.Error("Error"), viewModel.state.value)
    }

    @Test
    fun `Post empty state when fetched conversations are empty`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) } returns DataResult.Success(emptyList())

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}

        assertEquals(ViewState.Empty(R.string.nothingUnread, R.string.nothingUnreadSubtext, R.drawable.ic_panda_inboxzero), viewModel.state.value)
    }

    @Test
    fun `Post success state and create items from successful response`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) } returns DataResult.Success(listOf(Conversation(id = 1)))

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}

        assertEquals(ViewState.Success, viewModel.state.value)
        assertEquals(1, viewModel.itemViewModels.value!!.size)
        assertEquals(1, viewModel.itemViewModels.value!![0].data.id)
    }

    @Test
    fun `Succesfull refresh changes displayed items`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) }.returnsMany(
            DataResult.Success(listOf(Conversation(id = 1))),
            DataResult.Success(listOf(Conversation(id = 2)))
        )

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}

        assertEquals(ViewState.Success, viewModel.state.value)
        assertEquals(1, viewModel.itemViewModels.value!![0].data.id)

        viewModel.refresh()

        assertEquals(2, viewModel.itemViewModels.value!![0].data.id)
    }

    @Test
    fun `Failed refresh keeps displayed items and shows refresh error`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) }.returnsMany(
            DataResult.Success(listOf(Conversation(id = 1))),
            DataResult.Fail()
        )

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}

        assertEquals(ViewState.Success, viewModel.state.value)
        assertEquals(1, viewModel.itemViewModels.value!![0].data.id)

        viewModel.refresh()

        assertEquals(1, viewModel.itemViewModels.value!![0].data.id)
        assertEquals(InboxAction.RefreshFailed, viewModel.events.value!!.peekContent())
    }

    @Test
    fun `Open scope selector sends open event`() {
        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}

        viewModel.openScopeSelector()

        assertEquals(InboxAction.OpenScopeSelector, viewModel.events.value!!.peekContent())
    }

    @Test
    fun `Don't fetch data when the already selected scope is selected`() {
        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}

        viewModel.scopeChanged(InboxApi.Scope.INBOX)

        coVerify(exactly = 1) { inboxRepository.getConversations(any(), any(), any(), any()) }
    }

    @Test
    fun `Changing scope fetches new data and changes scope text`() {
        coEvery { inboxRepository.getConversations(eq(InboxApi.Scope.INBOX), any(), any(), any()) } returns DataResult.Success(listOf(Conversation(id = 1)))
        coEvery { inboxRepository.getConversations(eq(InboxApi.Scope.SENT), any(), any(), any()) } returns DataResult.Success(listOf(Conversation(id = 2)))

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}

        viewModel.scopeChanged(InboxApi.Scope.SENT)

        coVerify(exactly = 1) { inboxRepository.getConversations(InboxApi.Scope.INBOX, any(), any(), any()) }
        coVerify(exactly = 1) { inboxRepository.getConversations(InboxApi.Scope.SENT, any(), any(), any()) }
        assertEquals(1, viewModel.itemViewModels.value!!.size)
        assertEquals(2, viewModel.itemViewModels.value!![0].data.id)

    }

    @Test
    fun `Clicking item opens conversation`() {
        val conversation = Conversation(id = 1)
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) } returns DataResult.Success(listOf(conversation))

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.itemViewModels.value!![0].onClick(View(context))

        val events = mutableListOf<Event<InboxAction>>()
        viewModel.events.observeForever {
            events.add(it)
        }

        assertTrue(events.any { it.peekContent() == InboxAction.OpenConversation(conversation, InboxApi.Scope.INBOX) })
    }

    @Test
    fun `Clicking avatar sends avatar clicked action`() {
        val conversation = Conversation(id = 1)
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) } returns DataResult.Success(listOf(conversation))

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.itemViewModels.value!![0].onAvatarClick(View(context))

        val events = mutableListOf<Event<InboxAction>>()
        viewModel.events.observeForever {
            events.add(it)
        }

        assertTrue(events.any { it.peekContent() == InboxAction.AvatarClickedCallback(conversation, InboxApi.Scope.INBOX) })
    }

    @Test
    fun `Star selected items`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) } returns DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2)))

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.itemViewModels.value!![0].onLongClick(View(context))
        viewModel.itemViewModels.value!![1].onClick(View(context))
        viewModel.starSelected()

        assertTrue(viewModel.itemViewModels.value!![0].selected)
        assertTrue(viewModel.itemViewModels.value!![1].selected)
        assertTrue(viewModel.itemViewModels.value!![0].data.starred)
        assertTrue(viewModel.itemViewModels.value!![1].data.starred)
        coVerify { inboxRepository.batchUpdateConversations(any(), eq("star")) }
    }

    @Test
    fun `Unstar selected items`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) } returns DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2)))
        every { inboxEntryItemCreator.createInboxEntryItem(any(), any(), any(), any()) } answers { createItem(args[0] as Conversation, args[1], args[2], args[3], starred = true) }

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.itemViewModels.value!![0].onLongClick(View(context))
        viewModel.itemViewModels.value!![1].onClick(View(context))
        viewModel.unstarSelected()

        assertTrue(viewModel.itemViewModels.value!![0].selected)
        assertTrue(viewModel.itemViewModels.value!![1].selected)
        assertFalse(viewModel.itemViewModels.value!![0].data.starred)
        assertFalse(viewModel.itemViewModels.value!![1].data.starred)
        coVerify { inboxRepository.batchUpdateConversations(any(), eq("unstar")) }
    }

    @Test
    fun `Remove selected items from the list when unstarred in starred scope`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) }.returnsMany(
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))), // We need an other call for the scope change
            DataResult.Success(emptyList())
        )
        every { inboxEntryItemCreator.createInboxEntryItem(any(), any(), any(), any()) } answers { createItem(args[0] as Conversation, args[1], args[2], args[3], starred = true) }

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.scopeChanged(InboxApi.Scope.STARRED)
        viewModel.itemViewModels.value!![0].onLongClick(View(context))
        viewModel.itemViewModels.value!![1].onClick(View(context))
        viewModel.unstarSelected()

        assertTrue(viewModel.itemViewModels.value!!.isEmpty())
        coVerify { inboxRepository.batchUpdateConversations(any(), eq("unstar")) }
    }

    @Test
    fun `Mark selected items as unread`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) } returns DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2)))

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.itemViewModels.value!![0].onLongClick(View(context))
        viewModel.itemViewModels.value!![1].onClick(View(context))
        viewModel.markAsUnreadSelected()

        assertTrue(viewModel.itemViewModels.value!![0].selected)
        assertTrue(viewModel.itemViewModels.value!![1].selected)
        assertTrue(viewModel.itemViewModels.value!![0].data.unread)
        assertTrue(viewModel.itemViewModels.value!![1].data.unread)
        coVerify { inboxRepository.batchUpdateConversations(any(), eq("mark_as_unread")) }
        assertEquals(InboxAction.UpdateUnreadCountOffline(2), viewModel.events.value!!.peekContent())
    }

    @Test
    fun `Remove selected items from the list when marked as unread in archived scope`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) }.returnsMany(
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))), // We need an other call for the scope change
            DataResult.Success(emptyList())
        )

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.scopeChanged(InboxApi.Scope.ARCHIVED)
        viewModel.itemViewModels.value!![0].onLongClick(View(context))
        viewModel.itemViewModels.value!![1].onClick(View(context))
        viewModel.markAsUnreadSelected()

        assertTrue(viewModel.itemViewModels.value!!.isEmpty())
        coVerify { inboxRepository.batchUpdateConversations(any(), eq("mark_as_unread")) }
        assertEquals(InboxAction.UpdateUnreadCountOffline(2), viewModel.events.value!!.peekContent())
    }

    @Test
    fun `Mark selected items as read`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) } returns DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2)))
        every { inboxEntryItemCreator.createInboxEntryItem(any(), any(), any(), any()) } answers { createItem(args[0] as Conversation, args[1], args[2], args[3], unread = true) }

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.itemViewModels.value!![0].onLongClick(View(context))
        viewModel.itemViewModels.value!![1].onClick(View(context))
        viewModel.markAsReadSelected()

        assertTrue(viewModel.itemViewModels.value!![0].selected)
        assertTrue(viewModel.itemViewModels.value!![1].selected)
        assertFalse(viewModel.itemViewModels.value!![0].data.unread)
        assertFalse(viewModel.itemViewModels.value!![1].data.unread)
        coVerify { inboxRepository.batchUpdateConversations(any(), eq("mark_as_read")) }
        assertEquals(InboxAction.UpdateUnreadCountOffline(-2), viewModel.events.value!!.peekContent())
    }

    @Test
    fun `Remove selected items from the list when marked as read in unread scope`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) }.returnsMany(
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))), // We need an other call for the scope change
            DataResult.Success(emptyList())
        )
        every { inboxEntryItemCreator.createInboxEntryItem(any(), any(), any(), any()) } answers { createItem(args[0] as Conversation, args[1], args[2], args[3], unread = true) }

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.scopeChanged(InboxApi.Scope.UNREAD)
        viewModel.itemViewModels.value!![0].onLongClick(View(context))
        viewModel.itemViewModels.value!![1].onClick(View(context))
        viewModel.markAsReadSelected()

        assertTrue(viewModel.itemViewModels.value!!.isEmpty())
        coVerify { inboxRepository.batchUpdateConversations(any(), eq("mark_as_read")) }
        assertEquals(InboxAction.UpdateUnreadCountOffline(-2), viewModel.events.value!!.peekContent())
    }

    @Test
    fun `Remove selected items from the list when deleted`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) }.returnsMany(
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(emptyList())
        )

        viewModel = createViewModel()
        val events = mutableListOf<Event<InboxAction>>()
        viewModel.events.observeForever {
            events.add(it)
        }

        viewModel.data.observe(lifecycleOwner) {}
        viewModel.itemViewModels.value!![0].onLongClick(View(context))
        viewModel.deleteSelected()

        assertEquals(1, viewModel.itemViewModels.value!!.size)
        assertEquals(2, viewModel.itemViewModels.value!![0].data.id)
        coVerify { inboxRepository.batchUpdateConversations(any(), eq("destroy")) }

        assertFalse(events.any { it.peekContent() is InboxAction.UpdateUnreadCountOffline })
    }

    @Test
    fun `Remove selected unread items from the list when deleted`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) }.returnsMany(
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(emptyList())
        )
        every { inboxEntryItemCreator.createInboxEntryItem(any(), any(), any(), any()) } answers { createItem(args[0] as Conversation, args[1], args[2], args[3], unread = true) }

        viewModel = createViewModel()
        val events = mutableListOf<Event<InboxAction>>()
        viewModel.events.observeForever {
            events.add(it)
        }

        viewModel.data.observe(lifecycleOwner) {}
        viewModel.itemViewModels.value!![0].onLongClick(View(context))
        viewModel.deleteSelected()

        assertEquals(1, viewModel.itemViewModels.value!!.size)
        assertEquals(2, viewModel.itemViewModels.value!![0].data.id)
        coVerify { inboxRepository.batchUpdateConversations(any(), eq("destroy")) }

        assertTrue(events.any { it.peekContent() == InboxAction.UpdateUnreadCountOffline(-1) })
    }

    @Test
    fun `Remove selected items from the list when archived`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) }.returnsMany(
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(emptyList())
        )
        every { inboxEntryItemCreator.createInboxEntryItem(any(), any(), any(), any()) } answers { createItem(args[0] as Conversation, args[1], args[2], args[3], unread = true) }

        viewModel = createViewModel()
        val events = mutableListOf<Event<InboxAction>>()
        viewModel.events.observeForever {
            events.add(it)
        }

        viewModel.data.observe(lifecycleOwner) {}
        viewModel.itemViewModels.value!![1].onLongClick(View(context))
        viewModel.archiveSelected()

        assertEquals(1, viewModel.itemViewModels.value!!.size)
        assertEquals(1, viewModel.itemViewModels.value!![0].data.id)
        coVerify { inboxRepository.batchUpdateConversations(any(), eq("archive")) }
        assertTrue(events.any { it.peekContent() == InboxAction.UpdateUnreadCountOffline(-1) })
    }

    @Test
    fun `Don't remove selected items from the list when archived if we are in starred scope`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) }.returnsMany(
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
        )

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.scopeChanged(InboxApi.Scope.STARRED)
        viewModel.itemViewModels.value!![1].onLongClick(View(context))
        viewModel.archiveSelected()

        assertEquals(2, viewModel.itemViewModels.value!!.size)
        coVerify { inboxRepository.batchUpdateConversations(any(), eq("archive")) }
    }

    @Test
    fun `Unarchive selected items`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) }.returnsMany(
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))), // For scope change
            DataResult.Success(emptyList())
        )

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.scopeChanged(InboxApi.Scope.ARCHIVED)
        viewModel.itemViewModels.value!![1].onLongClick(View(context))
        viewModel.unarchiveSelected()

        assertEquals(1, viewModel.itemViewModels.value!!.size)
        assertEquals(1, viewModel.itemViewModels.value!![0].data.id)
        coVerify { inboxRepository.batchUpdateConversations(any(), eq("mark_as_read")) }
    }

    @Test
    fun `Show error when batch update is failed`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) } returns DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2)))
        coEvery { inboxRepository.batchUpdateConversations(any(), any()) } returns DataResult.Fail()

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.itemViewModels.value!![0].onLongClick(View(context))
        viewModel.itemViewModels.value!![1].onClick(View(context))
        viewModel.starSelected()

        assertEquals(InboxAction.ShowConfirmationSnackbar("Epic Fail"), viewModel.events.value!!.peekContent())
    }

    @Test
    fun `Change selection mode when items were selected`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) } returns DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2)))

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.itemViewModels.value!![0].onLongClick(View(context))
        viewModel.itemViewModels.value!![1].onClick(View(context))

        val expectedMenuItems = setOf(InboxMenuItem.DELETE, InboxMenuItem.ARCHIVE, InboxMenuItem.STAR, InboxMenuItem.MARK_AS_UNREAD)
        assertEquals(InboxViewData("Inbox", "2", "All Courses", true, expectedMenuItems), viewModel.data.value)
    }

    @Test
    fun `Change selection mode when items were removed`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) }.returnsMany(
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(emptyList())
        )

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.itemViewModels.value!![0].onLongClick(View(context))
        viewModel.itemViewModels.value!![1].onClick(View(context))
        viewModel.deleteSelected()

        val expectedMenuItems = setOf(InboxMenuItem.DELETE, InboxMenuItem.ARCHIVE, InboxMenuItem.UNSTAR, InboxMenuItem.MARK_AS_UNREAD)
        assertEquals(InboxViewData("Inbox", "0", "All Courses", false, expectedMenuItems), viewModel.data.value)
    }

    @Test
    fun `Disable selection mode on back press`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) } returns DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2)))

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.itemViewModels.value!![0].onLongClick(View(context))
        viewModel.itemViewModels.value!![1].onClick(View(context))
        val backPressHandledByViewModel = viewModel.handleBackPressed()

        assertFalse(viewModel.data.value!!.selectionMode)
        assertTrue(backPressHandledByViewModel)
    }

    @Test
    fun `Don't handle back press if we are not in selection mode`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) } returns DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2)))

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}
        val backPressHandledByViewModel = viewModel.handleBackPressed()

        assertFalse(backPressHandledByViewModel)
    }

    @Test
    fun `Invalidate cache invalidates cached conversations`() {
        viewModel = createViewModel()
        viewModel.invalidateCache()

        verify { inboxRepository.invalidateCachedResponses() }
    }

    @Test
    fun `Create new messages sends correct event`() {
        viewModel = createViewModel()
        viewModel.createNewMessage()

        assertEquals(InboxAction.CreateNewMessage, viewModel.events.value!!.peekContent())
    }

    @Test
    fun `Send open filter selector event when course filter is clicked and contexts are already fetched`() {
        val contexts = listOf(Course(id = 1))
        coEvery { inboxRepository.getCanvasContexts() } returns DataResult.Success(contexts)

        viewModel = createViewModel()
        viewModel.coursesFilterClicked()

        assertEquals(InboxAction.OpenContextFilterSelector(contexts), viewModel.events.value!!.peekContent())
        coVerify (exactly = 1){ inboxRepository.getCanvasContexts() } // Verify that this was not called again when the filter was clicked
    }

    @Test
    fun `Fetch canvas contexts when course filter is clicked when contexts are not present`() {
        val contexts = listOf(Course(id = 1))
        coEvery { inboxRepository.getCanvasContexts() }.returnsMany(
            DataResult.Fail(),
            DataResult.Success(contexts)
        )

        viewModel = createViewModel()
        viewModel.coursesFilterClicked()

        assertEquals(InboxAction.OpenContextFilterSelector(contexts), viewModel.events.value!!.peekContent())
        coVerify (exactly = 2){ inboxRepository.getCanvasContexts() } // Verify that this was called again when the filter was selected
    }

    @Test
    fun `Changing filter fetches new data and changes filter text`() {
        val course = Course(id = 1, name = "Filter")
        val contexts = listOf(course)
        coEvery { inboxRepository.getConversations(any(), any(), null, any()) } returns DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2)))
        coEvery { inboxRepository.getConversations(any(), any(), eq(course), any()) } returns DataResult.Success(listOf(Conversation(id = 2))) // Filtered courses
        coEvery { inboxRepository.getCanvasContexts() } returns DataResult.Success(contexts)

        viewModel = createViewModel()
        viewModel.canvasContextFilterSelected(course.id)

        assertEquals(1, viewModel.itemViewModels.value!!.size)
        assertEquals(2, viewModel.itemViewModels.value!![0].data.id)
        assertEquals(course.name, viewModel.data.value!!.filterText)
    }

    @Test
    fun `Don't fetch data when the already selected filter is selected`() {
        val course = Course(id = 1, name = "Filter")
        val contexts = listOf(course)
        coEvery { inboxRepository.getConversations(any(), any(), null, any()) } returns DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2)))
        coEvery { inboxRepository.getConversations(any(), any(), eq(course), any()) } returns DataResult.Success(listOf(Conversation(id = 2))) // Filtered courses
        coEvery { inboxRepository.getCanvasContexts() } returns DataResult.Success(contexts)

        viewModel = createViewModel()
        viewModel.canvasContextFilterSelected(course.id)
        viewModel.canvasContextFilterSelected(course.id)

        coVerify(exactly = 1) { inboxRepository.getConversations(any(), any(), eq(course), any()) }
    }

    @Test
    fun `Selecting all courses fetches new data and changes filter text`() {
        val course = Course(id = 1, name = "Filter")
        val contexts = listOf(course)
        coEvery { inboxRepository.getConversations(any(), any(), null, any()) } returns DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2)))
        coEvery { inboxRepository.getConversations(any(), any(), eq(course), any()) } returns DataResult.Success(listOf(Conversation(id = 2))) // Filtered courses
        coEvery { inboxRepository.getCanvasContexts() } returns DataResult.Success(contexts)

        viewModel = createViewModel()
        viewModel.canvasContextFilterSelected(course.id)
        viewModel.allCoursesSelected()

        assertEquals(2, viewModel.itemViewModels.value!!.size)
        assertEquals(1, viewModel.itemViewModels.value!![0].data.id)
        assertEquals(2, viewModel.itemViewModels.value!![1].data.id)
        assertEquals("All Courses", viewModel.data.value!!.filterText)
    }

    @Test
    fun `Load next page when bottom is reached and has next page`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), null) } returns
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2)), linkHeaders = LinkHeaders(nextUrl = "http://nextpage.com"))
        coEvery { inboxRepository.getConversations(any(), any(), any(), "http://nextpage.com") } returns
            DataResult.Success(listOf(Conversation(id = 3), Conversation(id = 4)))

        viewModel = createViewModel()
        viewModel.bottomReachedCallback()

        assertEquals(4, viewModel.itemViewModels.value!!.size)
        assertEquals(1, viewModel.itemViewModels.value!![0].data.id)
        assertEquals(2, viewModel.itemViewModels.value!![1].data.id)
        assertEquals(3, viewModel.itemViewModels.value!![2].data.id)
        assertEquals(4, viewModel.itemViewModels.value!![3].data.id)
    }

    @Test
    fun `Don't load next page when bottom is reached and there is no next page`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), null) } returns DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2)))
        coEvery { inboxRepository.getConversations(any(), any(), any(), "http://nextpage.com") } returns
            DataResult.Success(listOf(Conversation(id = 3), Conversation(id = 4)))

        viewModel = createViewModel()
        viewModel.bottomReachedCallback()

        assertEquals(2, viewModel.itemViewModels.value!!.size)
        assertEquals(1, viewModel.itemViewModels.value!![0].data.id)
        assertEquals(2, viewModel.itemViewModels.value!![1].data.id)
    }

    @Test
    fun `Confirm delete sends event with the selected item count`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) }.returnsMany(
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
        )

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.itemViewModels.value!![0].onLongClick(View(context))
        viewModel.itemViewModels.value!![1].onClick(View(context))
        viewModel.confirmDelete()

        assertEquals(InboxAction.ConfirmDelete(2), viewModel.events.value!!.peekContent())
    }

    @Test
    fun `Remove swiped item from the list when archived`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) }.returnsMany(
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(emptyList())
        )

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.archiveConversation(1)

        assertEquals(1, viewModel.itemViewModels.value!!.size)
        assertEquals(2, viewModel.itemViewModels.value!![0].data.id)
        coVerify { inboxRepository.updateConversation(1, Conversation.WorkflowState.ARCHIVED) }
    }

    @Test
    fun `Undoing archived conversation keeps it in the list`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) }.returnsMany(
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(emptyList())
        )

        viewModel = createViewModel()
        val events = mutableListOf<Event<InboxAction>>()
        viewModel.events.observeForever {
            events.add(it)
        }
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.archiveConversation(1)

        val snackbarEvent = events.find { it.peekContent() is InboxAction.ShowConfirmationSnackbar }?.peekContent() as InboxAction.ShowConfirmationSnackbar
        snackbarEvent.undoAction!!.invoke()

        assertEquals(2, viewModel.itemViewModels.value!!.size)
        coVerifyOrder {
            inboxRepository.updateConversation(1, Conversation.WorkflowState.ARCHIVED)
            inboxRepository.updateConversation(1, Conversation.WorkflowState.READ)
        }
    }

    @Test
    fun `Update conversation when marked as read`() {
        every { inboxEntryItemCreator.createInboxEntryItem(any(), any(), any(), any()) } answers { createItem(args[0] as Conversation, args[1], args[2], args[3], unread = true) }
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) }.returnsMany(
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
        )

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.markConversationAsRead(1)

        assertEquals(2, viewModel.itemViewModels.value!!.size)
        assertFalse(viewModel.itemViewModels.value!![0].data.unread)
        assertTrue(viewModel.itemViewModels.value!![1].data.unread)
        coVerify { inboxRepository.updateConversation(1, Conversation.WorkflowState.READ) }
    }

    @Test
    fun `Remove conversation when marked as read in unread scope`() {
        every { inboxEntryItemCreator.createInboxEntryItem(any(), any(), any(), any()) } answers { createItem(args[0] as Conversation, args[1], args[2], args[3], unread = true) }
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) }.returnsMany(
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(emptyList())
        )

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.scopeChanged(InboxApi.Scope.UNREAD)
        viewModel.markConversationAsRead(1)

        assertEquals(1, viewModel.itemViewModels.value!!.size)
        assertEquals(2, viewModel.itemViewModels.value!![0].data.id)
        coVerify { inboxRepository.updateConversation(1, Conversation.WorkflowState.READ) }
    }

    @Test
    fun `Undoing mark conversation as read in unread scope keeps it in the list`() {
        every { inboxEntryItemCreator.createInboxEntryItem(any(), any(), any(), any()) } answers { createItem(args[0] as Conversation, args[1], args[2], args[3], unread = true) }
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) }.returnsMany(
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(emptyList())
        )

        viewModel = createViewModel()
        val events = mutableListOf<Event<InboxAction>>()
        viewModel.events.observeForever {
            events.add(it)
        }
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.scopeChanged(InboxApi.Scope.UNREAD)
        viewModel.markConversationAsRead(1)

        val snackbarEvent = events.find { it.peekContent() is InboxAction.ShowConfirmationSnackbar }?.peekContent() as InboxAction.ShowConfirmationSnackbar
        snackbarEvent.undoAction!!.invoke()

        assertEquals(2, viewModel.itemViewModels.value!!.size)
        coVerifyOrder {
            inboxRepository.updateConversation(1, Conversation.WorkflowState.READ)
            inboxRepository.updateConversation(1, Conversation.WorkflowState.UNREAD)
        }
    }

    @Test
    fun `Update conversation when marked as unread`() {
        every { inboxEntryItemCreator.createInboxEntryItem(any(), any(), any(), any()) } answers { createItem(args[0] as Conversation, args[1], args[2], args[3], unread = false) }
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) }.returnsMany(
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
        )

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.markConversationAsUnread(1)

        assertEquals(2, viewModel.itemViewModels.value!!.size)
        assertTrue(viewModel.itemViewModels.value!![0].data.unread)
        assertFalse(viewModel.itemViewModels.value!![1].data.unread)
        coVerify { inboxRepository.updateConversation(1, Conversation.WorkflowState.UNREAD) }
    }

    @Test
    fun `Remove conversation when marked as unread in archived scope`() {
        every { inboxEntryItemCreator.createInboxEntryItem(any(), any(), any(), any()) } answers { createItem(args[0] as Conversation, args[1], args[2], args[3], unread = false) }
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) }.returnsMany(
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(emptyList())
        )

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.scopeChanged(InboxApi.Scope.ARCHIVED)
        viewModel.markConversationAsUnread(1)

        assertEquals(1, viewModel.itemViewModels.value!!.size)
        assertEquals(2, viewModel.itemViewModels.value!![0].data.id)
        coVerify { inboxRepository.updateConversation(1, Conversation.WorkflowState.UNREAD) }
    }

    @Test
    fun `Undoing mark conversation as unread in archived scope keeps it in the list`() {
        every { inboxEntryItemCreator.createInboxEntryItem(any(), any(), any(), any()) } answers { createItem(args[0] as Conversation, args[1], args[2], args[3], unread = false) }
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) }.returnsMany(
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(emptyList())
        )

        viewModel = createViewModel()
        val events = mutableListOf<Event<InboxAction>>()
        viewModel.events.observeForever {
            events.add(it)
        }
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.scopeChanged(InboxApi.Scope.ARCHIVED)
        viewModel.markConversationAsUnread(1)

        val snackbarEvent = events.find { it.peekContent() is InboxAction.ShowConfirmationSnackbar }?.peekContent() as InboxAction.ShowConfirmationSnackbar
        snackbarEvent.undoAction!!.invoke()

        assertEquals(2, viewModel.itemViewModels.value!!.size)
        coVerifyOrder {
            inboxRepository.updateConversation(1, Conversation.WorkflowState.UNREAD)
            inboxRepository.updateConversation(1, Conversation.WorkflowState.ARCHIVED)
        }
    }

    @Test
    fun `Remove conversation when unstarred`() {
        every { inboxEntryItemCreator.createInboxEntryItem(any(), any(), any(), any()) } answers { createItem(args[0] as Conversation, args[1], args[2], args[3], starred = true) }
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) }.returnsMany(
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(emptyList())
        )

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.scopeChanged(InboxApi.Scope.STARRED)
        viewModel.unstarConversation(1)

        assertEquals(1, viewModel.itemViewModels.value!!.size)
        assertEquals(2, viewModel.itemViewModels.value!![0].data.id)
        coVerify { inboxRepository.updateConversation(1, null, false) }
    }

    @Test
    fun `Undoing unstar conversation keeps it in the list and makes it starred`() {
        every { inboxEntryItemCreator.createInboxEntryItem(any(), any(), any(), any()) } answers { createItem(args[0] as Conversation, args[1], args[2], args[3], starred = true) }
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) }.returnsMany(
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(emptyList())
        )

        viewModel = createViewModel()
        val events = mutableListOf<Event<InboxAction>>()
        viewModel.events.observeForever {
            events.add(it)
        }
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.scopeChanged(InboxApi.Scope.STARRED)
        viewModel.unstarConversation(1)

        val snackbarEvent = events.find { it.peekContent() is InboxAction.ShowConfirmationSnackbar }?.peekContent() as InboxAction.ShowConfirmationSnackbar
        snackbarEvent.undoAction!!.invoke()

        assertEquals(2, viewModel.itemViewModels.value!!.size)
        coVerifyOrder {
            inboxRepository.updateConversation(1, null, false)
            inboxRepository.updateConversation(1, null, true)
        }
    }

    @Test
    fun `Remove conversation when unarchived`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) }.returnsMany(
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(emptyList())
        )

        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.scopeChanged(InboxApi.Scope.ARCHIVED)
        viewModel.unarchiveConversation(1)

        assertEquals(1, viewModel.itemViewModels.value!!.size)
        assertEquals(2, viewModel.itemViewModels.value!![0].data.id)
        coVerify { inboxRepository.updateConversation(1, Conversation.WorkflowState.READ, any()) }
    }

    @Test
    fun `Undoing unarchive conversation keeps it in the list and makes it archived`() {
        coEvery { inboxRepository.getConversations(any(), any(), any(), any()) }.returnsMany(
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(listOf(Conversation(id = 1), Conversation(id = 2))),
            DataResult.Success(emptyList())
        )

        viewModel = createViewModel()
        val events = mutableListOf<Event<InboxAction>>()
        viewModel.events.observeForever {
            events.add(it)
        }
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.scopeChanged(InboxApi.Scope.STARRED)
        viewModel.unarchiveConversation(1)

        val snackbarEvent = events.find { it.peekContent() is InboxAction.ShowConfirmationSnackbar }?.peekContent() as InboxAction.ShowConfirmationSnackbar
        snackbarEvent.undoAction!!.invoke()

        assertEquals(2, viewModel.itemViewModels.value!!.size)
        coVerifyOrder {
            inboxRepository.updateConversation(1, Conversation.WorkflowState.READ, any())
            inboxRepository.updateConversation(1, Conversation.WorkflowState.ARCHIVED, any())
        }
    }

    @Test
    fun `Inbox signature is fetched on init`() {
        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner) {}

        coVerify { inboxRepository.getInboxSignature() }
    }

    @Test
    fun `Create menu items includes delete when feature flag is disabled`() {
        coEvery { featureFlagProvider.checkEnvironmentFeatureFlag(EnvironmentFeatureFlag.RESTRICT_STUDENT_ACCESS) } returns false
        
        viewModel = createViewModel()
        viewModel.events.observe(lifecycleOwner) {}
        viewModel.onSelectionModeEnabled()
        viewModel.onConversationSelected(1L, true)
        viewModel.createMenuItems()
        
        val menuItems = viewModel.uiState.value.menuItems
        assertTrue("Delete menu item should be present when feature flag is disabled", 
            menuItems.any { it.id == ContextMenuItem.DELETE.id })
    }

    @Test
    fun `Create menu items excludes delete when feature flag is enabled`() {
        coEvery { featureFlagProvider.checkEnvironmentFeatureFlag(EnvironmentFeatureFlag.RESTRICT_STUDENT_ACCESS) } returns true
        
        viewModel = createViewModel()
        viewModel.events.observe(lifecycleOwner) {}
        viewModel.onSelectionModeEnabled()
        viewModel.onConversationSelected(1L, true)
        viewModel.createMenuItems()
        
        val menuItems = viewModel.uiState.value.menuItems
        assertFalse("Delete menu item should not be present when feature flag is enabled", 
            menuItems.any { it.id == ContextMenuItem.DELETE.id })
    }

    private fun createViewModel() = InboxViewModel(inboxRepository, resources, inboxEntryItemCreator, featureFlagProvider)
}
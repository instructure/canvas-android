package com.instructure.pandautils.features.discussion.router

import android.content.res.Resources
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.GroupTopicChild
import com.instructure.pandautils.R
import com.instructure.testutils.ViewModelTestRule
import com.instructure.testutils.LifecycleTestOwner
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class DiscussionRouterViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val lifecycleTestOwner = LifecycleTestOwner()

    private val discussionRouteHelper: DiscussionRouteHelper = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)

    private lateinit var viewModel: DiscussionRouterViewModel

    @Before
    fun setUp() {

        mockkStatic("kotlinx.coroutines.AwaitKt")

        coEvery { discussionRouteHelper.shouldShowDiscussionRedesign() } returns true

        viewModel = DiscussionRouterViewModel(discussionRouteHelper, resources)

        setupStrings()
    }

    @Test
    fun `Route to old discussion`() {
        val course = Course()
        val discussionTopicHeader = DiscussionTopicHeader(1L)

        coEvery { discussionRouteHelper.shouldShowDiscussionRedesign() } returns false

        viewModel.events.observe(lifecycleTestOwner.lifecycleOwner) {}

        viewModel.route(course, discussionTopicHeader, 1L, false)

        assertEquals(
            DiscussionRouterAction.RouteToDiscussion(course, false, discussionTopicHeader, false),
            viewModel.events.value?.getContentIfNotHandled()
        )
    }

    @Test
    fun `Route to discussion redesign`() {
        val course = Course()
        val discussionTopicHeader = DiscussionTopicHeader(1L)

        coEvery { discussionRouteHelper.shouldShowDiscussionRedesign() } returns true

        viewModel.events.observe(lifecycleTestOwner.lifecycleOwner) {}

        viewModel.route(course, discussionTopicHeader, 1L, false)

        assertEquals(
            DiscussionRouterAction.RouteToDiscussion(course, true, discussionTopicHeader, false),
            viewModel.events.value?.getContentIfNotHandled()
        )
    }

    @Test
    fun `Route to old group discussion`() {
        val group = Group(1L)
        val discussionTopicHeader = DiscussionTopicHeader(1L, groupTopicChildren = listOf(GroupTopicChild(2L, 1L)))
        val groupDiscussionTopicHeader = DiscussionTopicHeader(2L)

        coEvery { discussionRouteHelper.shouldShowDiscussionRedesign() } returns false
        coEvery { discussionRouteHelper.getDiscussionGroup(discussionTopicHeader) } returns Pair(group, 2L)
        coEvery { discussionRouteHelper.getDiscussionHeader(any(), any()) } returns groupDiscussionTopicHeader

        viewModel.events.observe(lifecycleTestOwner.lifecycleOwner) {}

        viewModel.route(group, discussionTopicHeader, 1L, false)

        assertEquals(
            DiscussionRouterAction.RouteToGroupDiscussion(group, 2L, groupDiscussionTopicHeader, false),
            viewModel.events.value?.getContentIfNotHandled()
        )
    }

    @Test
    fun `Route to group discussion redesign`() {
        val group = Group(1L)
        val discussionTopicHeader = DiscussionTopicHeader(1L, groupTopicChildren = listOf(GroupTopicChild(2L, 1L)))
        val groupDiscussionTopicHeader = DiscussionTopicHeader(2L)

        coEvery { discussionRouteHelper.shouldShowDiscussionRedesign() } returns true
        coEvery { discussionRouteHelper.getDiscussionGroup(discussionTopicHeader) } returns Pair(group, 2L)
        coEvery { discussionRouteHelper.getDiscussionHeader(any(), any()) } returns groupDiscussionTopicHeader

        viewModel.events.observe(lifecycleTestOwner.lifecycleOwner) {}

        viewModel.route(group, discussionTopicHeader, 1L, false)

        assertEquals(
            DiscussionRouterAction.RouteToGroupDiscussion(group, 2L, groupDiscussionTopicHeader, true),
            viewModel.events.value?.getContentIfNotHandled()
        )
    }

    @Test
    fun `Fetch topic header if only id is available`() {
        val course = Course()
        val discussionTopicHeader = DiscussionTopicHeader(1L)

        coEvery { discussionRouteHelper.getDiscussionHeader(any(), any()) } returns discussionTopicHeader

        viewModel.events.observe(lifecycleTestOwner.lifecycleOwner) {}

        viewModel.route(course, null, 1L, false)

        assertEquals(
            DiscussionRouterAction.RouteToDiscussion(course, true, discussionTopicHeader, false),
            viewModel.events.value?.getContentIfNotHandled()
        )
    }

    @Test
    fun `Route to parent discussion when group not found`() {
        val group = Group(1L)
        val discussionTopicHeader = DiscussionTopicHeader(1L, groupTopicChildren = listOf(GroupTopicChild(2L, 1L)))

        coEvery { discussionRouteHelper.getDiscussionGroup(discussionTopicHeader) } returns null

        viewModel.events.observe(lifecycleTestOwner.lifecycleOwner) {}

        viewModel.route(group, discussionTopicHeader, 1L, false)

        assertEquals(
            DiscussionRouterAction.RouteToDiscussion(group, true, discussionTopicHeader, false),
            viewModel.events.value?.getContentIfNotHandled()
        )
    }

    @Test
    fun `Toast when fetching topic header fails`() {
        val course = Course()

        coEvery { discussionRouteHelper.getDiscussionHeader(any(), any()) } throws Exception()

        viewModel.events.observe(lifecycleTestOwner.lifecycleOwner) {}

        viewModel.route(course, null, 1L, false)

        assertEquals(
            DiscussionRouterAction.ShowToast("Error occurred. The topic may no longer be available."),
            viewModel.events.value?.getContentIfNotHandled()
        )
    }

    private fun setupStrings() {
        every { resources.getString(R.string.discussionErrorToast) } returns "Error occurred. The topic may no longer be available."
    }
}

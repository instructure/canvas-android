package com.instructure.pandautils.features.discussion.router

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.GroupTopicChild
import com.instructure.canvasapi2.models.User
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DiscussionRouteHelperTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val discussionRouteHelperRepository: DiscussionRouteHelperRepository = mockk(relaxed = true)

    private var discussionRouteHelper =  DiscussionRouteHelper(discussionRouteHelperRepository)

    @Before
    fun setUp() {

    }

    @Test
    fun `Get Group with DiscussionTopic`() = runTest {
        val group1 = Group(1L)
        val group2 = Group(2L)
        val groups = listOf(group1, group2)
        val discussionTopicHeader = DiscussionTopicHeader(groupTopicChildren = listOf(GroupTopicChild(0L, 2L)))

        coEvery { discussionRouteHelperRepository.getAllGroups(any(), any(), any()) } returns groups

        assertEquals(Pair(group2, 0L), discussionRouteHelper.getDiscussionGroup(discussionTopicHeader, User(1L)))
    }

    @Test
    fun `Get Group returns null if no Group found`() = runTest {
        val group1 = Group(1L)
        val group2 = Group(2L)
        val groups = listOf(group1, group2)
        val discussionTopicHeader = DiscussionTopicHeader(groupTopicChildren = listOf(GroupTopicChild(0L, 3L)))

        coEvery { discussionRouteHelperRepository.getAllGroups(any(), any(), any()) } returns groups

        assertNull(discussionRouteHelper.getDiscussionGroup(discussionTopicHeader, User(1L)))
    }
}
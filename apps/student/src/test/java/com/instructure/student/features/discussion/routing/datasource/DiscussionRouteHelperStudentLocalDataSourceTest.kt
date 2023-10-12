package com.instructure.student.features.discussion.routing.datasource

import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.GroupTopicChild
import com.instructure.pandautils.room.offline.facade.DiscussionTopicHeaderFacade
import com.instructure.pandautils.room.offline.facade.GroupFacade
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class DiscussionRouteHelperStudentLocalDataSourceTest {
    private val discussionTopicHeaderFacade: DiscussionTopicHeaderFacade = mockk(relaxed = true)
    private val groupFacade: GroupFacade = mockk(relaxed = true)

    private val dataSource = DiscussionRouteHelperStudentLocalDataSource(discussionTopicHeaderFacade, groupFacade)

    @Test
    fun `getDiscussionTopicHeader returns correct data`() = runTest {
        val expected = DiscussionTopicHeader(1L)

        coEvery { discussionTopicHeaderFacade.getDiscussionTopicHeaderById(any()) } returns expected

        val result = dataSource.getDiscussionTopicHeader(mockk(), 1L, true)

        TestCase.assertEquals(expected, result)
    }

    @Test
    fun `getAllGroups returns correct data if group exists`() = runTest {
        val discussionTopicHeader = DiscussionTopicHeader(1L, groupTopicChildren = listOf(
            GroupTopicChild(1L, 1L)
        ))
        val groups = listOf(Group(1L))
        val expected = Pair(groups[0], 1L)

        coEvery { groupFacade.getGroupsByUserId(any()) } returns groups

        val result = dataSource.getAllGroups(discussionTopicHeader, 1L, true)

        TestCase.assertEquals(expected, result)
    }
}
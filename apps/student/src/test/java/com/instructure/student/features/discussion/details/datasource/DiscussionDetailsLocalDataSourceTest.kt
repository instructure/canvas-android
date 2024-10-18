package com.instructure.student.features.discussion.details.datasource

import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.room.offline.daos.CourseSettingsDao
import com.instructure.pandautils.room.offline.entities.CourseSettingsEntity
import com.instructure.pandautils.room.offline.facade.DiscussionTopicFacade
import com.instructure.pandautils.room.offline.facade.DiscussionTopicHeaderFacade
import com.instructure.pandautils.room.offline.facade.GroupFacade
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DiscussionDetailsLocalDataSourceTest {
    private val discussionTopicHeaderFacade: DiscussionTopicHeaderFacade = mockk(relaxed = true)
    private val discussionTopicFacade: DiscussionTopicFacade = mockk(relaxed = true)
    private val courseSettingsDao: CourseSettingsDao = mockk(relaxed = true)
    private val groupFacade: GroupFacade = mockk(relaxed = true)

    private val dataSource = DiscussionDetailsLocalDataSource(discussionTopicHeaderFacade, discussionTopicFacade, courseSettingsDao, groupFacade)

    @Test
    fun `Returns correct course settings`() = runTest {
        val expectedCourseSettingsEntity = CourseSettingsEntity(CourseSettings(), 1)

        coEvery { courseSettingsDao.findByCourseId(any()) } returns expectedCourseSettingsEntity

        val result = dataSource.getCourseSettings(1, true).dataOrNull

        assertEquals(expectedCourseSettingsEntity.toApiModel(), result)
    }

    @Test
    fun `Returns detailed discussion`() = runTest {
        val expectedDiscussionTopicHeader = DiscussionTopicHeader(1)

        coEvery { discussionTopicHeaderFacade.getDiscussionTopicHeaderById(any()) } returns expectedDiscussionTopicHeader

        val result = dataSource.getDetailedDiscussion(mockk(),1, true).dataOrNull

        assertEquals(expectedDiscussionTopicHeader, result)
    }

    @Test
    fun `Returns user groups first page`() = runTest {
        val expectedGroups = listOf(Group(1), Group(2))

        coEvery { groupFacade.getGroupsByUserId(any()) } returns expectedGroups

        val result = dataSource.getFirstPageGroups(1, true).dataOrNull

        assertEquals(expectedGroups, result)
    }

    @Test
    fun `User groups next page is always empty`() = runTest {

        val result = dataSource.getNextPageGroups("", true).dataOrNull

        assertEquals(emptyList<Group>(), result)
    }

    @Test
    fun `Get full discussion topic`() = runTest {
        val expectedDiscussionTopic = DiscussionTopic()

        coEvery { discussionTopicFacade.getDiscussionTopic(any()) } returns expectedDiscussionTopic

        val result = dataSource.getFullDiscussionTopic(mockk(), 1, true).dataOrNull

        assertEquals(expectedDiscussionTopic, result)
    }
}
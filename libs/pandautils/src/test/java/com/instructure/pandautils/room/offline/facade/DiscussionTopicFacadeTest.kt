package com.instructure.pandautils.room.offline.facade

import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.DiscussionParticipant
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.pandautils.room.offline.daos.DiscussionEntryDao
import com.instructure.pandautils.room.offline.daos.DiscussionParticipantDao
import com.instructure.pandautils.room.offline.daos.DiscussionTopicDao
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DiscussionTopicFacadeTest {

    private val discussionTopicDao: DiscussionTopicDao = mockk(relaxed = true)
    private val discussionParticipantDao: DiscussionParticipantDao = mockk(relaxed = true)
    private val discussionEntryDao: DiscussionEntryDao = mockk(relaxed = true)

    private val facade = DiscussionTopicFacade(discussionTopicDao, discussionParticipantDao, discussionEntryDao)

    @Test
    fun `Insert discussion topic should insert discussion topic and related entities`() = runTest {
        val discussionTopic = DiscussionTopic(
            participants = mutableListOf(
                DiscussionParticipant(id = 1L),
                DiscussionParticipant(id = 2L)
            ),
            views = mutableListOf(
                DiscussionEntry(id = 1L),
                DiscussionEntry(id = 2L)
            )
        )

        facade.insertDiscussionTopic(1L, discussionTopic)

        coVerify(exactly = 1) { discussionTopicDao.insert(any()) }
        coVerify(exactly = 4) { discussionParticipantDao.upsertAll(any()) }
        coVerify(exactly = 3) { discussionEntryDao.insertAll(any()) }
    }
    @Test
    fun `Get discussion topic should return discussion topic and related entities`() = runTest {
        val discussionTopic = DiscussionTopic(
            participants = mutableListOf(
                DiscussionParticipant(id = 1L),
                DiscussionParticipant(id = 2L)
            ),
            views = mutableListOf(
                DiscussionEntry(id = 1L),
                DiscussionEntry(id = 2L)
            )
        )

        facade.getDiscussionTopic(1L)

        coVerify(exactly = 1) { discussionTopicDao.findById(any()) }
    }

}
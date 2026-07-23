/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.ngc.features.comments.domain

import android.webkit.MimeTypeMap
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.room.appdatabase.daos.PendingSubmissionCommentDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class CompleteMediaCommentUseCaseTest {

    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val useCase = CompleteMediaCommentUseCase(pendingSubmissionCommentDao, apiPrefs)

    @Before
    fun setup() {
        mockkStatic(MimeTypeMap::getSingleton)
        every { MimeTypeMap.getSingleton() } returns mockk(relaxed = true)
        every { MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp4") } returns "video/mp4"
        every { MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3") } returns "audio/mp3"
    }

    @After
    fun tearDown() {
        unmockkStatic(MimeTypeMap::getSingleton)
    }

    @Test
    fun `deletes the optimistic pending row and maps an mp4 file to a VIDEO comment`() = runTest {
        every { apiPrefs.user } returns User(id = 9L, name = "Teacher", avatarUrl = "avatar.png")
        val file = File("/tmp/recording.mp4")

        val result = useCase(CompleteMediaCommentUseCase.Params(pendingId = 12L, file = file))

        coVerify { pendingSubmissionCommentDao.deleteById(12L) }
        assertEquals("12", result.id)
        assertEquals("Teacher", result.authorName)
        assertEquals("9", result.authorId)
        assertEquals("avatar.png", result.authorAvatarUrl)
        assertTrue(result.isOwnComment)
        requireNotNull(result.mediaObject)
        assertEquals("12", result.mediaObject!!.id)
        assertEquals("/tmp/recording.mp4", result.mediaObject!!.mediaDownloadUrl)
        assertEquals(SubmissionCommentMediaType.VIDEO, result.mediaObject!!.mediaType)
        assertEquals("video/mp4", result.mediaObject!!.contentType)
    }

    @Test
    fun `maps a non-mp4 file to an AUDIO comment`() = runTest {
        every { apiPrefs.user } returns User(id = 9L, name = "Teacher", avatarUrl = "avatar.png")
        val file = File("/tmp/recording.mp3")

        val result = useCase(CompleteMediaCommentUseCase.Params(pendingId = 13L, file = file))

        coVerify { pendingSubmissionCommentDao.deleteById(13L) }
        requireNotNull(result.mediaObject)
        assertEquals(SubmissionCommentMediaType.AUDIO, result.mediaObject!!.mediaType)
        assertEquals("audio/mp3", result.mediaObject!!.contentType)
    }
}

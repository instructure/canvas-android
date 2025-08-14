/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.pandautils.features.speedgrader.grade.comments.commentlibrary

import com.apollographql.apollo.api.Error
import com.apollographql.apollo.exception.ApolloGraphQLException
import com.instructure.canvasapi2.managers.CommentLibraryManager
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test


class SpeedGraderCommentLibraryRepositoryTest {

    private val commentLibraryManager: CommentLibraryManager = mockk(relaxed = true)

    private lateinit var repository: SpeedGraderCommentLibraryRepository

    @Before
    fun setup() {
        repository = SpeedGraderCommentLibraryRepository(commentLibraryManager)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Get comment library items calls commentLibraryManager`() = runTest {
        val userId = 1L

        val expected = listOf("Comment 1", "Comment 2", "Comment 3")
        coEvery { commentLibraryManager.getCommentLibraryItems(userId) } returns expected

        val result = repository.getCommentLibraryItems(userId)

        assertEquals(expected, result)
    }

    @Test(expected = ApolloGraphQLException::class)
    fun `Throw exception if commentLibraryManager throws exception`() = runTest {
        val userId = 1L

        coEvery { commentLibraryManager.getCommentLibraryItems(userId) } throws ApolloGraphQLException(Error.Builder("Error").build())

        repository.getCommentLibraryItems(userId)
    }
}

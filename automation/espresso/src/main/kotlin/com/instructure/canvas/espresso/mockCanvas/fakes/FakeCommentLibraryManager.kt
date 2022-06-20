/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.canvas.espresso.mockCanvas.fakes

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvasapi2.CommentLibraryQuery
import com.instructure.canvasapi2.managers.CommentLibraryManager

class FakeCommentLibraryManager() : CommentLibraryManager {

    override suspend fun getCommentLibraryItems(userId: Long): CommentLibraryQuery.Data {
        return createCommentLibraryResponse(userId)
    }

    private fun createCommentLibraryResponse(userId: Long): CommentLibraryQuery.Data {
        val commentLibraryItems = MockCanvas.data.commentLibraryItems[userId]

        val commentBankItems = commentLibraryItems?.map { CommentLibraryQuery.Node("commentBankItem", it, it) } ?: emptyList()
        return CommentLibraryQuery.Data(
            CommentLibraryQuery.AsUser("", "",
                CommentLibraryQuery.CommentBankItems("", commentBankItems)
            )
        )
    }
}
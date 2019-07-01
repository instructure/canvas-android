/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.ui.interaction

import com.instructure.canvas.espresso.Stub
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import org.junit.Test

class DiscussionsInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussionCreate_base() {

    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussionCreate_withAttachment() {

    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussionCreate_disabledWhenNotPermitted() {

    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussion_linksRouteInApp() {
        // Links to other Canvas content routes properly
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussion_postsGetMarkedAsRead() {
        // Replies automatically get marked as read as the user scrolls through the list
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussion_previewAttachment() {

    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussionLikePost_base() {
        // Users can like entries and the correct like count is displayed, if the liking is enabled
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussionLikePost_disabledWhenNotPermitted() {

    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussionView_base() {

    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussionView_replies() {

    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussionView_repliesHiddenWhenNotPermitted() {

    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussionReply_base() {

    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussionReply_withAttachment() {

    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussionReply_threaded() {

    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussionReply_threadedWithAttachment() {

    }
}

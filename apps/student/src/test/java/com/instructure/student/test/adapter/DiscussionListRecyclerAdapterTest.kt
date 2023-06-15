/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 *
 */

package com.instructure.student.test.adapter

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.student.features.discussion.list.DiscussionListRepository
import com.instructure.student.features.discussion.list.adapter.DiscussionListRecyclerAdapter
import io.mockk.mockk
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.*

@RunWith(AndroidJUnit4::class)
class DiscussionListRecyclerAdapterTest : TestCase() {
    private var mAdapter: DiscussionListRecyclerAdapter? = null

    private lateinit var topicHeader: DiscussionTopicHeader
    private lateinit var topicHeader2: DiscussionTopicHeader

    class DiscussionListRecyclerAdapterWrapper(context: Context) : DiscussionListRecyclerAdapter(context, CanvasContext.emptyCourseContext(), true, mockk(relaxed = true),
        object : DiscussionListRecyclerAdapter.AdapterToDiscussionsCallback {
        override fun askToDeleteDiscussion(discussionTopicHeader: DiscussionTopicHeader) {}
        override fun onRefreshStarted() {}
        override fun onRowClicked(discussionTopicHeader: DiscussionTopicHeader, position: Int, isOpenDetail: Boolean) {}
        override fun onRefreshFinished() {}
    }, true)

    @Before
    fun setup() {
        mAdapter = DiscussionListRecyclerAdapterWrapper(RuntimeEnvironment.application)
        topicHeader = DiscussionTopicHeader(title = "discussion1")
        topicHeader2 = DiscussionTopicHeader(title = "discussion2")
    }

    @Test
    fun testAreContentsTheSame_SameTitle() {
        TestCase.assertTrue(mAdapter!!.createItemCallback().areContentsTheSame(topicHeader, topicHeader))
    }

    @Test
    fun testAreContentsTheSame_DifferentTitle() {
        TestCase.assertFalse(mAdapter!!.createItemCallback().areContentsTheSame(topicHeader, topicHeader2))
    }

    // region Compare tests

    @Test
    fun testCompare_bothHaveNullDates() {
        TestCase.assertEquals(-1, mAdapter!!.createItemCallback().compare("", topicHeader, topicHeader2))
        TestCase.assertEquals(-1, mAdapter!!.createItemCallback().compare("", topicHeader2, topicHeader))
        TestCase.assertEquals(-1, mAdapter!!.createItemCallback().compare("", topicHeader, topicHeader))
    }

    @Test
    fun testCompare_oneNullDateLastReply() {
        val date = GregorianCalendar(2014, 11, 29).time
        val d1 = topicHeader.copy(lastReplyDate = date)

        TestCase.assertEquals(-1, mAdapter!!.createItemCallback().compare("", d1, topicHeader2))
        TestCase.assertEquals(1, mAdapter!!.createItemCallback().compare("", topicHeader2, d1))
        TestCase.assertEquals(0, mAdapter!!.createItemCallback().compare("", d1, d1))
    }

    @Test
    fun testCompare_oneNullDatePostedAt() {
        val date = GregorianCalendar(2014, 11, 29).time
        val d1 = topicHeader.copy(postedDate = date)

        TestCase.assertEquals(-1, mAdapter!!.createItemCallback().compare("", d1, topicHeader2))
        TestCase.assertEquals(-1, mAdapter!!.createItemCallback().compare("", topicHeader2, d1))
        TestCase.assertEquals(-1, mAdapter!!.createItemCallback().compare("", d1, d1))
    }

    @Test
    fun testCompare_bothHaveDates() {
        val date = GregorianCalendar(2014, 11, 27).time
        val date2 = GregorianCalendar(2014, 11, 29).time

        val d1 = topicHeader.copy(lastReplyDate = date)
        val d2 = topicHeader2.copy(lastReplyDate = date2)

        // Callback sorts most recent date first
        TestCase.assertEquals(1, mAdapter!!.createItemCallback().compare("", d1, d2))
        TestCase.assertEquals(-1, mAdapter!!.createItemCallback().compare("", d2, d1))
        TestCase.assertEquals(0, mAdapter!!.createItemCallback().compare("", d1, d1))
    }
    // endregion
}

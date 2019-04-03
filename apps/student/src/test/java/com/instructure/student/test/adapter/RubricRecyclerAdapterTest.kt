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
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.RubricCriterionRating
import com.instructure.student.adapter.RubricRecyclerAdapter
import com.instructure.student.model.RubricCommentItem
import com.instructure.student.model.RubricRatingItem
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RubricRecyclerAdapterTest : TestCase() {
    private var mAdapter: RubricRecyclerAdapter? = null

    private lateinit var rating: RubricCriterionRating

    /**
     * Make it so the protected constructor can be called
     */
    class RubricRecyclerAdapterWrapper(context: Context) : RubricRecyclerAdapter(context)

    @Before
    fun setup() {
        mAdapter = RubricRecyclerAdapterWrapper(ApplicationProvider.getApplicationContext())
        rating = RubricCriterionRating(description = "item")
    }

    @Test
    fun testAreContentsTheSame_SameNotComment() {
        val item = RubricRatingItem(rating)
        TestCase.assertTrue(mAdapter!!.createItemCallback().areContentsTheSame(item, item))
    }

    @Test
    fun testAreContentsTheSame_DifferentNotComment() {
        val item = RubricRatingItem(rating)

        val rating1 = RubricCriterionRating(description = "item1")
        val item1 = RubricRatingItem(rating1)

        TestCase.assertFalse(mAdapter!!.createItemCallback().areContentsTheSame(item, item1))
    }

    @Test
    fun testAreContentsTheSame_SameComment() {
        val item = RubricCommentItem("hodor", 0.0)

        TestCase.assertFalse(mAdapter!!.createItemCallback().areContentsTheSame(item, item))
    }
}

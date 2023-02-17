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

package com.emeritus.student.test.adapter

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Recipient
import com.instructure.student.adapter.InboxRecipientAdapter
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChooseMessageRecipientRecyclerAdapterTest : TestCase() {

    private val itemCallback = InboxRecipientAdapter.inboxItemCallback

    @Test
    fun testAreContentsTheSame_SameName() {
        val recipient = Recipient(name = "name")
        TestCase.assertTrue(itemCallback.areContentsTheSame(recipient, recipient))
    }

    @Test
    fun testAreContentsTheSame_DifferentName() {
        val recipient1 = Recipient(name = "name")
        val recipient2 = recipient1.copy(name = "hodor")
        TestCase.assertFalse(itemCallback.areContentsTheSame(recipient1, recipient2))
    }
}

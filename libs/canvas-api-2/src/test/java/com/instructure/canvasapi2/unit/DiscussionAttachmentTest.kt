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

package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.models.DiscussionAttachment
import com.instructure.canvasapi2.utils.toApiString
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*


class DiscussionAttachmentTest {

    @Test
    fun shouldShowToUser_hidden() {
        val attachment = DiscussionAttachment(hidden = true)

        assertEquals(false, attachment.shouldShowToUser())
    }

    @Test
    fun shouldShowToUser_hiddenForUser() {
        val attachment = DiscussionAttachment(hiddenForUser = true)

        assertEquals(false, attachment.shouldShowToUser())
    }

    @Test
    fun shouldShowToUser_unlockedAndVisible() {
        val attachment = DiscussionAttachment(
                // These are false by default, but it's more explicit when we call them out... explicitly
                hidden = false,
                hiddenForUser = false,
                locked = false,
                lockedForUser = false
        )

        assertEquals(true, attachment.shouldShowToUser())
    }

    @Test
    fun shouldShowToUser_locked_noUnlockDate() {
        val attachment = DiscussionAttachment(locked = true, unlockAt = null)

        assertEquals(false, attachment.shouldShowToUser())
    }

    @Test
    fun shouldShowToUser_lockedForUser_noUnlockDate() {
        val attachment = DiscussionAttachment(lockedForUser = true, unlockAt = null)

        assertEquals(false, attachment.shouldShowToUser())
    }

    @Test
    fun shouldShowToUser_locked_unlockDatePassed() {
        val lockDate = Date(System.currentTimeMillis() - MILLIS_PER_DAY)
        val attachment = DiscussionAttachment(unlockAt = lockDate.toApiString(), locked = true)

        assertEquals(true, attachment.shouldShowToUser())
    }

    @Test
    fun shouldShowToUser_locked_unlockDateNotPassed() {
        val lockDate = Date(System.currentTimeMillis() + MILLIS_PER_DAY)
        val attachment = DiscussionAttachment(unlockAt = lockDate.toApiString(), locked = true)

        assertEquals(false, attachment.shouldShowToUser())
    }

    @Test
    fun shouldShowToUser_lockedForUser_unlockDatePassed() {
        val lockDate = Date(System.currentTimeMillis() - MILLIS_PER_DAY)
        val attachment = DiscussionAttachment(unlockAt = lockDate.toApiString(), lockedForUser = true)

        assertEquals(true, attachment.shouldShowToUser())
    }

    @Test
    fun shouldShowToUser_lockedForUser_unlockDateNotPassed() {
        val lockDate = Date(System.currentTimeMillis() + MILLIS_PER_DAY)
        val attachment = DiscussionAttachment(unlockAt = lockDate.toApiString(), lockedForUser = true)

        assertEquals(false, attachment.shouldShowToUser())
    }

    companion object {
        private const val MILLIS_PER_DAY = (24 * 60 * 60 * 10000).toLong()
    }
}

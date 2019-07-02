/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.ui.interaction

import com.instructure.canvas.espresso.Stub
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import org.junit.Test

class AnnouncementInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.ANNOUNCEMENTS, TestCategory.INTERACTION, true)
    fun testAnnouncement_replyToSectionSpecificAnnouncement() {
        // Student enrolled in intended section can see and reply to the announcement
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.ANNOUNCEMENTS, TestCategory.INTERACTION, true)
    fun testAnnouncement_previewAttachment() {
        // User can preview an announcement attachment
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.ANNOUNCEMENTS, TestCategory.INTERACTION, true)
    fun testAnnouncement_reply() {
        // View/reply to an announcement
    }
}
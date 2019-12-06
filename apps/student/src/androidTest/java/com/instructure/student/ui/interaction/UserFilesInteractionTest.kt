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

class UserFilesInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.FILES, TestCategory.INTERACTION, true)
    fun testUpload_deviceFile() {
        // Should be able to upload a file from the user's device
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.FILES, TestCategory.INTERACTION, true)
    fun testUpload_audioFile() {
        // Should be able to upload an audio file
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.FILES, TestCategory.INTERACTION, true)
    fun testUpload_file() {
        // Should be able to upload a file
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.FILES, TestCategory.INTERACTION, true)
    fun testUpload_fileFromCamera() {
        // Should be able to upload a file from the camera
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.FILES, TestCategory.INTERACTION, true)
    fun testUpload_gallery() {
        // Should be able to upload a file from the user's photo gallery
    }

    @Stub
    @Test
    @TestMetaData(Priority.P2, FeatureCategory.FILES, TestCategory.INTERACTION, true)
    fun testView_previewAudio() {
        // Should be able to preview an audio file
    }

    @Stub
    @Test
    @TestMetaData(Priority.P2, FeatureCategory.FILES, TestCategory.INTERACTION, true)
    fun testView_previewVideo() {
        // Should be able to preview a video file
    }

    @Stub
    @Test
    @TestMetaData(Priority.P2, FeatureCategory.FILES, TestCategory.INTERACTION, true)
    fun testView_previewImage() {
        // Should be able to preview an image file
    }

    // TODO - Add all interaction tests for supported file view/previews

}
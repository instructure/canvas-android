/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
package com.instructure.student.features.files.list

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.pandautils.utils.FeatureFlagProvider
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FileListFragmentMenuTest {

    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    @Test
    fun `getFileMenuOptions returns download options for students when feature flag is disabled`() {
        // Given
        val fileFolder = FileFolder(contentType = "pdf", isFile = true, lockedForUser = false)
        val course = Course(id = 1L, enrollments = mutableListOf(
            mockk { 
                coEvery { type } returns "StudentEnrollment"
                coEvery { isStudent } returns true
            }
        ))
        val canvasContext = CanvasContext.fromContextCode("course_1") ?: course

        // When
        val options = FileListFragment.getFileMenuOptions(fileFolder, canvasContext, true, null)

        // Then
        assertTrue("Should include download option", options.contains(FileListFragment.FileMenuType.DOWNLOAD))
        assertTrue("Should include open in alternate option for PDF", options.contains(FileListFragment.FileMenuType.OPEN_IN_ALTERNATE))
        assertFalse("Should not include rename option for students", options.contains(FileListFragment.FileMenuType.RENAME))
        assertFalse("Should not include delete option for students", options.contains(FileListFragment.FileMenuType.DELETE))
    }

    @Test
    fun `getFileMenuOptions returns all options for teachers`() {
        // Given
        val fileFolder = FileFolder(contentType = "pdf", isFile = true, lockedForUser = false)
        val course = Course(id = 1L, enrollments = mutableListOf(
            mockk { 
                coEvery { type } returns "TeacherEnrollment"
                coEvery { isTeacher } returns true
            }
        ))
        val canvasContext = CanvasContext.fromContextCode("course_1") ?: course

        // When
        val options = FileListFragment.getFileMenuOptions(fileFolder, canvasContext, true, null)

        // Then
        assertTrue("Should include download option", options.contains(FileListFragment.FileMenuType.DOWNLOAD))
        assertTrue("Should include open in alternate option for PDF", options.contains(FileListFragment.FileMenuType.OPEN_IN_ALTERNATE))
        assertTrue("Should include rename option for teachers", options.contains(FileListFragment.FileMenuType.RENAME))
        assertTrue("Should include delete option for teachers", options.contains(FileListFragment.FileMenuType.DELETE))
    }

    @Test
    fun `getFileMenuOptions returns all options for user files`() {
        // Given
        val fileFolder = FileFolder(contentType = "pdf", isFile = true, lockedForUser = false)
        val canvasContext = CanvasContext.fromContextCode("user_1")!!

        // When
        val options = FileListFragment.getFileMenuOptions(fileFolder, canvasContext, true, null)

        // Then
        assertTrue("Should include download option", options.contains(FileListFragment.FileMenuType.DOWNLOAD))
        assertTrue("Should include open in alternate option for PDF", options.contains(FileListFragment.FileMenuType.OPEN_IN_ALTERNATE))
        assertTrue("Should include rename option for user files", options.contains(FileListFragment.FileMenuType.RENAME))
        assertTrue("Should include delete option for user files", options.contains(FileListFragment.FileMenuType.DELETE))
    }

    @Test
    fun `getFileMenuOptions excludes options for locked files`() {
        // Given
        val fileFolder = FileFolder(contentType = "pdf", isFile = true, lockedForUser = true)
        val canvasContext = CanvasContext.fromContextCode("user_1")!!

        // When
        val options = FileListFragment.getFileMenuOptions(fileFolder, canvasContext, true, null)

        // Then
        assertTrue("Should return empty list for locked files", options.isEmpty())
    }

    @Test
    fun `getFileMenuOptions excludes open in alternate for non-PDF files`() {
        // Given
        val fileFolder = FileFolder(contentType = "image/png", isFile = true, lockedForUser = false)
        val canvasContext = CanvasContext.fromContextCode("user_1")!!

        // When
        val options = FileListFragment.getFileMenuOptions(fileFolder, canvasContext, true, null)

        // Then
        assertTrue("Should include download option", options.contains(FileListFragment.FileMenuType.DOWNLOAD))
        assertFalse("Should not include open in alternate for non-PDF", options.contains(FileListFragment.FileMenuType.OPEN_IN_ALTERNATE))
    }

    @Test
    fun `getFileMenuOptions excludes download options when offline`() {
        // Given
        val fileFolder = FileFolder(contentType = "pdf", isFile = true, lockedForUser = false)
        val course = Course(id = 1L, enrollments = mutableListOf(
            mockk { 
                coEvery { type } returns "StudentEnrollment"
                coEvery { isStudent } returns true
            }
        ))
        val canvasContext = CanvasContext.fromContextCode("course_1") ?: course

        // When
        val options = FileListFragment.getFileMenuOptions(fileFolder, canvasContext, false, null)

        // Then
        assertFalse("Should not include download option when offline", options.contains(FileListFragment.FileMenuType.DOWNLOAD))
        assertTrue("Should still include open in alternate option for PDF", options.contains(FileListFragment.FileMenuType.OPEN_IN_ALTERNATE))
    }
}

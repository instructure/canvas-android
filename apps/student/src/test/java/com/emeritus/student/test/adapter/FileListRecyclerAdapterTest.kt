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

import android.content.Context
import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.student.adapter.FileFolderCallback
import com.instructure.student.adapter.FileListRecyclerAdapter
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

@RunWith(AndroidJUnit4::class)
class FileListRecyclerAdapterTest : TestCase() {
    private var mAdapter: FileListRecyclerAdapter? = null

    private lateinit var fileFolder: FileFolder
    private lateinit var fileFolder2: FileFolder

    class FileListRecyclerAdapterWrapper(context: Context) : FileListRecyclerAdapter(context, CanvasContext.emptyCourseContext(), emptyList(), FileFolder(), object : FileFolderCallback {
        override fun onItemClicked(item: FileFolder) {}
        override fun onOpenItemMenu(item: FileFolder, anchorView: View) {}
        override fun onRefreshFinished() {}
    }, true)

    @Before
    fun setup() {
        mAdapter = FileListRecyclerAdapterWrapper(RuntimeEnvironment.application)
        fileFolder = FileFolder(displayName = "fileFolder")
        fileFolder2 = FileFolder(displayName = "fileFolder2")
    }

    @Test
    fun testAreContentsTheSame_SameObjects() {
        val ff = fileFolder.copy(size = 0)
        TestCase.assertTrue(mAdapter!!.itemCallback.areContentsTheSame(ff, ff))
    }

    @Test
    fun testAreContentsTheSame_DifferentObjectNames() {
        val ff = fileFolder.copy(size = 100)
        val ff2 = fileFolder2.copy(size = 100)

        TestCase.assertFalse(mAdapter!!.itemCallback.areContentsTheSame(ff, ff2))
    }

    @Test
    fun testAreContentsTheSame_DifferentObjectSizes() {
        val ff = fileFolder.copy(size = 10)
        val ff2 = fileFolder2.copy(size = 100)

        TestCase.assertFalse(mAdapter!!.itemCallback.areContentsTheSame(ff, ff2))
    }

    @Test
    fun testAreContentsTheSame_SameFolders() {
        val ff = fileFolder.copy(size = 0)
        TestCase.assertTrue(mAdapter!!.itemCallback.areContentsTheSame(ff, ff))
    }

    @Test
    fun testAreContentsTheSame_DifferentFolderNames() {
        val ff = fileFolder.copy(size = 100)
        val ff2 = fileFolder2.copy(size = 100)

        TestCase.assertFalse(mAdapter!!.itemCallback.areContentsTheSame(ff, ff2))
    }

    @Test
    fun testAreContentsTheSame_DifferentFolderSizes() {
        val ff = fileFolder.copy(size = 10)
        val ff2 = fileFolder2.copy(size = 100)

        TestCase.assertFalse(mAdapter!!.itemCallback.areContentsTheSame(ff, ff2))
    }

}

/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.pandautils.utils

import android.content.Context
import android.os.Environment
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class StorageUtilsTest {

    private val context: Context = mockk(relaxed = true)
    private val storageUtils = StorageUtils(context)

    @Before
    fun setUp() {
        mockkStatic(Environment::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Returns the size of the storage in bytes`() {
        val totalSpace = 1024L
        every { Environment.getExternalStorageDirectory().totalSpace } returns totalSpace
        val result = storageUtils.getTotalSpace()
        assertEquals(totalSpace, result)
    }

    @Test
    fun `Returns the number of unallocated bytes on the storage`() {
        val freeSpace = 512L
        every { Environment.getExternalStorageDirectory().freeSpace } returns freeSpace
        val result = storageUtils.getFreeSpace()
        assertEquals(freeSpace, result)
    }

    @Test
    fun `Returns the app size in bytes`() {

    }
}

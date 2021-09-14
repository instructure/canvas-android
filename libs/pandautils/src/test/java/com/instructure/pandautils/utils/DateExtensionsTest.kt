/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.utils

import junit.framework.Assert.assertEquals
import org.junit.Test
import java.util.*

class DateExtensionsTest {

    @Test
    fun `Get last Sunday`() {
        val calendar = Calendar.getInstance()
        calendar.set(2021, 6, 1)
        val currentDate = calendar.time
        val lastSunday = currentDate.getLastSunday()

        calendar.set(2021, 5, 27)
        val expectedDate = calendar.time

        assertEquals(expectedDate, lastSunday)
    }

    @Test
    fun `Get next Saturday`() {
        val calendar = Calendar.getInstance()
        calendar.set(2021, 5, 29)
        val currentDate = calendar.time
        val nextSaturday = currentDate.getNextSaturday()

        calendar.set(2021, 6, 3)
        val expectedDate = calendar.time

        assertEquals(expectedDate, nextSaturday)
    }

    @Test
    fun `Is Yesterday`() {
        val calendar = Calendar.getInstance()
        calendar.set(2021, 6, 1)
        val currentDate = calendar.time
        calendar.set(2021, 5, 30)
        val yesterday = calendar.time
        assert(yesterday.isPreviousDay(currentDate))
    }

    @Test
    fun `Is Tomorrow`() {
        val calendar = Calendar.getInstance()
        calendar.set(2021, 6, 31)
        val currentDate = calendar.time
        calendar.set(2021, 7, 1)
        val tomorrow = calendar.time
        assert(tomorrow.isNextDay(currentDate))
    }
}
/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.features.grades

import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemedColor
import com.instructure.parentapp.util.ParentPrefs
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Test


class ParentGradesBehaviourTest {

    private val parentPrefs: ParentPrefs = mockk(relaxed = true)
    private val colorKeeper: ColorKeeper = mockk(relaxed = true)

    private lateinit var gradesBehaviour: ParentGradesBehaviour

    @Before
    fun setup() {
        coEvery { colorKeeper.getOrGenerateUserColor(any()) } returns ThemedColor(1, 1)
    }

    @Test
    fun `Grades behaviour has the correct canvas context color`() {
        createGradesBehaviour()

        val expected = 1

        Assert.assertEquals(expected, gradesBehaviour.canvasContextColor)
    }

    private fun createGradesBehaviour() {
        gradesBehaviour = ParentGradesBehaviour(parentPrefs, colorKeeper)
    }
}

/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */

package com.instructure.teacher.unit

import com.instructure.canvasapi2.models.Course
import com.instructure.teacher.factory.AssignmentListPresenterFactory
import com.instructure.teacher.features.assignment.list.AssignmentListPresenter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before

class AssignmentListPresenterTest {

    // FIXME: This test class requires manager mocking

    private lateinit var mPresenter: AssignmentListPresenter

    @Before
    fun setup() {
        // Create base state for presenter
        val course = Course(id = 1)
        val factory = AssignmentListPresenterFactory(course)
        mPresenter = factory.create()

        // TODO: Replace the next two lines with whatever we end up doing to test the managers
//        CourseManager.setMTesting(true)
//        AssignmentManager.setMTesting(true)
        // Initialize presenters state for testing
        mPresenter.loadData(false)
        mPresenter.selectGradingPeriodIndex(1)
    }

//    @Test
    fun baseStateTest_GradingPeriodsSize() {
        // Assert equals should be written as: assertEquals(expected, actual);
        assertEquals(4, mPresenter.getGradingPeriods().size.toLong())
    }

//    @Test
    fun baseStateTest_SelectedGradingPeriod() {
        assertEquals(1L, mPresenter.getSelectedGradingPeriodId())
    }

//    @Test
    fun baseStateTest_CourseNotNull() {
        assertTrue(mPresenter.getCanvasContext() != null)
    }

//    @Test
    fun selectGradingPeriodIndex_TestNewIndex() {
        mPresenter.selectGradingPeriodIndex(2)
        assertTrue(mPresenter.getSelectedGradingPeriodId() == 2L)
    }

}

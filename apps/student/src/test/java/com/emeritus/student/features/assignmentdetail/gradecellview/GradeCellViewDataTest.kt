///*
// * Copyright (C) 2023 - present Instructure, Inc.
// *
// *     This program is free software: you can redistribute it and/or modify
// *     it under the terms of the GNU General Public License as published by
// *     the Free Software Foundation, version 3 of the License.
// *
// *     This program is distributed in the hope that it will be useful,
// *     but WITHOUT ANY WARRANTY; without even the implied warranty of
// *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *     GNU General Public License for more details.
// *
// *     You should have received a copy of the GNU General Public License
// *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
// *
// */
//
package com.emeritus.student.features.assignmentdetail.gradecellview
//
//import android.content.res.Resources
//import com.instructure.canvasapi2.models.Assignment
//import com.instructure.canvasapi2.models.Course
//import com.instructure.canvasapi2.models.Submission
//import com.instructure.pandautils.utils.ColorKeeper
//import com.instructure.student.features.assignmentdetails.gradecellview.GradeCellViewData
//import io.mockk.mockk
//import org.junit.Assert
//import org.junit.Test
//import java.util.*
//
//class GradeCellViewDataTest {
//
//    private val resources: Resources = mockk(relaxed = true)
//    private val colorKeeper: ColorKeeper = mockk(relaxed = true)
//
//    @Test
//    fun `Map empty grade cell`() {
//        val gradeCell = GradeCellViewData.fromSubmission(
//            resources,
//            colorKeeper.getOrGenerateColor(Course()),
//            Assignment(),
//            Submission()
//        )
//
//        Assert.assertEquals(GradeCellViewData.State.EMPTY, gradeCell.state)
//    }
//
//    @Test
//    fun `Map submitted grade cell`() {
//        val gradeCell = GradeCellViewData.fromSubmission(
//            resources,
//            colorKeeper.getOrGenerateColor(Course()),
//            Assignment(),
//            Submission(submittedAt = Date(), workflowState = "submitted")
//        )
//
//        Assert.assertEquals(GradeCellViewData.State.SUBMITTED, gradeCell.state)
//    }
//
//    @Test
//    fun `Map graded grade cell`() {
//        val gradeCell = GradeCellViewData.fromSubmission(
//            resources,
//            colorKeeper.getOrGenerateColor(Course()),
//            Assignment(),
//            Submission(submittedAt = Date(), grade = "A")
//        )
//
//        Assert.assertEquals(GradeCellViewData.State.GRADED, gradeCell.state)
//    }
//}

/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.parentapp.ui

import com.instructure.espresso.ditto.Ditto
import com.instructure.parentapp.ui.utils.ParentTest
import com.instructure.parentapp.ui.utils.seedData
import com.instructure.parentapp.ui.utils.tokenLogin
import org.junit.Test

class ViewStudentPageTest : ParentTest() {

    @Test
    @Ditto
    override fun displaysPageObjects() {
        val parent = seedData(parents = 1, courses = 1, students = 1).parentsList[0]
        tokenLogin(parent)
        viewStudentPage.assertPageObjects()
    }

    @Test
    @Ditto
    fun displaysStudentName() {
        val data = seedData(parents = 1, courses = 1, students = 1)
        val parent = data.parentsList[0]
        val student = data.studentsList[0]
        tokenLogin(parent)
        viewStudentPage.assertDisplaysStudentName(student)
    }

    @Test
    @Ditto
    fun switchesUsers() {
        val data = seedData(parents = 1, courses = 1, students = 2)
        val parent = data.parentsList[0]
        val student1 = data.studentsList[0]
        val student2 = data.studentsList[1]
        tokenLogin(parent)
        viewStudentPage.selectStudent(student1)
        viewStudentPage.assertDisplaysStudentName(student1)
        viewStudentPage.selectStudent(student2)
        viewStudentPage.assertDisplaysStudentName(student2)
    }

}

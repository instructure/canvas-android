/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.teacher.features.assignment.details

import androidx.lifecycle.LiveData
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsRepository
import com.instructure.pandautils.room.appdatabase.entities.ReminderEntity

class TeacherAssignmentDetailsRepository: AssignmentDetailsRepository {
    override suspend fun getCourseWithGrade(courseId: Long, forceNetwork: Boolean): Course {
        TODO("Not yet implemented")
    }

    override suspend fun getAssignment(
        isObserver: Boolean,
        assignmentId: Long,
        courseId: Long,
        forceNetwork: Boolean
    ): Assignment {
        TODO("Not yet implemented")
    }

    override suspend fun getQuiz(courseId: Long, quizId: Long, forceNetwork: Boolean): Quiz {
        TODO("Not yet implemented")
    }

    override suspend fun getExternalToolLaunchUrl(
        courseId: Long,
        externalToolId: Long,
        assignmentId: Long,
        forceNetwork: Boolean
    ): LTITool? {
        TODO("Not yet implemented")
    }

    override suspend fun getLtiFromAuthenticationUrl(url: String, forceNetwork: Boolean): LTITool? {
        TODO("Not yet implemented")
    }

    override fun getRemindersByAssignmentIdLiveData(
        userId: Long,
        assignmentId: Long
    ): LiveData<List<ReminderEntity>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteReminderById(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun addReminder(
        userId: Long,
        assignment: Assignment,
        text: String,
        time: Long
    ): Long {
        TODO("Not yet implemented")
    }

    override fun isOnline(): Boolean {
        TODO("Not yet implemented")
    }
}
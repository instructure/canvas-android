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